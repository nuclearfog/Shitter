package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.nuclearfog.twidda.backend.SendStatus;
import org.nuclearfog.twidda.R;

/**
 * Tweet Window
 * @see SendStatus
 */
public class TweetPopup extends AppCompatActivity implements View.OnClickListener {

    private EditText tweetfield;
    private ImageView tweetImg1,tweetImg2,tweetImg3,tweetImg4;
    private long inReplyId;
    private String imgPath, hashtag="";

    @Override
    protected void onCreate(Bundle SavedInstance) {
        super.onCreate(SavedInstance);
        setContentView(R.layout.tweetwindow);

        inReplyId = getIntent().getExtras().getLong("TweetID");
        if(getIntent().hasExtra("Hashtag"))
            hashtag = getIntent().getExtras().getString("Hashtag");

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
        tweetfield.setText(hashtag);
    }

    @Override
    protected void onDestroy() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.sendTweet:
                send();
                break;
            case R.id.close:
                finish();
                break;
            case R.id.image:
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_FIRST_USER );
                break;
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i){
        super.onActivityResult(reqCode,returnCode,i);
        if(returnCode == RESULT_OK){
            Uri imageInput = i.getData();
            String[] filepath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(imageInput,filepath,null,null,null);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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