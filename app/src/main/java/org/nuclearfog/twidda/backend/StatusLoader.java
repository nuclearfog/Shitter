package org.nuclearfog.twidda.backend;

import android.content.Intent;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.helper.FilenameTools;
import org.nuclearfog.twidda.backend.helper.FilenameTools.FileType;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.window.MediaViewer;
import org.nuclearfog.twidda.window.TweetDetail;
import org.nuclearfog.twidda.window.UserProfile;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import twitter4j.TwitterException;

import static android.view.View.VISIBLE;
import static org.nuclearfog.twidda.fragment.TweetListFragment.RETURN_TWEET_CHANGED;
import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.window.MediaViewer.MediaType.ANGIF;
import static org.nuclearfog.twidda.window.MediaViewer.MediaType.IMAGE;
import static org.nuclearfog.twidda.window.MediaViewer.MediaType.VIDEO;
import static org.nuclearfog.twidda.window.TweetDetail.KEY_TWEET_ID;
import static org.nuclearfog.twidda.window.TweetDetail.KEY_TWEET_NAME;


public class StatusLoader extends AsyncTask<Long, Tweet, Void> {

    public enum Mode {
        LOAD,
        RETWEET,
        FAVORITE,
        DELETE
    }

    private final Mode mode;
    private boolean failure = false;

    private TwitterEngine mTwitter;
    private TwitterException err;
    private WeakReference<TweetDetail> ui;
    private SimpleDateFormat sdf;
    private NumberFormat formatter;
    private long homeId;
    private int font_color, highlight;
    private boolean toggleImg;


    public StatusLoader(@NonNull TweetDetail context, Mode mode) {
        mTwitter = TwitterEngine.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        sdf = settings.getDateFormatter();
        formatter = NumberFormat.getIntegerInstance();
        font_color = settings.getFontColor();
        highlight = settings.getHighlightColor();
        toggleImg = settings.getImageLoad();
        homeId = settings.getUserId();
        ui = new WeakReference<>(context);
        this.mode = mode;
    }


