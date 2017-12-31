package org.nuclearfog.twidda.Window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.nuclearfog.twidda.Backend.ShowStatus;
import org.nuclearfog.twidda.DataBase.TweetDatabase;
import org.nuclearfog.twidda.R;

public class TweetDetail extends AppCompatActivity {

    private TweetDatabase mTweet;

    private long tweetID;
    private long userID;

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.tweet_detail);
        tweetID = getIntent().getExtras().getLong("tweetID");
        userID = getIntent().getExtras().getLong("userID");


        ImageView pb = (ImageView) findViewById(R.id.profileimage_detail);
        pb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent profile = new Intent(getApplicationContext(), UserProfile.class);
              Bundle bundle = new Bundle();
              bundle.putLong("userID",userID);
              profile.putExtras(bundle);
              startActivity(profile);
            }
        });
        setContent();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    private void setContent() {
        ShowStatus set = new ShowStatus(this);
        set.execute(tweetID);

    }
}