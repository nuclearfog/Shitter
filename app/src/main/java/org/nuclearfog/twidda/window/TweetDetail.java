package org.nuclearfog.twidda.window;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
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
import org.nuclearfog.twidda.backend.GlobalSettings;
import org.nuclearfog.twidda.backend.ImagePopup;
import org.nuclearfog.twidda.backend.StatusLoader;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler.OnItemClicked;

import static android.content.DialogInterface.BUTTON_POSITIVE;

/**
 * Detailed Tweet Activity
 * @see StatusLoader
 */
public class TweetDetail extends AppCompatActivity implements OnClickListener,
        OnItemClicked, DialogInterface.OnClickListener, OnRefreshListener, StatusLoader.OnMediaClick {

    private RecyclerView answer_list;
    private StatusLoader mStat, mReply;
    private ImagePopup mediaContent;
    private SwipeRefreshLayout answerReload;
    private ConnectivityManager mConnect;
    private GlobalSettings settings;
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
        setContentView(R.layout.tweetpage);

        Toolbar tool = findViewById(R.id.tweet_toolbar);
        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        settings = GlobalSettings.getInstance(this);
        isHome = userID == settings.getUserId();
        mConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        View retweet = findViewById(R.id.rt_button_detail);
        View favorite = findViewById(R.id.fav_button_detail);
        View txtRt = findViewById(R.id.no_rt_detail);
        View txtFav = findViewById(R.id.no_fav_detail);
        View date = findViewById(R.id.timedetail);
        View profile_img = findViewById(R.id.profileimage_detail);
        View answer = findViewById(R.id.answer_button);
        answerReload = findViewById(R.id.answer_reload);
        answer_list = findViewById(R.id.answer_list);
        answer_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        favorite.setOnClickListener(this);
        retweet.setOnClickListener(this);
        answerReload.setOnRefreshListener(this);
        txtFav.setOnClickListener(this);
        txtRt.setOnClickListener(this);
        date.setOnClickListener(this);
        profile_img.setOnClickListener(this);
        answer.setOnClickListener(this);
        setContent();
    }

    @Override
    protected void onPause() {
        if (mStat != null && !mStat.isCancelled()) {
            mStat.cancel(true);
        }
        if (mReply != null && !mReply.isCancelled()) {
            mReply.cancel(true);
            answerReload.setRefreshing(false);
        }
        if (mediaContent != null && !mediaContent.isCancelled()) {
            mediaContent.cancel(true);
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.tweet, m); //TODO add more
        if (isHome) {
            m.findItem(R.id.delete_tweet).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_tweet:
                AlertDialog.Builder alerta = new AlertDialog.Builder(this);
                alerta.setMessage(R.string.delete_tweet);
                alerta.setPositiveButton(R.string.yes_confirm, this);
                alerta.setNegativeButton(R.string.no_confirm, this);
                alerta.show();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle bundle = new Bundle();
        StatusLoader mStat = new StatusLoader(this);
        switch(v.getId()) {
            case R.id.rt_button_detail:
                mStat.execute(tweetID, StatusLoader.RETWEET);
                break;

            case R.id.fav_button_detail:
                mStat.execute(tweetID, StatusLoader.FAVORITE);
                break;

            case R.id.no_rt_detail:
                intent = new Intent(this, UserDetail.class);
                bundle.putLong("tweetID",tweetID);
                bundle.putLong("mode",2L);
                intent.putExtras(bundle);
                startActivity(intent);
                break;

            case R.id.no_fav_detail:
                intent = new Intent(this, UserDetail.class);
                bundle.putLong("tweetID",tweetID);
                bundle.putLong("mode",3L);
                intent.putExtras(bundle);
                startActivity(intent);
                break;

            case R.id.timedetail:
                if (mConnect.getActiveNetworkInfo() != null && mConnect.getActiveNetworkInfo().isConnected()) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    String tweetLink = "https://twitter.com/" + username.substring(1) + "/status/" + tweetID;
                    intent.setData(Uri.parse(tweetLink));
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.profileimage_detail:
                Intent profile = new Intent(this, UserProfile.class);
                Bundle b = new Bundle();
                b.putLong("userID",userID);
                b.putString("username", username);
                profile.putExtras(b);
                startActivity(profile);
                break;

            case R.id.answer_button:
                Intent tweetPopup = new Intent(this, TweetPopup.class);
                Bundle ext = new Bundle();
                ext.putLong("TweetID", tweetID);
                ext.putString("Addition", username);
                tweetPopup.putExtras(ext);
                startActivity(tweetPopup);
                break;
        }
    }

    @Override
    public void onClick(DialogInterface d, int id) {
        if (id == BUTTON_POSITIVE)
            new StatusLoader(this).execute(tweetID, StatusLoader.DELETE);
    }

    @Override
    public void onItemClick(View view, ViewGroup parent, int position) {
        TimelineRecycler tlAdp = (TimelineRecycler) answer_list.getAdapter();
        Tweet tweet = tlAdp.getData().get(position);

        Intent intent = new Intent(this,TweetDetail.class);
        Bundle bundle = new Bundle();

        bundle.putLong("tweetID",tweet.tweetID);
        bundle.putLong("userID",tweet.user.userID);
        bundle.putString("username", tweet.user.screenname);

        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        mReply = new StatusLoader(this);
        mReply.execute(tweetID, StatusLoader.LOAD_REPLY);
    }

    @Override
    public void onMediaClicked(String mediaLinks[]) {
        mediaContent = new ImagePopup(this);
        mediaContent.execute(mediaLinks);
    }

    private void setContent() {
        int backgroundColor = settings.getBackgroundColor();
        int fontColor = settings.getFontColor();
        CollapsingToolbarLayout cLayout = findViewById(R.id.tweet_detail);
        View tweet = findViewById(R.id.tweetbar);
        TextView txtTw = findViewById(R.id.tweet_detailed);
        cLayout.setBackgroundColor(backgroundColor);
        tweet.setBackgroundColor(backgroundColor);
        answer_list.setBackgroundColor(backgroundColor);
        txtTw.setTextColor(fontColor);
        new StatusLoader(this).execute(tweetID, StatusLoader.LOAD_DB);
        mStat = new StatusLoader(this);
        mReply = new StatusLoader(this);
        mStat.execute(tweetID, StatusLoader.LOAD_TWEET);
        mReply.execute(tweetID, StatusLoader.LOAD_REPLY);
    }
}