package org.nuclearfog.twidda.window;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.adapter.FragmentAdapter.AdapterType;
import org.nuclearfog.twidda.backend.StatusLoader;
import org.nuclearfog.twidda.backend.StatusLoader.Mode;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.window.UserDetail.UserType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.AsyncTask.Status.RUNNING;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.window.SearchPage.KEY_SEARCH;
import static org.nuclearfog.twidda.window.TweetPopup.KEY_TWEETPOPUP_ADDITION;
import static org.nuclearfog.twidda.window.TweetPopup.KEY_TWEETPOPUP_REPLYID;
import static org.nuclearfog.twidda.window.UserDetail.KEY_USERLIST_ID;
import static org.nuclearfog.twidda.window.UserDetail.KEY_USERLIST_MODE;


public class TweetDetail extends AppCompatActivity implements OnClickListener, OnLongClickListener, OnTagClickListener {

    public static final String KEY_TWEET_ID = "tweetID";
    public static final String KEY_TWEET_NAME = "username";

    private ConnectivityManager mConnect;
    private GlobalSettings settings;
    private StatusLoader statusAsync;
    private String username;
    private boolean isHome;
    private long tweetID;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_tweet);

        Bundle param = getIntent().getExtras();
        Uri link = getIntent().getData();
        settings = GlobalSettings.getInstance(this);

        if (param != null && param.containsKey(KEY_TWEET_ID) && param.containsKey(KEY_TWEET_NAME)) {
            tweetID = param.getLong(KEY_TWEET_ID);
            username = param.getString(KEY_TWEET_NAME);
        } else if (link != null) {
            getTweet(link);
        } else if (BuildConfig.DEBUG) {
            throw new AssertionError();
        }

        View root = findViewById(R.id.tweet_layout);
        Button ansButton = findViewById(R.id.tweet_answer);
        Button rtwButton = findViewById(R.id.tweet_retweet);
        Button favButton = findViewById(R.id.tweet_favorit);
        TextView tweetTxt = findViewById(R.id.tweet_detailed);
        ViewPager pager = findViewById(R.id.tweet_pager);
        Toolbar tool = findViewById(R.id.tweet_toolbar);

        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.TWEET_PAGE, tweetID, username);
        mConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        tweetTxt.setMovementMethod(new ScrollingMovementMethod());
        tweetTxt.setLinkTextColor(settings.getHighlightColor());
        root.setBackgroundColor(settings.getBackgroundColor());
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(adapter);

        ansButton.setOnClickListener(this);
        rtwButton.setOnClickListener(this);
        favButton.setOnClickListener(this);
        rtwButton.setOnLongClickListener(this);
        favButton.setOnLongClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (statusAsync == null) {
            statusAsync = new StatusLoader(this, Mode.LOAD);
            statusAsync.execute(tweetID);
        }
    }


    @Override
    protected void onDestroy() {
        if (statusAsync != null && statusAsync.getStatus() == RUNNING)
            statusAsync.cancel(true);
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.tweet, m);
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        if (isHome)
            m.findItem(R.id.delete_tweet).setVisible(true);
        return super.onPrepareOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (statusAsync != null && statusAsync.getStatus() != RUNNING) {
            switch (item.getItemId()) {
                case R.id.delete_tweet:
                    Builder deleteDialog = new Builder(this);
                    deleteDialog.setMessage(R.string.delete_tweet);
                    deleteDialog.setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            statusAsync = new StatusLoader(TweetDetail.this, Mode.DELETE);
                            statusAsync.execute(tweetID);
                        }
                    });
                    deleteDialog.setNegativeButton(R.string.no_confirm, null);
                    deleteDialog.show();
                    break;

                case R.id.tweet_link:
                    if (mConnect.getActiveNetworkInfo() != null && mConnect.getActiveNetworkInfo().isConnected()) {
                        String tweetLink = "https://twitter.com/" + username.substring(1) + "/status/" + tweetID;
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(tweetLink));
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, R.string.connection_failed, LENGTH_SHORT).show();
                    }
                    break;

                case R.id.link_copy:
                    String tweetLink = "https://twitter.com/" + username.substring(1) + "/status/" + tweetID;
                    ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData linkClip = ClipData.newPlainText("tweet link", tweetLink);
                    clip.setPrimaryClip(linkClip);
                    Toast.makeText(this, R.string.copied_to_clipboard, LENGTH_SHORT).show();
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        if (statusAsync != null && statusAsync.getStatus() != RUNNING) {
            switch (v.getId()) {
                case R.id.tweet_answer:
                    Intent tweet = new Intent(this, TweetPopup.class);
                    tweet.putExtra(KEY_TWEETPOPUP_REPLYID, tweetID);
                    tweet.putExtra(KEY_TWEETPOPUP_ADDITION, username);
                    startActivity(tweet);
                    break;

                case R.id.tweet_retweet:
                    Intent userList = new Intent(this, UserDetail.class);
                    userList.putExtra(KEY_USERLIST_ID, tweetID);
                    userList.putExtra(KEY_USERLIST_MODE, UserType.RETWEETS);
                    startActivity(userList);
                    break;

                case R.id.tweet_favorit:
                    userList = new Intent(this, UserDetail.class);
                    userList.putExtra(KEY_USERLIST_ID, tweetID);
                    userList.putExtra(KEY_USERLIST_MODE, UserType.FAVORITS);
                    startActivity(userList);
                    break;
            }
        }
    }


    @Override
    public boolean onLongClick(View v) {
        if (statusAsync != null && statusAsync.getStatus() != RUNNING) {
            switch (v.getId()) {
                case R.id.tweet_retweet:
                    statusAsync = new StatusLoader(this, Mode.RETWEET);
                    statusAsync.execute(tweetID);
                    Toast.makeText(this, R.string.loading, LENGTH_SHORT).show();
                    return true;

                case R.id.tweet_favorit:
                    statusAsync = new StatusLoader(this, Mode.FAVORITE);
                    statusAsync.execute(tweetID);
                    Toast.makeText(this, R.string.loading, LENGTH_SHORT).show();
                    return true;
            }
        }
        return false;
    }


    @Override
    public void onClick(String text) {
        Intent intent = new Intent(this, SearchPage.class);
        intent.putExtra(KEY_SEARCH, text);
        startActivity(intent);
    }


    public void setIsHome() {
        isHome = true;
        invalidateOptionsMenu();
    }


    private void getTweet(@NonNull Uri link) {
        String path = link.getPath() == null ? "" : link.getPath();
        Pattern linkPattern = Pattern.compile("/@?[\\w_]+/status/\\d{1,20}");
        Matcher linkMatch = linkPattern.matcher(path);

        if (linkMatch.matches() && settings.getLogin()) {
            if (path.startsWith("/@"))
                path = path.substring(1);
            else
                path = '@' + path.substring(1);
            int end = path.indexOf('/');
            username = path.substring(0, end);
            path = path.substring(end + 8);
            tweetID = Long.parseLong(path);
        } else {
            Toast.makeText(this, R.string.failed_open_link, LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}