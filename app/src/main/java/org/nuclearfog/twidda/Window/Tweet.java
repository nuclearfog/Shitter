package org.nuclearfog.twidda.Window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.nuclearfog.twidda.DataBase.TweetDatabase;
import org.nuclearfog.twidda.R;

public class Tweet extends AppCompatActivity {

    private TweetDatabase mTweet;
    private TextView tweet, username;
    private long tweetID;
    private long userID;

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.tweet_detail);
        tweetID = getIntent().getExtras().getLong("tweetID");
        userID = getIntent().getExtras().getLong("userID");
        tweet = (TextView) findViewById(R.id.tweetdetail);
        username = (TextView) findViewById(R.id.usernamedetail);

        ImageView pb = (ImageView) findViewById(R.id.profileimage_detail);
        pb.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                      Intent profile = new Intent(getApplicationContext(), Profile.class);
                                      Bundle bundle = new Bundle();
                                      bundle.putLong("userID",userID);
                                      profile.putExtras(bundle);
                                      startActivity(profile);
                                  }
                              });
        setContent();
    }



    private void setContent() {
        mTweet = new TweetDatabase(getApplicationContext(),TweetDatabase.GET_TWEET,tweetID);
        tweet.setText(mTweet.getTweet(0));
        username.setText(mTweet.getUsername(0));

    }
}