    @Override
    protected Void doInBackground(Long... data) {
        Tweet tweet;
        final long TWEETID = data[0];
        boolean updateStatus = false;
        DatabaseAdapter db = new DatabaseAdapter(ui.get());
        try {
            switch (mode) {
                case LOAD:
                    tweet = db.getStatus(TWEETID);
                    if (tweet != null) {
                        publishProgress(tweet);
                        updateStatus = true;
                    }
                    tweet = mTwitter.getStatus(TWEETID);
                    publishProgress(tweet);
                    if (updateStatus)
                        db.updateStatus(tweet);
                    break;

                case DELETE:
                    mTwitter.deleteTweet(TWEETID);
                    db.removeStatus(TWEETID);
                    break;

                case RETWEET:
                    tweet = mTwitter.retweet(TWEETID);
                    publishProgress(tweet);

                    if (!tweet.retweeted())
                        db.removeRetweet(TWEETID);
                    db.updateStatus(tweet);
                    break;

                case FAVORITE:
                    tweet = mTwitter.favorite(TWEETID);
                    publishProgress(tweet);

                    if (tweet.favored())
                        db.storeFavorite(tweet);
                    else
                        db.removeFavorite(TWEETID);
                    break;
            }
        } catch (TwitterException err) {
            this.err = err;
            int rCode = err.getErrorCode();
            if (rCode == 144 || rCode == 34 || rCode == 63)
                db.removeStatus(TWEETID);
            failure = true;
        } catch (Exception err) {
            if (err.getMessage() != null)
                Log.e("StatusLoader", err.getMessage());
            failure = true;
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(Tweet[] tweets) {
        if (ui.get() == null) return;

        TextView username = ui.get().findViewById(R.id.usernamedetail);
        TextView scrName = ui.get().findViewById(R.id.scrnamedetail);
        TextView replyName = ui.get().findViewById(R.id.answer_reference_detail);
        ImageView profile_img = ui.get().findViewById(R.id.profileimage_detail);
        Button retweetButton = ui.get().findViewById(R.id.tweet_retweet);
        Button favoriteButton = ui.get().findViewById(R.id.tweet_favorit);

        final Tweet tweet = tweets[0];
        if (mode == Mode.LOAD) {
            View tweet_header = ui.get().findViewById(R.id.tweet_head);
            if (tweet_header.getVisibility() != VISIBLE) {
                TextView tweetText = ui.get().findViewById(R.id.tweet_detailed);
                TextView tweetDate = ui.get().findViewById(R.id.timedetail);
                TextView tweet_api = ui.get().findViewById(R.id.used_api);
                View tweet_footer = ui.get().findViewById(R.id.tweet_foot);

                Spannable sTweet = Tagger.makeText(tweet.getTweet(), highlight, ui.get());
                tweetText.setMovementMethod(LinkMovementMethod.getInstance());
                tweetText.setText(sTweet);
                tweetText.setTextColor(font_color);
                tweetDate.setText(sdf.format(tweet.getTime()));
                tweetDate.setTextColor(font_color);
                tweet_api.setText(R.string.sent_from);
                tweet_api.append(tweet.getSource());
                tweet_api.setTextColor(font_color);

                if (tweet.getUser().isVerified()) {
                    username.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
                } else {
                    username.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
                if (tweet.getUser().isLocked()) {
                    scrName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
                } else {
                    scrName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
                if (tweet.hasMedia()) {
                    String firstLink = tweet.getMediaLinks()[0];
                    FileType ext = FilenameTools.getFileType(firstLink);
                    switch (ext) {
                        case IMAGE:
                            View imageButton = ui.get().findViewById(R.id.image_attach);
                            imageButton.setVisibility(VISIBLE);
                            imageButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent media = new Intent(ui.get(), MediaViewer.class);
                                    media.putExtra(KEY_MEDIA_LINK, tweet.getMediaLinks());
                                    media.putExtra(KEY_MEDIA_TYPE, IMAGE);
                                    ui.get().startActivity(media);
                                }
                            });
                            break;

                        case VIDEO:
                            View videoButton = ui.get().findViewById(R.id.video_attach);
                            videoButton.setVisibility(VISIBLE);
                            videoButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent media = new Intent(ui.get(), MediaViewer.class);
                                    media.putExtra(KEY_MEDIA_LINK, tweet.getMediaLinks());
                                    media.putExtra(KEY_MEDIA_TYPE, ANGIF);
                                    ui.get().startActivity(media);
                                }
                            });
                            break;

                        case STREAM:
                            videoButton = ui.get().findViewById(R.id.video_attach);
                            videoButton.setVisibility(VISIBLE);
                            videoButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent media = new Intent(ui.get(), MediaViewer.class);
                                    media.putExtra(KEY_MEDIA_LINK, tweet.getMediaLinks());
                                    media.putExtra(KEY_MEDIA_TYPE, VIDEO);
                                    ui.get().startActivity(media);
                                }
                            });
                            break;
                    }
                }
                profile_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profile = new Intent(ui.get(), UserProfile.class);
                        profile.putExtra(UserProfile.KEY_PROFILE_ID, tweet.getUser().getId());
                        profile.putExtra(UserProfile.KEY_PROFILE_NAME, tweet.getUser().getScreenname());
                        ui.get().startActivity(profile);
                    }
                });
                tweet_header.setVisibility(VISIBLE);
                tweet_footer.setVisibility(VISIBLE);
            }
        }

        username.setText(tweet.getUser().getUsername());
        username.setTextColor(font_color);
        scrName.setText(tweet.getUser().getScreenname());
        scrName.setTextColor(font_color);

        favoriteButton.setText(formatter.format(tweet.getFavorCount()));
        retweetButton.setText(formatter.format(tweet.getRetweetCount()));

        if (tweet.getReplyId() > 1) {
            String reply = ui.get().getString(R.string.answering);
            reply += tweet.getReplyName();
            replyName.setText(reply);
            replyName.setVisibility(VISIBLE);
            if (!replyName.isClickable()) {
                replyName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ui.get(), TweetDetail.class);
                        intent.putExtra(KEY_TWEET_ID, tweet.getReplyId());
                        intent.putExtra(KEY_TWEET_NAME, tweet.getReplyName());
                        ui.get().startActivity(intent);
                    }
                });
            }
        }
        if (toggleImg) {
            Picasso.get().load(tweet.getUser().getImageLink() + "_bigger").into(profile_img);
        }
        if (tweet.retweeted()) {
            retweetButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.retweet_enabled, 0, 0, 0);
        } else {
            retweetButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.retweet, 0, 0, 0);
        }
        if (tweet.favored()) {
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_enabled, 0, 0, 0);
        } else {
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite, 0, 0, 0);
        }
        if (tweet.getUser().getId() == homeId) {
            ui.get().setIsHome();
        }
    }


    @Override
    protected void onPostExecute(Void v) {
        if (ui.get() == null) return;

        if (!failure) {
            if (mode == Mode.DELETE) {
                Toast.makeText(ui.get(), R.string.tweet_removed, Toast.LENGTH_SHORT).show();
                ui.get().setResult(RETURN_TWEET_CHANGED);
                ui.get().finish();
            }
        } else {
            if (err != null) {
                int rCode = err.getErrorCode();
                if (rCode == 144 || rCode == 34 || rCode == 63)
                    ui.get().setResult(RETURN_TWEET_CHANGED);
                boolean killActivity = ErrorHandler.printError(ui.get(), err);
                if (killActivity)
                    ui.get().finish();
            }
        }
    }
}