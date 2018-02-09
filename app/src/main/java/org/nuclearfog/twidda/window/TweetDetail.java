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
import android.widget.TextView;

import org.nuclearfog.twidda.backend.ShowStatus;
import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;

/**
 * Detailed Tweet Window
 * @see ShowStatus
 */
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
        Button retweet = (Button) findViewById(R.id.rt_button_detail);
        Button favorite = (Button) findViewById(R.id.fav_button_detail);
        ImageView pb = (ImageView) findViewById(R.id.profileimage_detail);

        TextView txtRt = (TextView) findViewById(R.id.no_rt_detail);
        TextView txtFav = (TextView) findViewById(R.id.no_fav_detail);

        answer_list.setOnItemClickListener(this);
        favorite.setOnClickListener(this);
        retweet.setOnClickListener(this);
        answer.setOnClickListener(this);
        txtFav.setOnClickListener(this);
        txtRt.setOnClickListener(this);
        pb.setOnClickListener(this);
        setContent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle bundle = new Bundle();
        ShowStatus mStat = new ShowStatus(this);
        switch(v.getId()) {
            case R.id.answer_button:
                intent = new Intent(getApplicationContext(), TweetPopup.class);
                bundle.putLong("TweetID", tweetID);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.rt_button_detail:
                mStat.execute(tweetID, ShowStatus.RETWEET);
                break;
            case R.id.fav_button_detail:
                mStat.execute(tweetID, ShowStatus.FAVORITE);
                break;
            case R.id.profileimage_detail:
                intent = new Intent(getApplicationContext(), UserProfile.class);
                bundle.putLong("userID",userID);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.no_rt_detail:
                intent = new Intent(getApplicationContext(), UserDetail.class);
                bundle.putLong("userID",userID);
                bundle.putLong("tweetID",tweetID);
                bundle.putLong("mode",2L);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.no_fav_detail:
                intent = new Intent(getApplicationContext(), UserDetail.class);
                bundle.putLong("userID",userID);
                bundle.putLong("tweetID",tweetID);
                bundle.putLong("mode",3L);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TimelineAdapter tlAdp = (TimelineAdapter) answer_list.getAdapter();
        TweetDatabase twDB = tlAdp.getData();
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
        int backgroundColor = mColor.getColor(ColorPreferences.BACKGROUND);
        int fontColor = mColor.getColor(ColorPreferences.FONT_COLOR);
        LinearLayout background = (LinearLayout) findViewById(R.id.tweet_detail);
        LinearLayout tweetaction = (LinearLayout) findViewById(R.id.tweetbar);
        TextView txtTw = (TextView) findViewById(R.id.tweet_detailed);
        background.setBackgroundColor(backgroundColor);
        tweetaction.setBackgroundColor(backgroundColor);
        answer_list.setBackgroundColor(backgroundColor);
        txtTw.setTextColor(fontColor);
        new ShowStatus(this).execute(tweetID);
    }
}