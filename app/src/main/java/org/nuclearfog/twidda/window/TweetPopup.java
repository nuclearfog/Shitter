package org.nuclearfog.twidda.window;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.nuclearfog.twidda.backend.SendStatus;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.ColorPreferences;

/**
 * Tweet Window
 * @see SendStatus
 */
public class TweetPopup extends AppCompatActivity implements View.OnClickListener {

    private EditText tweetfield;
    private long inReplyId;

    @Override
    protected void onCreate(Bundle SavedInstance) {
        super.onCreate(SavedInstance);
        setContentView(R.layout.tweetwindow);
        inReplyId = getIntent().getExtras().getLong("TweetID");

        Button tweetButton = (Button) findViewById(R.id.sendTweet);
        Button closeButton = (Button) findViewById(R.id.close);
        tweetfield = (EditText) findViewById(R.id.tweet_input);

        final int size = LinearLayout.LayoutParams.WRAP_CONTENT;
        getWindow().setLayout(size, size);

        closeButton.setOnClickListener(this);
        tweetButton.setOnClickListener(this);

        LinearLayout root = (LinearLayout) findViewById(R.id.tweet_popup);
        ColorPreferences mColor = ColorPreferences.getInstance(this);
        root.setBackgroundColor(mColor.getTweetColor());
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
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void send() {
        String tweet = tweetfield.getText().toString();
        SendStatus sendTweet = new SendStatus(getApplicationContext());
        if(inReplyId > 0)
            sendTweet.execute(tweet, inReplyId);
        else
            sendTweet.execute(tweet);
        finish();
    }
}