package org.nuclearfog.twidda.window;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.TimelineAdapter;
import org.nuclearfog.twidda.backend.ImageLoad;
import org.nuclearfog.twidda.backend.StatusLoader;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.RUNNING;

/**
 * Detailed Tweet Activity
 *
 * @see StatusLoader
 * @see ImageLoad
 */
public class TweetDetail extends AppCompatActivity implements OnClickListener,
        OnItemClickListener, OnRefreshListener, OnTagClickListener {

    public static final int TWEET_REMOVED = 1;
    private static final int TWEET = 2;

    private RecyclerView answer_list;
    private StatusLoader mStat;
    private GlobalSettings settings;
    private SwipeRefreshLayout answerReload;
    private ConnectivityManager mConnect;
    private String username = "";
    private boolean isHome;
    private long userID = 0;
    private long tweetID = 0;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_tweet);

        b = getIntent().getExtras();
        if (b != null) {
            tweetID = b.getLong("tweetID");
            userID = b.getLong("userID");
            username = b.getString("username");
        }

        Toolbar tool = findViewById(R.id.tweet_toolbar);
        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        settings = GlobalSettings.getInstance(this);
        isHome = userID == settings.getUserId();
        mConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

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

        root.setBackgroundColor(settings.getBackgroundColor());

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
            TimelineAdapter answerAdapter = new TimelineAdapter(this);
            answerAdapter.toggleImage(settings.loadImages());
            answerAdapter.setColor(settings.getHighlightColor(), settings.getFontColor());
            answer_list.setAdapter(answerAdapter);

            answerReload.setRefreshing(true);
            mStat = new StatusLoader(this);
            mStat.execute(tweetID, StatusLoader.LOAD);
        }
    }


    @Override
    protected void onStop() {
        if (mStat != null && mStat.getStatus() == RUNNING)
            mStat.cancel(true);
        super.onStop();
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        super.onActivityResult(reqCode, returnCode, i);
        if (reqCode == TWEET && returnCode == TWEET_REMOVED) {
            mStat = null;
        }
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
                Builder deleteDialog = new Builder(this);
                deleteDialog.setMessage(R.string.delete_tweet);
                deleteDialog.setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mStat != null && mStat.getStatus() == RUNNING)
                            mStat.cancel(true);
                        mStat = new StatusLoader(TweetDetail.this);
                        mStat.execute(tweetID, StatusLoader.DELETE);
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

            case R.id.link_copy:
                String tweetLink = "https://twitter.com/" + username.substring(1) + "/status/" + tweetID;
                ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData linkClip = ClipData.newPlainText("tweet link", tweetLink);
                clip.setPrimaryClip(linkClip);
                Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        if (mStat != null && mStat.getStatus() == RUNNING)
            mStat.cancel(true);

        switch (v.getId()) {
            case R.id.rt_button_detail:
                if (mStat != null && mStat.getStatus() == RUNNING)
                    mStat.cancel(true);
                mStat = new StatusLoader(this);
                mStat.execute(tweetID, StatusLoader.RETWEET);
                break;

            case R.id.fav_button_detail:
                if (mStat != null && mStat.getStatus() == RUNNING)
                    mStat.cancel(true);
                mStat = new StatusLoader(this);
                mStat.execute(tweetID, StatusLoader.FAVORITE);
                break;

            case R.id.no_rt_detail:
                Intent retweet = new Intent(this, UserDetail.class);
                retweet.putExtra("tweetID", tweetID);
                retweet.putExtra("mode", 2);
                startActivity(retweet);
                break;

            case R.id.no_fav_detail:
                Intent favorit = new Intent(this, UserDetail.class);
                favorit.putExtra("tweetID", tweetID);
                favorit.putExtra("mode", 3);
                startActivity(favorit);
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
                startActivityForResult(tweet, TWEET);
                break;
        }
    }


    @Override
    public void onClick(String text) {
        Intent intent = new Intent(this, SearchPage.class);
        intent.putExtra("search", text);
        startActivity(intent);
    }


    @Override
    public void onItemClick(RecyclerView rv, int position) {
        TimelineAdapter timeLineAdapter = (TimelineAdapter) answer_list.getAdapter();
        if (timeLineAdapter != null && !answerReload.isRefreshing()) {
            Tweet tweet = timeLineAdapter.getData().get(position);
            Intent intent = new Intent(this, TweetDetail.class);
            intent.putExtra("tweetID", tweet.getId());
            intent.putExtra("userID", tweet.getUser().getId());
            intent.putExtra("username", tweet.getUser().getScreenname());
            startActivityForResult(intent, TWEET);
        }
    }


    @Override
    public void onRefresh() {
        mStat = new StatusLoader(this);
        mStat.execute(tweetID, StatusLoader.LOAD);
    }


    public void imageClick(String mediaLinks[]) {
        Intent image = new Intent(this, ImageDetail.class);
        image.putExtra("link", mediaLinks);
        startActivity(image);
    }
}