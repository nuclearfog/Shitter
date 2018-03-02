package org.nuclearfog.twidda.window;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import static android.content.DialogInterface.*;

import org.nuclearfog.twidda.backend.ImagePopup;
import org.nuclearfog.twidda.backend.StatusUpload;
import org.nuclearfog.twidda.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Tweet Window
 * @see StatusUpload
 */
public class TweetPopup extends AppCompatActivity implements View.OnClickListener,
        DialogInterface.OnClickListener {

    private EditText tweetfield;
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
        imageButton = (Button) findViewById(R.id.image);
        previewBtn  = (Button) findViewById(R.id.img_preview);
        tweetfield = (EditText) findViewById(R.id.tweet_input);
        imgcount = (TextView) findViewById(R.id.imgcount);
        Button tweetButton = (Button) findViewById(R.id.sendTweet);
        Button closeButton = (Button) findViewById(R.id.close);
        LinearLayout root = (LinearLayout) findViewById(R.id.tweet_popup);
        ColorPreferences mColor = ColorPreferences.getInstance(this);
        root.setBackgroundColor(mColor.getColor(ColorPreferences.TWEET_COLOR));
        tweetfield.append(addition);

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
                finish();
                break;
            case BUTTON_NEGATIVE:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.sendTweet:
                send();
                break;
            case R.id.close:
                showClosingMsg();
                break;
            case R.id.image:
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 0);
                break;
            case R.id.img_preview:
                new ImagePopup(this).execute(mediaPath.toArray(new String[mediaPath.size()]));
                break;
        }
    }

    private void showClosingMsg() {
        if( !addition.equals(tweetfield.getText().toString()) || imgIndex > 0) {
            AlertDialog.Builder alerta = new AlertDialog.Builder(this);
            alerta.setMessage("Tweet verwerfen?");
            alerta.setPositiveButton(R.string.yes_confirm, this);
            alerta.setNegativeButton(R.string.no_confirm, this);
            alerta.show();
        } else {
            finish();
        }
    }

    private void send() {
        String tweet = tweetfield.getText().toString();
        String[] paths = new String[mediaPath.size()];
        paths = mediaPath.toArray(paths);
        StatusUpload sendTweet;
        sendTweet = new StatusUpload(getApplicationContext(),paths);

        if(inReplyId > 0) {
            sendTweet.execute(tweet, inReplyId);
        } else {
            sendTweet.execute(tweet);
        }
        finish();
    }

    @SuppressWarnings("ConstantCondidions")
    private void getExtras(Bundle b) {
        if(b.containsKey("TweetID"))
            inReplyId = b.getLong("TweetID");
        if(b.containsKey("Addition"))
            addition = b.getString("Addition")+" ";
    }
}