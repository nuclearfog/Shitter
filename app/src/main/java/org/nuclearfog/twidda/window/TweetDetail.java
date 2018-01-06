package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.nuclearfog.twidda.backend.ShowStatus;
import org.nuclearfog.twidda.database.ColorPreferences;
import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;

public class TweetDetail extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ListView answer_list;
    private long tweetID;
    private long userID;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.tweet_detail);
        tweetID = getIntent().getExtras().getLong("tweetID");
        userID = getIntent().getExtras().getLong("userID");

        answer_list = (ListView) findViewById(R.id.answer_list);
        Button answer = (Button) findViewById(R.id.answer_button);
        Button retweet = (Button) findViewById(R.id.rt_button);
        Button favorite = (Button) findViewById(R.id.fav_button);
        ImageView pb = (ImageView) findViewById(R.id.profileimage_detail);

        answer_list.setOnItemClickListener(this);
        answer.setOnClickListener(this);
        retweet.setOnClickListener(this);
        favorite.setOnClickListener(this);
        pb.setOnClickListener(this);
        setContent();
    }

    @Override
    protected void onDestroy() {
        // TODO save tweet
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle bundle = new Bundle();
        switch(v.getId()) {
            case R.id.answer_button:
                intent = new Intent(getApplicationContext(), TweetPopup.class);
                bundle.putLong("TweetID", tweetID);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.rt_button:
                //todo
                break;
            case R.id.fav_button:
                //todo
                break;
            case R.id.profileimage_detail:
                intent = new Intent(getApplicationContext(), UserProfile.class);
                bundle.putLong("userID",userID);
                bundle.putBoolean("home", false);//todo
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TimelineAdapter tlAdp = (TimelineAdapter) answer_list.getAdapter();
        TweetDatabase twDB = tlAdp.getAdapter();
        long userID = twDB.getUserID(position);
        long tweetID = twDB.getTweetId(position);
        Intent intent = new Intent(getApplicationContext(), TweetDetail.class);
        Bundle bundle = new Bundle();
        bundle.putLong("userID",userID);
        bundle.putLong("tweetID",tweetID);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void setContent() {
        ColorPreferences mColor = ColorPreferences.getInstance(getApplicationContext());
        LinearLayout background = (LinearLayout) findViewById(R.id.tweet_detail);
        background.setBackgroundColor(mColor.getBackgroundColor());//TODO
        ShowStatus set = new ShowStatus(this);
        set.execute(tweetID);
    }
}