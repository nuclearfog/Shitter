package org.nuclearfog.twidda.window;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.ImagePopup;
import org.nuclearfog.twidda.backend.StatusUpload;

import java.util.ArrayList;
import java.util.List;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

/**
 * Tweet Window
 * @see StatusUpload
 */
public class TweetPopup extends AppCompatActivity implements View.OnClickListener,
        DialogInterface.OnClickListener, StatusUpload.TweetSender {

    private StatusUpload sendTweet;
    private ProgressBar send_circle;
    private EditText tweet;
    private Button imageButton, previewBtn;
    private TextView imgcount;
    private long inReplyId =-1L;
    private String addition="";
    private int imgIndex = 0;
    private List<String> mediaPath;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.tweetwindow);
        getExtras(getIntent().getExtras());

        mediaPath = new ArrayList<>();
        imageButton = findViewById(R.id.image);
        previewBtn  = findViewById(R.id.img_preview);
        tweet = findViewById(R.id.tweet_input);
        imgcount = findViewById(R.id.imgcount);
        send_circle = findViewById(R.id.tweet_sending);
        Button tweetButton = findViewById(R.id.sendTweet);
        Button closeButton = findViewById(R.id.close);
        LinearLayout root = findViewById(R.id.tweet_popup);
        SharedPreferences settings = getSharedPreferences("settings", 0);
        int tweetColor = settings.getInt("tweet_color", 0xff19aae8);
        root.setBackgroundColor(tweetColor);
        tweet.append(addition);

        closeButton.setOnClickListener(this);
        tweetButton.setOnClickListener(this);
        imageButton.setOnClickListener(this);
        previewBtn.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        showClosingMsg();
    }

    /**
     * Home Button
     */
    @Override
    protected void onUserLeaveHint(){
        super.onUserLeaveHint();
        overridePendingTransition(0,0);
    }

    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        super.onActivityResult(reqCode,returnCode,i);
        if(returnCode == RESULT_OK){
            String[] mode = {MediaStore.Images.Media.DATA};
            if(i.getData() == null)
                return;
            Cursor c = getContentResolver().query(i.getData(),mode,null,null,null);
            if(c != null && c.moveToFirst()) {
                if(imgIndex == 0) {
                    previewBtn.setVisibility(View.VISIBLE);
                }
                if(imgIndex  < 4) {
                    int index = c.getColumnIndex(mode[0]);
                    mediaPath.add(c.getString(index));
                    String count = Integer.toString(++imgIndex);
                    imgcount.setText(count);
                }
                if(imgIndex == 4) {
                    imageButton.setVisibility(View.INVISIBLE);
                }
                c.close();
            }
        }
    }

    @Override
    public void onClick(DialogInterface d, int id) {
        switch(id) {
            case BUTTON_POSITIVE:
                if(sendTweet != null)
                    sendTweet.cancel(true);
                finish();
                break;
            case BUTTON_NEGATIVE:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if(send_circle.getVisibility() == View.VISIBLE)
            return;

        switch(v.getId()){
            case R.id.sendTweet:
                send();
                break;
            case R.id.close:
                showClosingMsg();
                break;
            case R.id.image:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int check = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                    if(check == PackageManager.PERMISSION_GRANTED) {
                        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, 0);
                    }
                    else {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                } else {
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, 0);
                }
                break;
            case R.id.img_preview:
                new ImagePopup(this).execute(mediaPath.toArray(new String[mediaPath.size()]));
                break;
        }
    }

    private void showClosingMsg() {
        if( !addition.equals(tweet.getText().toString()) || imgIndex > 0) {
            AlertDialog.Builder alerta = new AlertDialog.Builder(this);
            alerta.setMessage("Tweet verwerfen?");
            alerta.setPositiveButton(R.string.yes_confirm, this);
            alerta.setNegativeButton(R.string.no_confirm, this);
            alerta.show();
        } else {
            finish();
        }
    }

    @Override
    public void send() {
        String tweetStr = tweet.getText().toString();
        String[] paths = new String[mediaPath.size()];
        paths = mediaPath.toArray(paths);
        sendTweet = new StatusUpload(this ,paths);
        if(!tweetStr.trim().isEmpty() || paths.length > 0) {
            if(inReplyId > 0) {
                sendTweet.execute(tweetStr, inReplyId);
            } else {
                sendTweet.execute(tweetStr);
            }
        }
    }

    private void getExtras(@Nullable Bundle b) {
        if(b != null) {
            if (b.containsKey("TweetID"))
                inReplyId = b.getLong("TweetID");
            if (b.containsKey("Addition"))
                addition = b.getString("Addition") + " ";
        }
    }
}