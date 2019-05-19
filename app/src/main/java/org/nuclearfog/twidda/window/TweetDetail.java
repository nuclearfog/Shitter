package org.nuclearfog.twidda.window;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask.Status;
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


public class TweetDetail extends AppCompatActivity implements OnClickListener, OnLongClickListener, OnTagClickListener {

    public static final int STAT_CHANGED = 1;
    private static final int TWEET = 2;

    private ConnectivityManager mConnect;
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

        if (param != null && param.containsKey("tweetID") && param.containsKey("username")) {
            tweetID = param.getLong("tweetID");
            username = param.getString("username");
        } else if (link != null) {
            getTweet(link.getPath());
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

        GlobalSettings settings = GlobalSettings.getInstance(this);
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.TWEET_PAGE, tweetID, username);
        mConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        tweetTxt.setMovementMethod(ScrollingMovementMethod.getInstance());
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
    protected void onStop() {
        if (statusAsync != null && statusAsync.getStatus() == Status.RUNNING)
            statusAsync.cancel(true);
        super.onStop();
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        if (reqCode == TWEET && returnCode == STAT_CHANGED) {
            statusAsync = null;
        }
        super.onActivityResult(reqCode, returnCode, i);
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
        if (statusAsync != null && statusAsync.getStatus() != Status.RUNNING) {
            switch (item.getItemId()) {
                case R.id.delete_tweet:
                    Builder deleteDialog = new Builder(this);
                    deleteDialog.setMessage(R.string.delete_tweet);
                    deleteDialog.setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (statusAsync != null && statusAsync.getStatus() == Status.RUNNING)
                                statusAsync.cancel(true);
                            statusAsync = new StatusLoader(TweetDetail.this, Mode.DELETE);
                            statusAsync.execute(tweetID);
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
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        if (statusAsync != null && statusAsync.getStatus() != Status.RUNNING) {
            switch (v.getId()) {
                case R.id.tweet_answer:
                    Intent tweet = new Intent(this, TweetPopup.class);
                    tweet.putExtra("TweetID", tweetID);
                    tweet.putExtra("Addition", username);
                    startActivityForResult(tweet, TWEET);
                    break;

                case R.id.tweet_retweet:
                    Intent userList = new Intent(this, UserDetail.class);
                    userList.putExtra("ID", tweetID);
                    userList.putExtra("mode", UserType.RETWEETS);
                    startActivity(userList);
                    break;

                case R.id.tweet_favorit:
                    userList = new Intent(this, UserDetail.class);
                    userList.putExtra("ID", tweetID);
                    userList.putExtra("mode", UserType.FAVORITS);
                    startActivity(userList);
                    break;
            }
        }
    }


    @Override
    public boolean onLongClick(View v) {
        if (statusAsync != null && statusAsync.getStatus() != Status.RUNNING) {
            switch (v.getId()) {
                case R.id.tweet_retweet:
                    statusAsync = new StatusLoader(this, Mode.RETWEET);
                    statusAsync.execute(tweetID);
                    Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show();
                    return true;

                case R.id.tweet_favorit:
                    statusAsync = new StatusLoader(this, Mode.FAVORITE);
                    statusAsync.execute(tweetID);
                    Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show();
                    return true;
            }
        }
        return false;
    }


    @Override
    public void onClick(String text) {
        Intent intent = new Intent(this, SearchPage.class);
        intent.putExtra("search", text);
        startActivity(intent);
    }


    public void imageClick(String[] mediaLinks) {
        Intent image = new Intent(this, ImageDetail.class);
        image.putExtra("link", mediaLinks);
        image.putExtra("storable", true);
        startActivity(image);
    }


    public void enableDelete() {
        isHome = true;
        invalidateOptionsMenu();
    }


    private void getTweet(String link) {
        if (link != null) {
            Pattern linkPattern = Pattern.compile("/@?[\\w_]+/status/\\d{1,20}");
            Matcher linkMatch = linkPattern.matcher(link);
            if (linkMatch.matches()) {
                if (link.startsWith("/@"))
                    link = link.substring(1);
                else
                    link = '@' + link.substring(1);
                int end = link.indexOf('/');

                username = link.substring(0, end);
                link = link.substring(end + 8);
                tweetID = Long.parseLong(link);
            } else {
                Toast.makeText(this, R.string.tweet_not_found, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}