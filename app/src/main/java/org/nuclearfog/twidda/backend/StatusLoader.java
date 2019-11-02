package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.window.TweetDetail;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.fragment.TweetListFragment.RETURN_TWEET_CHANGED;


public class StatusLoader extends AsyncTask<Long, Tweet, Tweet> {

    public enum Mode {
        LOAD,
        RETWEET,
        FAVORITE,
        DELETE
    }

    private final Mode mode;
    private WeakReference<TweetDetail> ui;
    private TwitterEngine mTwitter;
    private TwitterException twException;
    private AppDatabase db;
    private boolean statusNotFound = false;


    public StatusLoader(@NonNull TweetDetail context, Mode mode) {
        mTwitter = TwitterEngine.getInstance(context);
        db = new AppDatabase(context);
        ui = new WeakReference<>(context);
        this.mode = mode;
    }


    @Override
    protected Tweet doInBackground(Long[] data) {
        Tweet tweet = null;
        long tweetId = data[0];
        boolean updateStatus = false;
        try {
            switch (mode) {
                case LOAD:
                    tweet = db.getStatus(tweetId);
                    if (tweet != null) {
                        publishProgress(tweet);
                        updateStatus = true;
                    }
                    tweet = mTwitter.getStatus(tweetId);
                    publishProgress(tweet);
                    if (updateStatus)
                        db.updateStatus(tweet);
                    break;

                case DELETE:
                    tweet = mTwitter.deleteTweet(tweetId);
                    db.removeStatus(tweetId);
                    break;

                case RETWEET:
                    tweet = mTwitter.retweet(tweetId);
                    publishProgress(tweet);

                    if (!tweet.retweeted())
                        db.removeRetweet(tweetId);
                    db.updateStatus(tweet);
                    break;

                case FAVORITE:
                    tweet = mTwitter.favorite(tweetId);
                    publishProgress(tweet);

                    if (tweet.favored())
                        db.storeFavorite(tweet);
                    else
                        db.removeFavorite(tweetId);
                    break;
            }
        } catch (TwitterException twException) {
            this.twException = twException;
            int rCode = twException.getErrorCode();
            if (rCode == 144 || rCode == 34 || rCode == 63) {
                db.removeStatus(tweetId);
                statusNotFound = true;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return tweet;
    }


    @Override
    protected void onProgressUpdate(Tweet[] tweets) {
        Tweet tweet = tweets[0];
        if (ui.get() != null && tweet != null) {
            ui.get().setTweet(tweet);
        }
    }


    @Override
    protected void onPostExecute(@Nullable Tweet tweet) {
        if (ui.get() != null) {
            if (tweet != null) {
                switch (mode) {
                    case RETWEET:
                        if (tweet.retweeted())
                            Toast.makeText(ui.get(), R.string.tweet_retweeted, LENGTH_SHORT).show();
                        else
                            Toast.makeText(ui.get(), R.string.tweet_unretweeted, LENGTH_SHORT).show();
                        break;

                    case FAVORITE:
                        if (tweet.favored())
                            Toast.makeText(ui.get(), R.string.tweet_favored, LENGTH_SHORT).show();
                        else
                            Toast.makeText(ui.get(), R.string.tweet_unfavored, LENGTH_SHORT).show();
                        break;

                    case DELETE:
                        Toast.makeText(ui.get(), R.string.tweet_removed, LENGTH_SHORT).show();
                        ui.get().setResult(RETURN_TWEET_CHANGED);
                        ui.get().finish();
                        break;
                }
            }
            if (twException != null) {
                boolean killActivity = ErrorHandler.printError(ui.get(), twException);
                if (killActivity) {
                    if (statusNotFound)
                        ui.get().setResult(RETURN_TWEET_CHANGED);
                    ui.get().finish();
                }
            }
        }
    }
}