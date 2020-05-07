package org.nuclearfog.twidda.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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
import org.nuclearfog.textviewtool.LinkAndScrollMovement;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.adapter.FragmentAdapter.AdapterType;
import org.nuclearfog.twidda.backend.TweetLoader;
import org.nuclearfog.twidda.backend.TweetLoader.Action;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.backend.helper.StringTools;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.activity.MediaViewer.MEDIAVIEWER_ANGIF;
import static org.nuclearfog.twidda.activity.MediaViewer.MEDIAVIEWER_IMAGE;
import static org.nuclearfog.twidda.activity.MediaViewer.MEDIAVIEWER_VIDEO;
import static org.nuclearfog.twidda.activity.SearchPage.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.activity.TweetPopup.KEY_TWEETPOPUP_PREFIX;
import static org.nuclearfog.twidda.activity.TweetPopup.KEY_TWEETPOPUP_REPLYID;
import static org.nuclearfog.twidda.activity.UserDetail.KEY_USERDETAIL_ID;
import static org.nuclearfog.twidda.activity.UserDetail.KEY_USERDETAIL_MODE;
import static org.nuclearfog.twidda.activity.UserDetail.USERLIST_RETWEETS;


public class TweetDetail extends AppCompatActivity implements OnClickListener,
        OnLongClickListener, OnTagClickListener {

    public static final String KEY_TWEET_ID = "tweetID";
    public static final String KEY_TWEET_NAME = "username";
    public static final Pattern linkPattern = Pattern.compile(".*/@?[\\w_]+/status/\\d{1,20}/?.*");

    private TweetLoader statusAsync;
    private GlobalSettings settings;

    private View header, footer, videoButton, imageButton;
    private TextView tweet_api, tweetDate, tweetText, scrName, usrName, tweetLocName;
    private Button rtwButton, favButton, replyName, tweetLocGPS;
    private ImageView profile_img;

    @Nullable
    private Tweet tweet;
    private String username;
    private long tweetID;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_tweet);
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
        tweetLocName = findViewById(R.id.tweet_location_name);
        tweetLocGPS = findViewById(R.id.tweet_location_coordinate);
        imageButton = findViewById(R.id.image_attach);
        videoButton = findViewById(R.id.video_attach);

        Bundle param = getIntent().getExtras();
        Uri link = getIntent().getData();
        settings = GlobalSettings.getInstance(this);
        if (param != null && param.containsKey(KEY_TWEET_ID) && param.containsKey(KEY_TWEET_NAME)) {
            tweetID = param.getLong(KEY_TWEET_ID);
            username = param.getString(KEY_TWEET_NAME);
        } else if (link != null) {
            getTweet(link);
        }

        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        FontTool.setViewFontAndColor(settings, root);
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.TWEET_PAGE, tweetID, username);
        tweetText.setMovementMethod(LinkAndScrollMovement.getInstance());
        tweetText.setLinkTextColor(settings.getHighlightColor());
        root.setBackgroundColor(settings.getBackgroundColor());
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(adapter);

        replyName.setOnClickListener(this);
        ansButton.setOnClickListener(this);
        rtwButton.setOnClickListener(this);
        rtwButton.setOnLongClickListener(this);
        favButton.setOnLongClickListener(this);
        profile_img.setOnClickListener(this);
        tweetLocGPS.setOnClickListener(this);
        videoButton.setOnClickListener(this);
        imageButton.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (statusAsync == null) {
            statusAsync = new TweetLoader(this, Action.LOAD);
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
                    Builder deleteDialog = new Builder(this, R.style.ConfirmDialog);
                    deleteDialog.setMessage(R.string.confirm_delete_tweet);
                    deleteDialog.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            statusAsync = new TweetLoader(TweetDetail.this, Action.DELETE);
                            statusAsync.execute(tweetID);
                        }
                    });
                    deleteDialog.setNegativeButton(R.string.confirm_no, null);
                    deleteDialog.show();
                    break;

                case R.id.tweet_link:
                    String tweetLink = "https://twitter.com/" + username.substring(1) + "/status/" + tweetID;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetLink));
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivity(intent);
                    else
                        Toast.makeText(this, R.string.error_connection, LENGTH_SHORT).show();
                    break;

                case R.id.link_copy:
                    tweetLink = "https://twitter.com/" + username.substring(1) + "/status/" + tweetID;
                    ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    if (clip != null) {
                        ClipData linkClip = ClipData.newPlainText("tweet link", tweetLink);
                        clip.setPrimaryClip(linkClip);
                        Toast.makeText(this, R.string.info_clipboard, LENGTH_SHORT).show();
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
                    userList.putExtra(KEY_USERDETAIL_ID, tweetID);
                    userList.putExtra(KEY_USERDETAIL_MODE, USERLIST_RETWEETS);
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
                        Intent answerIntent = new Intent(getApplicationContext(), TweetDetail.class);
                        answerIntent.putExtra(KEY_TWEET_ID, tweet.getReplyId());
                        answerIntent.putExtra(KEY_TWEET_NAME, tweet.getReplyName());
                        startActivity(answerIntent);
                    }
                    break;

                case R.id.tweet_location_coordinate:
                    if (tweet != null) {
                        Intent locationIntent = new Intent(Intent.ACTION_VIEW);
                        locationIntent.setData(Uri.parse("geo:" + tweet.getLocationCoordinates()));
                        if (locationIntent.resolveActivity(getPackageManager()) != null)
                            startActivity(locationIntent);
                        else
                            Toast.makeText(getApplicationContext(), R.string.error_no_card_app, LENGTH_SHORT).show();
                        break;
                    }

                case R.id.image_attach:
                    if (tweet != null) {
                        Intent mediaIntent = new Intent(getApplicationContext(), MediaViewer.class);
                        mediaIntent.putExtra(KEY_MEDIA_LINK, tweet.getMediaLinks());
                        mediaIntent.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_IMAGE);
                        startActivity(mediaIntent);
                    }
                    break;

                case R.id.video_attach:
                    if (tweet != null) {
                        String[] links = tweet.getMediaLinks();
                        StringTools.FileType ext = StringTools.getFileType(links[0]);
                        Intent mediaIntent = new Intent(getApplicationContext(), MediaViewer.class);
                        if (ext == StringTools.FileType.VIDEO)
                            mediaIntent.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_ANGIF);
                        else
                            mediaIntent.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_VIDEO);
                        mediaIntent.putExtra(KEY_MEDIA_LINK, links);
                        startActivity(mediaIntent);
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
                    statusAsync = new TweetLoader(this, Action.RETWEET);
                    statusAsync.execute(tweetID);
                    Toast.makeText(this, R.string.info_loading, LENGTH_SHORT).show();
                    return true;

                case R.id.tweet_favorit:
                    statusAsync = new TweetLoader(this, Action.FAVORITE);
                    statusAsync.execute(tweetID);
                    Toast.makeText(this, R.string.info_loading, LENGTH_SHORT).show();
                    return true;
            }
        }
        return false;
    }


    @Override
    public void onTagClick(String tag) {
        Intent intent = new Intent(this, SearchPage.class);
        intent.putExtra(KEY_SEARCH_QUERY, tag);
        startActivity(intent);
    }


    @Override
    public void onLinkClick(String link) {
        if (linkPattern.matcher(link).matches()) {
            Intent intent = new Intent(this, TweetDetail.class);
            intent.setData(Uri.parse(link));
            startActivity(intent);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivity(intent);
        }
    }


    public void setTweet(Tweet tweet) {
        this.tweet = tweet;
        invalidateOptionsMenu();

        TwitterUser author = tweet.getUser();
        NumberFormat buttonNumber = NumberFormat.getIntegerInstance();
        int rtwDraw = tweet.retweeted() ? R.drawable.retweet_enabled : R.drawable.retweet;
        int favDraw = tweet.favored() ? R.drawable.favorite_enabled : R.drawable.favorite;
        int verDraw = author.isVerified() ? R.drawable.verify : 0;
        int locDraw = author.isLocked() ? R.drawable.lock : 0;
        rtwButton.setCompoundDrawablesWithIntrinsicBounds(rtwDraw, 0, 0, 0);
        favButton.setCompoundDrawablesWithIntrinsicBounds(favDraw, 0, 0, 0);
        usrName.setCompoundDrawablesWithIntrinsicBounds(verDraw, 0, 0, 0);
        scrName.setCompoundDrawablesWithIntrinsicBounds(locDraw, 0, 0, 0);
        usrName.setText(author.getUsername());
        scrName.setText(author.getScreenname());
        tweetDate.setText(SimpleDateFormat.getDateTimeInstance().format(tweet.getTime()));
        favButton.setText(buttonNumber.format(tweet.getFavorCount()));
        rtwButton.setText(buttonNumber.format(tweet.getRetweetCount()));
        tweet_api.setText(R.string.tweet_sent_from);
        tweet_api.append(tweet.getSource());

        if (header.getVisibility() != VISIBLE) {
            header.setVisibility(VISIBLE);
            footer.setVisibility(VISIBLE);
        }
        if (!tweet.getTweet().trim().isEmpty()) {
            Spannable sTweet = Tagger.makeTextWithLinks(tweet.getTweet(), settings.getHighlightColor(), this);
            tweetText.setVisibility(VISIBLE);
            tweetText.setText(sTweet);
        }
        if (tweet.getReplyId() > 0) {
            replyName.setText(R.string.tweet_answering);
            replyName.append(tweet.getReplyName());
            replyName.setVisibility(VISIBLE);
        }
        if (tweet.hasMedia()) {
            String[] links = tweet.getMediaLinks();
            StringTools.FileType ext = StringTools.getFileType(links[0]);
            if (ext == StringTools.FileType.IMAGE)
                imageButton.setVisibility(VISIBLE);
            else
                videoButton.setVisibility(VISIBLE);
        }
        if (settings.getImageLoad()) {
            String pbLink = author.getImageLink();
            if (!author.hasDefaultProfileImage())
                pbLink += "_bigger";
            Picasso.get().load(pbLink).into(profile_img);
        }
        String placeName = tweet.getLocationName();
        if (placeName != null && !placeName.isEmpty()) {
            tweetLocName.setVisibility(VISIBLE);
            tweetLocName.setText(placeName);
        }
        String location = tweet.getLocationCoordinates();
        if (location != null && !location.isEmpty()) {
            tweetLocGPS.setVisibility(VISIBLE);
            tweetLocGPS.setText(location);
        }
    }


    public void finishIfEmpty() {
        if (tweet == null) {
            finish();
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
            Toast.makeText(this, R.string.error_open_link, LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}