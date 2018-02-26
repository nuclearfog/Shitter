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
    Button imageButton, previewBtn;
    private long inReplyId =-1L;
    private String addition="";
    private int imgIndex = 0;
    private List<String> mediaPath;

    @Override
    protected void onCreate(Bundle SavedInstance) {
        super.onCreate(SavedInstance);
        setContentView(R.layout.tweetwindow);
        if(getIntent().hasExtra("TweetID"))
            inReplyId = getIntent().getExtras().getLong("TweetID");
        if(getIntent().hasExtra("Addition"))
            addition = getIntent().getExtras().getString("Addition");

       // final int size = LinearLayout.LayoutParams.WRAP_CONTENT;
      //  getWindow().setLayout(size, size);
        mediaPath = new ArrayList<>();


        Button tweetButton = (Button) findViewById(R.id.sendTweet);
        Button closeButton = (Button) findViewById(R.id.close);
        imageButton = (Button) findViewById(R.id.image);
        previewBtn  = (Button) findViewById(R.id.img_preview);
        tweetfield = (EditText) findViewById(R.id.tweet_input);

        LinearLayout root = (LinearLayout) findViewById(R.id.tweet_popup);
        ColorPreferences mColor = ColorPreferences.getInstance(this);
        root.setBackgroundColor(mColor.getColor(ColorPreferences.TWEET_COLOR));
        tweetfield.setText(addition);

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
            Cursor c = getContentResolver().query(i.getData(),mode,null,null,null);
            if(c != null && c.moveToFirst()) {
                if(imgIndex == 0) {
                    previewBtn.setVisibility(View.VISIBLE);
                }
                if(imgIndex  < 4) {
                    int index = c.getColumnIndex(mode[0]);
                    mediaPath.add(c.getString(index));
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
                new ImagePopup(this).execute((String[])mediaPath.toArray());
                break;
        }
    }

    private void showClosingMsg() {
        if( !addition.equals(tweetfield.getText().toString()) ){
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
}