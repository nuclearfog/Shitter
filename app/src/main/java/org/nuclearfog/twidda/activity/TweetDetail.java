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
import org.nuclearfog.twidda.backend.TweetLoader;
import org.nuclearfog.twidda.backend.TweetLoader.Action;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.GONE;
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
import static org.nuclearfog.twidda.backend.engine.EngineException.ErrorType.NOT_AUTHORIZED;
import static org.nuclearfog.twidda.backend.engine.EngineException.ErrorType.RESOURCE_NOT_FOUND;
import static org.nuclearfog.twidda.fragment.TweetFragment.INTENT_TWEET_REMOVED_ID;
import static org.nuclearfog.twidda.fragment.TweetFragment.RETURN_TWEET_CHANGED;


public class TweetDetail extends AppCompatActivity implements OnClickListener,
        OnLongClickListener, OnTagClickListener {

    public static final String KEY_TWEET_ID = "tweetID";
    public static final String KEY_TWEET_NAME = "username";
    public static final Pattern linkPattern = Pattern.compile("https://twitter.com/\\w+/status/\\d+");

    private TextView tweet_api, tweetDate, tweetText, scrName, usrName, tweetLocName;
    private Button rtwButton, favButton, replyName, tweetLocGPS;
    private ImageView profile_img, mediaButton;
    private View header, footer;
    private FragmentAdapter adapter;

    private GlobalSettings settings;
    @Nullable
    private TweetLoader statusAsync;
    @Nullable
    private Tweet tweet;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_tweet);
        Toolbar tool = findViewById(R.id.tweet_toolbar);
        View root = findViewById(R.id.tweet_layout);
        Button ansButton = findViewById(R.id.tweet_answer);
        ViewPager pager = findViewById(R.id.tweet_pager);
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
        mediaButton = findViewById(R.id.tweet_media_attach);

        tool.setTitle("");
        setSupportActionBar(tool);

        adapter = new FragmentAdapter(getSupportFragmentManager());
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(adapter);

        settings = GlobalSettings.getInstance(this);
        FontTool.setViewFontAndColor(settings, root);
        tweetText.setMovementMethod(LinkAndScrollMovement.getInstance());
        tweetText.setLinkTextColor(settings.getHighlightColor());
        root.setBackgroundColor(settings.getBackgroundColor());

        replyName.setOnClickListener(this);
        ansButton.setOnClickListener(this);
        rtwButton.setOnClickListener(this);
        rtwButton.setOnLongClickListener(this);
        favButton.setOnLongClickListener(this);
        profile_img.setOnClickListener(this);
        tweetLocGPS.setOnClickListener(this);
        mediaButton.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Bundle param = getIntent().getExtras();
        if (statusAsync == null && param != null) {
            if (param.containsKey(KEY_TWEET_ID) && param.containsKey(KEY_TWEET_NAME)) {
                long tweetID = param.getLong(KEY_TWEET_ID);
                String username = param.getString(KEY_TWEET_NAME);
                adapter.setupTweetPage(tweetID, username);
                statusAsync = new TweetLoader(this, Action.LOAD);
                statusAsync.execute(tweetID);
            }
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
        if (statusAsync != null && tweet != null && statusAsync.getStatus() != RUNNING) {
            switch (item.getItemId()) {
                case R.id.delete_tweet:
                    Builder deleteDialog = new Builder(this, R.style.ConfirmDialog);
                    deleteDialog.setMessage(R.string.confirm_delete_tweet);
                    deleteDialog.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            statusAsync = new TweetLoader(TweetDetail.this, Action.DELETE);
                            statusAsync.execute(tweet.getId());
                        }
                    });
                    deleteDialog.setNegativeButton(R.string.confirm_no, null);
                    deleteDialog.show();
                    break;

                case R.id.tweet_link:
                    String username = tweet.getUser().getScreenname().substring(1);
                    String tweetLink = "https://twitter.com/" + username + "/status/" + tweet.getId();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetLink));
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivity(intent);
                    else
                        Toast.makeText(this, R.string.error_connection_failed, LENGTH_SHORT).show();
                    break;

                case R.id.link_copy:
                    username = tweet.getUser().getScreenname().substring(1);
                    tweetLink = "https://twitter.com/" + username + "/status/" + tweet.getId();
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
        if (statusAsync != null && tweet != null && statusAsync.getStatus() != RUNNING) {
            switch (v.getId()) {
                case R.id.tweet_answer:
                    Intent tweetPopup = new Intent(this, TweetPopup.class);
                    tweetPopup.putExtra(KEY_TWEETPOPUP_REPLYID, tweet.getId());
                    tweetPopup.putExtra(KEY_TWEETPOPUP_PREFIX, tweet.getUser().getScreenname());
                    startActivity(tweetPopup);
                    break;

                case R.id.tweet_retweet:
                    Intent userList = new Intent(this, UserDetail.class);
                    userList.putExtra(KEY_USERDETAIL_ID, tweet.getId());
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

                case R.id.tweet_media_attach:
                    if (tweet != null) {
                        Intent mediaIntent = new Intent(this, MediaViewer.class);
                        mediaIntent.putExtra(KEY_MEDIA_LINK, tweet.getMediaLinks());
                        switch (tweet.getMediaType()) {
                            case IMAGE:
                                mediaIntent.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_IMAGE);
                                break;

                            case VIDEO:
                                mediaIntent.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_VIDEO);
                                break;

                            case GIF:
                                mediaIntent.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_ANGIF);
                                break;
                        }
                        startActivity(mediaIntent);
                    }
                    break;
            }
        }
    }


    @Override
    public boolean onLongClick(View v) {
        if (statusAsync != null && tweet != null && statusAsync.getStatus() != RUNNING) {
            switch (v.getId()) {
                case R.id.tweet_retweet:
                    statusAsync = new TweetLoader(this, Action.RETWEET);
                    statusAsync.execute(tweet.getId());
                    Toast.makeText(this, R.string.info_loading, LENGTH_SHORT).show();
                    return true;

                case R.id.tweet_favorit:
                    statusAsync = new TweetLoader(this, Action.FAVORITE);
                    statusAsync.execute(tweet.getId());
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
    public void onLinkClick(String tag) {
        if (linkPattern.matcher(tag).matches()) {
            String name = tag.substring(20, tag.indexOf('/', 20));
            long id = Long.parseLong(tag.substring(tag.lastIndexOf('/') + 1));
            Intent intent = new Intent(this, TweetDetail.class);
            intent.putExtra(KEY_TWEET_ID, id);
            intent.putExtra(KEY_TWEET_NAME, name);
            startActivity(intent);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tag));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }


    /**
     * load tweet into UI
     *
     * @param tweet Tweet information
     */
    public void setTweet(Tweet tweet) {
        this.tweet = tweet;
        invalidateOptionsMenu();

        NumberFormat buttonNumber = NumberFormat.getIntegerInstance();
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
        switch (tweet.getMediaType()) {
            case IMAGE:
                mediaButton.setVisibility(VISIBLE);
                mediaButton.setImageResource(R.drawable.image);
                break;

            case VIDEO:
                mediaButton.setVisibility(VISIBLE);
                mediaButton.setImageResource(R.drawable.video);
                break;

            case GIF:
                mediaButton.setVisibility(VISIBLE);
                mediaButton.setImageResource(R.drawable.images);
                break;

            default:
                mediaButton.setVisibility(GONE);
                break;
        }
        if (settings.getImageLoad()) {
            String pbLink = tweet.getUser().getImageLink();
            if (!tweet.getUser().hasDefaultProfileImage())
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

    /**
     * called after a tweet action
     *
     * @param tweet  tweet information
     * @param action action type
     */
    public void onAction(Tweet tweet, Action action) {
        switch (action) {
            case RETWEET:
                if (tweet.retweeted())
                    Toast.makeText(this, R.string.info_tweet_retweeted, LENGTH_SHORT).show();
                else
                    Toast.makeText(this, R.string.info_tweet_unretweeted, LENGTH_SHORT).show();
                break;

            case FAVORITE:
                if (tweet.favored())
                    Toast.makeText(this, R.string.info_tweet_favored, LENGTH_SHORT).show();
                else
                    Toast.makeText(this, R.string.info_tweet_unfavored, LENGTH_SHORT).show();
                break;

            case DELETE:
                Toast.makeText(this, R.string.info_tweet_removed, LENGTH_SHORT).show();
                Intent returnData = new Intent();
                returnData.putExtra(INTENT_TWEET_REMOVED_ID, tweet.getId());
                setResult(RETURN_TWEET_CHANGED, returnData);
                finish();
                break;
        }
    }

    /**
     * called when an error occurs
     *
     * @param error Engine Exception
     */
    public void onError(EngineException error) {
        ErrorHandler.handleFailure(this, error);
        EngineException.ErrorType errorType = error.getErrorType();
        if (tweet != null) {
            if (errorType == RESOURCE_NOT_FOUND || errorType == NOT_AUTHORIZED) {
                Intent returnData = new Intent();
                returnData.putExtra(INTENT_TWEET_REMOVED_ID, tweet.getId());
                setResult(RETURN_TWEET_CHANGED, returnData);
                finish();
            }
        } else {
            finish();
        }
    }
}