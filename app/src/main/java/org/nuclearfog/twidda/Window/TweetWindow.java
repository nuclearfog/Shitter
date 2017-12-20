package org.nuclearfog.twidda.Window;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.nuclearfog.twidda.Engine.SendStatus;
import org.nuclearfog.twidda.Engine.TwitterStore;
import org.nuclearfog.twidda.R;

import twitter4j.Twitter;
import twitter4j.TwitterException;


public class TweetWindow extends AppCompatActivity {

    private EditText tweetfield;

    @Override
    protected void onCreate(Bundle SavedInstance){
        super.onCreate(SavedInstance);
        getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setContentView(R.layout.tweet_popup);
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

    private void send(){
        String tweet = tweetfield.getText().toString();
        SendStatus sendTweet = new SendStatus(getApplicationContext());
        sendTweet.execute(SendStatus.SEND_STATUS, tweet);
        finish();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

}
