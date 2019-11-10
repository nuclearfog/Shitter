package org.nuclearfog.twidda.window;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.adapter.FragmentAdapter.AdapterType;
import org.nuclearfog.twidda.backend.StatusLoader;
import org.nuclearfog.twidda.backend.StatusLoader.Action;
import org.nuclearfog.twidda.backend.helper.FilenameTools;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.window.UserDetail.UserType;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.AsyncTask.Status.RUNNING;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.window.MediaViewer.MediaType.ANGIF;
import static org.nuclearfog.twidda.window.MediaViewer.MediaType.IMAGE;
import static org.nuclearfog.twidda.window.MediaViewer.MediaType.VIDEO;
import static org.nuclearfog.twidda.window.SearchPage.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.window.TweetPopup.KEY_TWEETPOPUP_PREFIX;
import static org.nuclearfog.twidda.window.TweetPopup.KEY_TWEETPOPUP_REPLYID;
import static org.nuclearfog.twidda.window.UserDetail.KEY_USERLIST_ID;
import static org.nuclearfog.twidda.window.UserDetail.KEY_USERLIST_MODE;


public class TweetDetail extends AppCompatActivity implements OnClickListener, OnTouchListener,
        OnLongClickListener, OnTagClickListener {

    public static final String KEY_TWEET_ID = "tweetID";
    public static final String KEY_TWEET_NAME = "username";

    private static final Pattern linkPattern = Pattern.compile("/@?[\\w_]+/status/\\d{1,20}/?.*");

    private View header, footer, videoButton, imageButton;
    private TextView tweet_api, tweetDate, tweetText, scrName, usrName, tweetLoc;
    private Button rtwButton, favButton, replyName;
    private ImageView profile_img;

    private ConnectivityManager mConnect;
    private GlobalSettings settings;
    private NumberFormat format;
    private StatusLoader statusAsync;

    @Nullable
    private Tweet tweet;
    private String username;
    private long tweetID;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_tweet);

        Bundle param = getIntent().getExtras();
        Uri link = getIntent().getData();
        settings = GlobalSettings.getInstance(this);
        format = settings.getNumberFormatter();

        if (param != null && param.containsKey(KEY_TWEET_ID) && param.containsKey(KEY_TWEET_NAME)) {
            tweetID = param.getLong(KEY_TWEET_ID);
            username = param.getString(KEY_TWEET_NAME);
        } else if (link != null) {
            getTweet(link);
        } else if (BuildConfig.DEBUG) {
            throw new AssertionError();
        }
        ViewPager pager = findViewById(R.id.tweet_pager);
        Toolbar tool = findViewById(R.id.tweet_toolbar);
        View root = findViewById(R.id.tweet_layout);
        Button ansButton = findViewById(R.id.tweet_answer);
        header = findViewById(R.id.tweet_head);
        footer = findViewById(R.id.tweet_foot);
        rtwButton = findViewById(R.id.tweet_retweet);
        favButton = findViewById(R.id.tweet_favorit);
        usrName = findViewById(R.id.usernamedetail);
        scrName = findViewById(R.id.scrnamedetail);
        profile_img = findViewById(R.id.profileimage_detail);
        replyName = findViewById(R.id.answer_reference_detail);
        tweetText = findViewById(R.id.tweet_detailed);
        tweetDate = findViewById(R.id.timedetail);
        tweet_api = findViewById(R.id.used_api);
        tweetLoc = findViewById(R.id.tweet_location);
        imageButton = findViewById(R.id.image_attach);
        videoButton = findViewById(R.id.video_attach);

        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.TWEET_PAGE, tweetID, username);
        mConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        tweetText.setMovementMethod(LinkMovementMethod.getInstance());
        tweetText.setLinkTextColor(settings.getHighlightColor());
        tweetText.setTextColor(settings.getFontColor());
        root.setBackgroundColor(settings.getBackgroundColor());
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(adapter);

        tweetText.setOnTouchListener(this);
        replyName.setOnClickListener(this);
        ansButton.setOnClickListener(this);
        rtwButton.setOnClickListener(this);
        favButton.setOnClickListener(this);
        rtwButton.setOnLongClickListener(this);
        favButton.setOnLongClickListener(this);
        profile_img.setOnClickListener(this);

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (statusAsync == null) {
            statusAsync = new StatusLoader(this, Action.LOAD);
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
        if (tweet != null && tweet.getUser().getId() == settings.getUserId())
            m.findItem(R.id.delete_tweet).setVisible(true);
        return super.onPrepareOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (statusAsync != null && statusAsync.getStatus() != RUNNING) {
            switch (item.getItemId()) {
                case R.id.delete_tweet:
                    Builder deleteDialog = new Builder(this);
                    deleteDialog.setMessage(R.string.delete_tweet);
                    deleteDialog.setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            statusAsync = new StatusLoader(TweetDetail.this, Action.DELETE);
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
                    if (clip != null) {
                        ClipData linkClip = ClipData.newPlainText("tweet link", tweetLink);
                        clip.setPrimaryClip(linkClip);
                        Toast.makeText(this, R.string.copied_to_clipboard, LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.error_cant_copy_clipboard, LENGTH_SHORT).show();
                    }
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
                    Intent tweetPopup = new Intent(this, TweetPopup.class);
                    tweetPopup.putExtra(KEY_TWEETPOPUP_REPLYID, tweetID);
                    tweetPopup.putExtra(KEY_TWEETPOPUP_PREFIX, username);
                    startActivity(tweetPopup);
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

                case R.id.profileimage_detail:
                    if (tweet != null) {
                        Intent profile = new Intent(getApplicationContext(), UserProfile.class);
                        profile.putExtra(UserProfile.KEY_PROFILE_ID, tweet.getUser().getId());
                        startActivity(profile);
                    }
                    break;

                case R.id.answer_reference_detail:
                    if (tweet != null) {
                        Intent intent = new Intent(getApplicationContext(), TweetDetail.class);
                        intent.putExtra(KEY_TWEET_ID, tweet.getReplyId());
                        intent.putExtra(KEY_TWEET_NAME, tweet.getReplyName());
                        startActivity(intent);
                    }
                    break;
            }
        }
    }


    @Override
    public boolean onLongClick(View v) {
        if (statusAsync != null && statusAsync.getStatus() != RUNNING) {
            switch (v.getId()) {
                case R.id.tweet_retweet:
                    statusAsync = new StatusLoader(this, Action.RETWEET);
                    statusAsync.execute(tweetID);
                    Toast.makeText(this, R.string.loading, LENGTH_SHORT).show();
                    return true;

                case R.id.tweet_favorit:
                    statusAsync = new StatusLoader(this, Action.FAVORITE);
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
        intent.putExtra(KEY_SEARCH_QUERY, text);
        startActivity(intent);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case ACTION_DOWN:
                v.getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case ACTION_UP:
                v.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return v.performClick();
    }


    public void setTweet(final Tweet tweet) {
        this.tweet = tweet;
        invalidateOptionsMenu();

        int rtwDraw = tweet.retweeted() ? R.drawable.retweet_enabled : R.drawable.retweet;
        int favDraw = tweet.favored() ? R.drawable.favorite_enabled : R.drawable.favorite;
        int verDraw = tweet.getUser().isVerified() ? R.drawable.verify : 0;
        int locDraw = tweet.getUser().isLocked() ? R.drawable.lock : 0;
        rtwButton.setCompoundDrawablesWithIntrinsicBounds(rtwDraw, 0, 0, 0);
        favButton.setCompoundDrawablesWithIntrinsicBounds(favDraw, 0, 0, 0);
        usrName.setCompoundDrawablesWithIntrinsicBounds(verDraw, 0, 0, 0);
        scrName.setCompoundDrawablesWithIntrinsicBounds(locDraw, 0, 0, 0);
        usrName.setText(tweet.getUser().getUsername());
        scrName.setText(tweet.getUser().getScreenname());
        usrName.setTextColor(settings.getFontColor());
        scrName.setTextColor(settings.getFontColor());
        tweetDate.setText(SimpleDateFormat.getDateTimeInstance().format(tweet.getTime()));
        tweetDate.setTextColor(settings.getFontColor());
        tweet_api.setTextColor(settings.getFontColor());
        favButton.setText(format.format(tweet.getFavorCount()));
        rtwButton.setText(format.format(tweet.getRetweetCount()));
        tweet_api.setText(R.string.sent_from);
        tweet_api.append(tweet.getSource());
        header.setVisibility(VISIBLE);
        footer.setVisibility(VISIBLE);

        if (!tweet.getTweet().trim().isEmpty()) {
            Spannable sTweet = Tagger.makeText(tweet.getTweet(), settings.getHighlightColor(), this);
            tweetText.setVisibility(VISIBLE);
            tweetText.setText(sTweet);
        }
        if (tweet.getReplyId() > 1) {
            replyName.setText(R.string.answering);
            replyName.append(tweet.getReplyName());
            replyName.setVisibility(VISIBLE);
        }
        if (tweet.hasMedia()) {
            String[] links = tweet.getMediaLinks();
            FilenameTools.FileType ext = FilenameTools.getFileType(links[0]);
            switch (ext) {
                case IMAGE:
                    imageButton.setVisibility(VISIBLE);
                    imageButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent media = new Intent(getApplicationContext(), MediaViewer.class);
                            media.putExtra(KEY_MEDIA_LINK, tweet.getMediaLinks());
                            media.putExtra(KEY_MEDIA_TYPE, IMAGE);
                            startActivity(media);
                        }
                    });
                    break;

                case VIDEO:
                    videoButton.setVisibility(VISIBLE);
                    videoButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent media = new Intent(getApplicationContext(), MediaViewer.class);
                            media.putExtra(KEY_MEDIA_LINK, tweet.getMediaLinks());
                            media.putExtra(KEY_MEDIA_TYPE, ANGIF);
                            startActivity(media);
                        }
                    });
                    break;

                case STREAM:
                    videoButton.setVisibility(VISIBLE);
                    videoButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent media = new Intent(getApplicationContext(), MediaViewer.class);
                            media.putExtra(KEY_MEDIA_LINK, tweet.getMediaLinks());
                            media.putExtra(KEY_MEDIA_TYPE, VIDEO);
                            startActivity(media);
                        }
                    });
                    break;
            }
        }
        if (settings.getImageLoad())
            Picasso.get().load(tweet.getUser().getImageLink() + "_bigger").into(profile_img);

        SpannableStringBuilder locationText = new SpannableStringBuilder("");
        if (!tweet.getLocationName().isEmpty()) {
            locationText.append(tweet.getLocationName());
            tweetLoc.setText(locationText);
            tweetLoc.setVisibility(VISIBLE);
        }
        if (!tweet.getLocationCoordinates().isEmpty()) {
            final int start = locationText.length();
            locationText.append(tweet.getLocationCoordinates());
            final int end = locationText.length() - 1;
            locationText.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent locationIntent = new Intent(Intent.ACTION_VIEW);
                    locationIntent.setData(Uri.parse("geo:" + tweet.getLocationCoordinates()));
                }
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(settings.getHighlightColor());
                    ds.setUnderlineText(false);
                }
            }, start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
            tweetLoc.setText(locationText);
            tweetLoc.setVisibility(VISIBLE);
        }
    }


    private void getTweet(@NonNull Uri link) {
        String path = link.getPath() == null ? "" : link.getPath();
        Matcher linkMatch = linkPattern.matcher(path);

        if (linkMatch.matches() && settings.getLogin()) {
            if (path.startsWith("/@"))
                path = path.substring(1);
            else
                path = '@' + path.substring(1);
            int end = path.indexOf('/');
            username = path.substring(0, end);
            path = path.substring(end + 8);
            end = path.indexOf('/');
            if (end > 0)
                path = path.substring(0, end);
            tweetID = Long.parseLong(path);
        } else {
            Toast.makeText(this, R.string.failed_open_link, LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}