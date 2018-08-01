package org.nuclearfog.twidda.window;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.GlobalSettings;
import org.nuclearfog.twidda.backend.ImagePopup;
import org.nuclearfog.twidda.backend.StatusUpload;

import java.util.ArrayList;
import java.util.List;


public class TweetPopup extends AppCompatActivity implements OnClickListener,
        DialogInterface.OnClickListener, StatusUpload.OnTweetSending {

    private StatusUpload sendTweet;
    private View imageButton, previewBtn;
    private View send_circle;
    private List<String> mediaPath;
    private TextView imgCount;
    private EditText tweet;
    private String addition = "";
    private long inReplyId = -1L;
    private int imgIndex = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        b = getIntent().getExtras();
        if (b != null) {
            if (b.containsKey("TweetID"))
                inReplyId = b.getLong("TweetID");
            if (b.containsKey("Addition"))
                addition = b.getString("Addition") + " ";
        }
        setContentView(R.layout.tweetwindow);

        GlobalSettings settings = GlobalSettings.getInstance(this);

        View tweetButton = findViewById(R.id.sendTweet);
        View closeButton = findViewById(R.id.close);
        View root = findViewById(R.id.tweet_popup);

        mediaPath = new ArrayList<>();
        imageButton = findViewById(R.id.image);
        previewBtn  = findViewById(R.id.img_preview);
        tweet = findViewById(R.id.tweet_input);
        imgCount = findViewById(R.id.imgcount);
        send_circle = findViewById(R.id.tweet_sending);
        root.setBackgroundColor(settings.getTweetColor());
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
                    imgCount.setText(count);
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
        if(sendTweet != null)
            sendTweet.cancel(true);
        finish();
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
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
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
            alerta.setPositiveButton(R.string.yes_confirm,this);
            alerta.setNegativeButton(R.string.no_confirm,null);
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
}