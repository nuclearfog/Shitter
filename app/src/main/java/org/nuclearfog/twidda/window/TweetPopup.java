package org.nuclearfog.twidda.window;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import static android.content.DialogInterface.*;

import org.nuclearfog.twidda.backend.SendStatus;
import org.nuclearfog.twidda.R;

/**
 * Tweet Window
 * @see SendStatus
 */
public class TweetPopup extends AppCompatActivity implements View.OnClickListener,
        DialogInterface.OnClickListener {

    private ImageView tweetImg1,tweetImg2,tweetImg3,tweetImg4;
    private EditText tweetfield;
    private long inReplyId =-1L;
    private String imgPath, addition="";

    @Override
    protected void onCreate(Bundle SavedInstance) {
        super.onCreate(SavedInstance);
        setContentView(R.layout.tweetwindow);
        if(getIntent().hasExtra("TweetID"))
            inReplyId = getIntent().getExtras().getLong("TweetID");
        if(getIntent().hasExtra("Addition"))
            addition = getIntent().getExtras().getString("Addition");

        final int size = LinearLayout.LayoutParams.WRAP_CONTENT;
        getWindow().setLayout(size, size);

        Button tweetButton = (Button) findViewById(R.id.sendTweet);
        Button closeButton = (Button) findViewById(R.id.close);
        Button imageButton = (Button) findViewById(R.id.image);
        tweetfield = (EditText) findViewById(R.id.tweet_input);
        tweetImg1 = (ImageView) findViewById(R.id.tweetImg1);
        tweetImg2 = (ImageView) findViewById(R.id.tweetImg2);
        tweetImg3 = (ImageView) findViewById(R.id.tweetImg3);
        tweetImg4 = (ImageView) findViewById(R.id.tweetImg4);

        closeButton.setOnClickListener(this);
        tweetButton.setOnClickListener(this);
        imageButton.setOnClickListener(this);

        LinearLayout root = (LinearLayout) findViewById(R.id.tweet_popup);
        ColorPreferences mColor = ColorPreferences.getInstance(this);
        root.setBackgroundColor(mColor.getColor(ColorPreferences.TWEET_COLOR));
        tweetfield.setText(addition);
    }


    @Override
    public void onBackPressed() {
        showClosingMsg();
    }

    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        super.onActivityResult(reqCode,returnCode,i);
        if(returnCode == RESULT_OK){
            String[] filepath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(i.getData(),filepath,null,null,null);
            if(c != null) {
                if(c.moveToFirst()) {
                    int index = c.getColumnIndex(filepath[0]);
                    imgPath = c.getString(index);
                    Bitmap img = BitmapFactory.decodeFile(imgPath);
                    tweetImg1.setImageBitmap(img);
                    tweetImg1.setVisibility(View.VISIBLE);
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
        SendStatus sendTweet;
        if(imgPath == null) {
            sendTweet = new SendStatus(getApplicationContext(), "");
        } else {
            sendTweet = new SendStatus(getApplicationContext(), imgPath);
        } if(inReplyId > 0) {
            sendTweet.execute(tweet, inReplyId);
        } else {
            sendTweet.execute(tweet);
        }
        finish();
    }
}