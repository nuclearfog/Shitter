package org.nuclearfog.twidda.Window;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.nuclearfog.twidda.R;


public class TweetWindow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle SavedInstance){
        super.onCreate(SavedInstance);
        getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setContentView(R.layout.tweet_popup);

        EditText tweet = (EditText) findViewById(R.id.tweet_input);

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

            }
        });

    }

}
