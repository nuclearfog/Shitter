package org.nuclearfog.twidda.window;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.nuclearfog.twidda.backend.SendStatus;
import org.nuclearfog.twidda.R;

public class TweetPopup extends AppCompatActivity {

    private EditText tweetfield;
    private long inReplyId;

    @Override
    protected void onCreate(Bundle SavedInstance) {
        super.onCreate(SavedInstance);
        setContentView(R.layout.tweetwindow);

        inReplyId = getIntent().getExtras().getLong("TweetID");

        getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        tweetfield = (EditText) findViewById(R.id.tweet_input);

        Button closeButton = (Button) findViewById(R.id.close);
        closeButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button tweetButton = (Button) findViewById(R.id.sendTweet);
        tweetButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}