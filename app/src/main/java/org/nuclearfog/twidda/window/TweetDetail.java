package org.nuclearfog.twidda.window;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.ImagePopup;
import org.nuclearfog.twidda.backend.StatusLoader;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter.OnItemClicked;

import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;

/**
 * Detailed Tweet Activity
 *
 * @see StatusLoader
 */
public class TweetDetail extends AppCompatActivity implements OnClickListener,
        OnItemClicked, OnRefreshListener {

    private RecyclerView answer_list;
    private StatusLoader mStat;
    private SwipeRefreshLayout answerReload;
    private ConnectivityManager mConnect;
    private String username = "";
    private boolean isHome;
    private long userID = 0;
    private long tweetID = 0;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        b = getIntent().getExtras();
        if (b != null) {
            tweetID = b.getLong("tweetID");
            userID = b.getLong("userID");
            username = b.getString("username");
        }
        setContentView(R.layout.page_tweet);

        Toolbar tool = findViewById(R.id.tweet_toolbar);
        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        int backgroundColor = settings.getBackgroundColor();
        int fontColor = settings.getFontColor();
        isHome = userID == settings.getUserId();
        mConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        TextView txtTw = findViewById(R.id.tweet_detailed);
        View root = findViewById(R.id.tweet_layout);
        View retweet = findViewById(R.id.rt_button_detail);
        View favorite = findViewById(R.id.fav_button_detail);
        View txtRt = findViewById(R.id.no_rt_detail);
        View txtFav = findViewById(R.id.no_fav_detail);
        View profile_img = findViewById(R.id.profileimage_detail);
        View answer = findViewById(R.id.answer_button);
        answerReload = findViewById(R.id.answer_reload);
        answer_list = findViewById(R.id.answer_list);
        answer_list.setLayoutManager(new LinearLayoutManager(this));

        root.setBackgroundColor(backgroundColor);
        txtTw.setTextColor(fontColor);

        favorite.setOnClickListener(this);
        retweet.setOnClickListener(this);
        answerReload.setOnRefreshListener(this);
        txtFav.setOnClickListener(this);
        txtRt.setOnClickListener(this);
        profile_img.setOnClickListener(this);
        answer.setOnClickListener(this);
    }


    protected void onStart() {
        super.onStart();
        if (mStat == null) {
            answerReload.setRefreshing(true);
            mStat = new StatusLoader(this);
            mStat.execute(tweetID, StatusLoader.LOAD);
        }
    }


    @Override
    protected void onStop() {
        if (mStat != null && mStat.getStatus() == RUNNING) {
            mStat.cancel(true);
            answerReload.setRefreshing(false);
        }
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.tweet, m);
        if (isHome)
            m.findItem(R.id.delete_tweet).setVisible(true);
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_tweet:
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
                deleteDialog.setMessage(R.string.delete_tweet);
                deleteDialog.setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new StatusLoader(TweetDetail.this).execute(tweetID, StatusLoader.DELETE);
                    }
                });
                deleteDialog.setNegativeButton(R.string.no_confirm, null);
                deleteDialog.show();
                break;

            case R.id.tweet_link:
                if (mConnect.getActiveNetworkInfo() != null && mConnect.getActiveNetworkInfo().isConnected()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String tweetLink = "https://twitter.com/" + username.substring(1) + "/status/" + tweetID;
                    intent.setData(Uri.parse(tweetLink));
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        if (mStat != null && mStat.getStatus() == FINISHED) {
            switch (v.getId()) {
                case R.id.rt_button_detail:
                    mStat = new StatusLoader(this);
                    mStat.execute(tweetID, StatusLoader.RETWEET);
                    break;

                case R.id.fav_button_detail:
                    mStat = new StatusLoader(this);
                    mStat.execute(tweetID, StatusLoader.FAVORITE);
                    break;

                case R.id.no_rt_detail:
                    Intent retweeter = new Intent(this, UserDetail.class);
                    retweeter.putExtra("tweetID", tweetID);
                    retweeter.putExtra("mode", 2);
                    startActivity(retweeter);
                    break;

                case R.id.no_fav_detail:
                    Intent favor = new Intent(this, UserDetail.class);
                    favor.putExtra("tweetID", tweetID);
                    favor.putExtra("mode", 3);
                    startActivity(favor);
                    break;

                case R.id.profileimage_detail:
                    Intent profile = new Intent(this, UserProfile.class);
                    profile.putExtra("userID", userID);
                    profile.putExtra("username", username);
                    startActivity(profile);
                    break;

                case R.id.answer_button:
                    Intent tweet = new Intent(this, TweetPopup.class);
                    tweet.putExtra("TweetID", tweetID);
                    tweet.putExtra("Addition", username);
                    startActivity(tweet);
                    break;
            }
        }
    }


    @Override
    public void onItemClick(ViewGroup parent, int position) {
        TimelineAdapter timeLineAdapter = (TimelineAdapter) answer_list.getAdapter();
        if (timeLineAdapter != null && !answerReload.isRefreshing()) {
            Tweet tweet = timeLineAdapter.getData().get(position);
            Intent intent = new Intent(this, TweetDetail.class);
            intent.putExtra("tweetID", tweet.tweetID);
            intent.putExtra("userID", tweet.user.userID);
            intent.putExtra("username", tweet.user.screenname);
            startActivity(intent);
        }
    }


    @Override
    public void onRefresh() {
        if (mStat != null && mStat.getStatus() == RUNNING)
            mStat.cancel(true);
        mStat = new StatusLoader(this);
        mStat.execute(tweetID, StatusLoader.LOAD);
    }


    public void onMediaClicked(String mediaLinks[]) {
        ImagePopup mediaContent = new ImagePopup(this);
        mediaContent.execute(mediaLinks);
    }
}