package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.textviewtool.LinkAndScrollMovement;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.TweetAction;
import org.nuclearfog.twidda.backend.TweetAction.Action;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.User;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.DialogBuilder;
import org.nuclearfog.twidda.backend.utils.DialogBuilder.OnDialogClick;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
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
import static org.nuclearfog.twidda.activity.TweetPopup.KEY_TWEETPOPUP_REPLYID;
import static org.nuclearfog.twidda.activity.TweetPopup.KEY_TWEETPOPUP_TEXT;
import static org.nuclearfog.twidda.activity.UserDetail.KEY_USERDETAIL_ID;
import static org.nuclearfog.twidda.activity.UserDetail.KEY_USERDETAIL_MODE;
import static org.nuclearfog.twidda.activity.UserDetail.USERLIST_RETWEETS;
import static org.nuclearfog.twidda.backend.utils.DialogBuilder.DialogType.DELETE_TWEET;
import static org.nuclearfog.twidda.fragment.TweetFragment.INTENT_TWEET_REMOVED_ID;
import static org.nuclearfog.twidda.fragment.TweetFragment.RETURN_TWEET_CHANGED;

/**
 * Tweet Activity for tweet and user informations
 */
public class TweetActivity extends AppCompatActivity implements OnClickListener,
        OnLongClickListener, OnTagClickListener, OnDialogClick {

    /**
     * ID of the tweet to open. required
     */
    public static final String KEY_TWEET_ID = "tweet_tweet_id";

    /**
     * screen name of the author. optional
     */
    public static final String KEY_TWEET_NAME = "tweet_author";

    /**
     * key for a tweet object
     */
    public static final String KEY_TWEET_DATA = "tweet_data";

    /**
     * regex pattern of a tweet URL
     */
    public static final Pattern LINK_PATTERN = Pattern.compile("https://twitter.com/\\w+/status/\\d+");

    private TextView tweet_api, tweetDate, tweetText, scrName, usrName, tweetLocName;
    private Button rtwButton, favButton, replyName, tweetLocGPS;
    private ImageView profile_img, mediaButton;
    private View header, footer, sensitive_media;
    private Dialog deleteDialog;

    private GlobalSettings settings;
    @Nullable
    private TweetAction statusAsync;
    @Nullable
    private Tweet tweet;
    private long tweetId;


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
        sensitive_media = findViewById(R.id.tweet_sensitive);

        tool.setTitle("");
        setSupportActionBar(tool);
        Bundle param = getIntent().getExtras();
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        if (param != null) {
            String username;
            Object data = param.getSerializable(KEY_TWEET_DATA);
            if (data instanceof Tweet) {
                tweet = (Tweet) data;
                username = tweet.getUser().getScreenname();
                tweetId = tweet.getId();
            } else {
                tweetId = param.getLong(KEY_TWEET_ID);
                username = param.getString(KEY_TWEET_NAME, "");
            }
            adapter.setupTweetPage(tweetId, username);
        }

        pager.setOffscreenPageLimit(1);
        pager.setAdapter(adapter);

        settings = GlobalSettings.getInstance(this);
        AppStyles.setTheme(settings, root);
        tweetText.setMovementMethod(LinkAndScrollMovement.getInstance());
        tweetText.setLinkTextColor(settings.getHighlightColor());
        deleteDialog = DialogBuilder.create(this, DELETE_TWEET, this);

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
        if (statusAsync == null) {
            if (tweet == null) {
                statusAsync = new TweetAction(this, tweetId);
                statusAsync.execute(Action.LD_DB);
            } else {
                statusAsync = new TweetAction(this, tweetId);
                statusAsync.execute(Action.LOAD);
                setTweet(tweet);
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
        AppStyles.setMenuIconColor(m, settings.getIconColor());
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        if (tweet != null && tweet.getUser().getId() == settings.getCurrentUserId())
            m.findItem(R.id.delete_tweet).setVisible(true);
        return super.onPrepareOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (tweet != null) {
            // Delete tweet option
            if (item.getItemId() == R.id.delete_tweet) {
                if (!deleteDialog.isShowing()) {
                    deleteDialog.show();
                }
            }
            // get tweet link
            else if (item.getItemId() == R.id.tweet_link) {
                String username = tweet.getUser().getScreenname().substring(1);
                String tweetLink = "https://twitter.com/" + username + "/status/" + tweet.getId();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetLink));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException err) {
                    Toast.makeText(this, R.string.error_connection_failed, LENGTH_SHORT).show();
                }
            }
            // copy tweet link to clipboard
            else if (item.getItemId() == R.id.link_copy) {
                String username = tweet.getUser().getScreenname().substring(1);
                String tweetLink = "https://twitter.com/" + username + "/status/" + tweet.getId();
                ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (clip != null) {
                    ClipData linkClip = ClipData.newPlainText("tweet link", tweetLink);
                    clip.setPrimaryClip(linkClip);
                    Toast.makeText(this, R.string.info_clipboard, LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.error_cant_copy_clipboard, LENGTH_SHORT).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        // answer to the tweet
        if (v.getId() == R.id.tweet_answer) {
            if (tweet != null) {
                String tweetPrefix = tweet.getUser().getScreenname() + " ";
                Intent tweetPopup = new Intent(this, TweetPopup.class);
                tweetPopup.putExtra(KEY_TWEETPOPUP_REPLYID, tweetId);
                tweetPopup.putExtra(KEY_TWEETPOPUP_TEXT, tweetPrefix);
                startActivity(tweetPopup);
            }
        }
        // show user retweeting this tweet
        else if (v.getId() == R.id.tweet_retweet) {
            if (tweet != null) {
                Intent userList = new Intent(this, UserDetail.class);
                userList.putExtra(KEY_USERDETAIL_ID, tweet.getId());
                userList.putExtra(KEY_USERDETAIL_MODE, USERLIST_RETWEETS);
                startActivity(userList);
            }
        }
        // open profile of the tweet author
        else if (v.getId() == R.id.profileimage_detail) {
            if (tweet != null) {
                Intent profile = new Intent(getApplicationContext(), UserProfile.class);
                profile.putExtra(UserProfile.KEY_PROFILE_DATA, tweet.getUser());
                startActivity(profile);
            }
        }
        // open replied tweet
        else if (v.getId() == R.id.answer_reference_detail) {
            if (tweet != null) {
                Intent answerIntent = new Intent(getApplicationContext(), TweetActivity.class);
                answerIntent.putExtra(KEY_TWEET_ID, tweet.getReplyId());
                answerIntent.putExtra(KEY_TWEET_NAME, tweet.getReplyName());
                startActivity(answerIntent);
            }
        }
        // open tweet location coordinates
        else if (v.getId() == R.id.tweet_location_coordinate) {
            if (tweet != null) {
                Intent locationIntent = new Intent(Intent.ACTION_VIEW);
                locationIntent.setData(Uri.parse("geo:" + tweet.getLocationCoordinates()));
                try {
                    startActivity(locationIntent);
                } catch (ActivityNotFoundException err) {
                    Toast.makeText(getApplicationContext(), R.string.error_no_card_app, LENGTH_SHORT).show();
                }
            }
        }
        // open tweet media
        else if (v.getId() == R.id.tweet_media_attach) {
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
        }
    }


    @Override
    public boolean onLongClick(View v) {
        if (tweet != null && (statusAsync == null || statusAsync.getStatus() != RUNNING)) {
            statusAsync = new TweetAction(this, tweet);
            // retweet this tweet
            if (v.getId() == R.id.tweet_retweet) {
                if (tweet.retweeted()) {
                    statusAsync.execute(Action.UNRETWEET);
                } else {
                    statusAsync.execute(Action.RETWEET);
                }
                Toast.makeText(this, R.string.info_loading, LENGTH_SHORT).show();
                return true;
            }
            // favorite the tweet
            else if (v.getId() == R.id.tweet_favorit) {
                if (tweet.favored()) {
                    statusAsync.execute(Action.UNFAVORITE);
                } else {
                    statusAsync.execute(Action.FAVORITE);
                }
                Toast.makeText(this, R.string.info_loading, LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }


    @Override
    public void onConfirm(DialogBuilder.DialogType type) {
        if (type == DELETE_TWEET) {
            statusAsync = new TweetAction(this, tweetId);
            statusAsync.execute(Action.DELETE);
        }
    }


    @Override
    public void onTagClick(String tag) {
        Intent intent = new Intent(this, SearchPage.class);
        intent.putExtra(KEY_SEARCH_QUERY, tag);
        startActivity(intent);
    }

    /**
     * called when a link is clicked
     *
     * @param tag link string
     */
    @Override
    public void onLinkClick(String tag) {
        String shortLink = tag;
        int cut = shortLink.indexOf('?');
        if (cut > 0) {
            shortLink = shortLink.substring(0, cut);
        }
        // check if the link if from a tweet
        if (LINK_PATTERN.matcher(shortLink).matches()) {
            String name = shortLink.substring(20, shortLink.indexOf('/', 20));
            long id = Long.parseLong(shortLink.substring(shortLink.lastIndexOf('/') + 1));
            Intent intent = new Intent(this, TweetActivity.class);
            intent.putExtra(KEY_TWEET_ID, id);
            intent.putExtra(KEY_TWEET_NAME, name);
            startActivity(intent);
        } else {
            // open link in a browser
            Uri link = Uri.parse(tag);
            Intent intent = new Intent(Intent.ACTION_VIEW, link);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException err) {
                Toast.makeText(this, R.string.error_connection_failed, LENGTH_SHORT).show();
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
        User author = tweet.getUser();
        invalidateOptionsMenu();

        NumberFormat buttonNumber = NumberFormat.getIntegerInstance();
        if (tweet.retweeted()) {
            AppStyles.setIconColor(rtwButton, Color.GREEN);
        } else {
            AppStyles.setIconColor(rtwButton, Color.WHITE);
        }
        if (tweet.favored()) {
            AppStyles.setIconColor(favButton, Color.YELLOW);
        } else {
            AppStyles.setIconColor(favButton, Color.WHITE);
        }
        if (author.isVerified()) {
            usrName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
        } else {
            usrName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        if (author.isLocked()) {
            scrName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
        } else {
            scrName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        usrName.setText(author.getUsername());
        scrName.setText(author.getScreenname());
        tweetDate.setText(SimpleDateFormat.getDateTimeInstance().format(tweet.getTime()));
        favButton.setText(buttonNumber.format(tweet.getFavorCount()));
        rtwButton.setText(buttonNumber.format(tweet.getRetweetCount()));
        tweet_api.setText(R.string.tweet_sent_from);
        tweet_api.append(" " + tweet.getSource());

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
        if (tweet.containsSensitiveMedia()) {
            sensitive_media.setVisibility(VISIBLE);
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
            String pbLink = author.getImageLink();
            if (!author.hasDefaultProfileImage())
                pbLink += settings.getImageSuffix();
            Picasso.get().load(pbLink).error(R.drawable.no_image).into(profile_img);
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
     * @param action  action type
     * @param tweetId ID of the tweet
     */
    public void onAction(Action action, long tweetId) {
        switch (action) {
            case RETWEET:
                Toast.makeText(this, R.string.info_tweet_retweeted, LENGTH_SHORT).show();
                break;

            case UNRETWEET:
                Toast.makeText(this, R.string.info_tweet_unretweeted, LENGTH_SHORT).show();
                break;

            case FAVORITE:
                Toast.makeText(this, R.string.info_tweet_favored, LENGTH_SHORT).show();
                break;

            case UNFAVORITE:
                Toast.makeText(this, R.string.info_tweet_unfavored, LENGTH_SHORT).show();
                break;

            case DELETE:
                Toast.makeText(this, R.string.info_tweet_removed, LENGTH_SHORT).show();
                Intent returnData = new Intent();
                returnData.putExtra(INTENT_TWEET_REMOVED_ID, tweetId);
                setResult(RETURN_TWEET_CHANGED, returnData);
                finish();
                break;
        }
    }

    /**
     * called when an error occurs
     *
     * @param error   Engine Exception7
     * @param tweetId ID of the tweet from which an error occurred
     */
    public void onError(@NonNull EngineException error, long tweetId) {
        ErrorHandler.handleFailure(this, error);
        if (error.resourceNotFound()) {
            Intent returnData = new Intent();
            returnData.putExtra(INTENT_TWEET_REMOVED_ID, tweetId);
            setResult(RETURN_TWEET_CHANGED, returnData);
            finish();
        } else if (tweet == null) {
            finish();
        }
    }
}