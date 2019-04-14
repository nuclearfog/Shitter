package org.nuclearfog.twidda.backend;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.TimelineAdapter;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.window.TweetDetail;
import org.nuclearfog.twidda.window.UserProfile;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import twitter4j.TwitterException;

import static android.view.View.VISIBLE;
import static org.nuclearfog.twidda.window.TweetDetail.STAT_CHANGED;

public class StatusLoader extends AsyncTask<Long, Void, Void> {

    public enum Mode {
        LOAD,
        ANS,
        RETWEET,
        FAVORITE,
        DELETE
    }
    private final Mode mode;
    private boolean failure = false;

    private TwitterEngine mTwitter;
    private TwitterException err;
    private WeakReference<TweetDetail> ui;
    private TimelineAdapter answerAdapter;
    private DatabaseAdapter database;
    private SimpleDateFormat sdf;
    private NumberFormat formatter;
    private List<Tweet> answers;
    private Tweet tweet;
    private int highlight, font_color;
    private boolean toggleImg, toggleAns;
    private long homeId;


    public StatusLoader(@NonNull TweetDetail context, Mode mode) {
        mTwitter = TwitterEngine.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        RecyclerView replyList = context.findViewById(R.id.answer_list);
        answerAdapter = (TimelineAdapter) replyList.getAdapter();
        database = new DatabaseAdapter(context);
        sdf = settings.getDateFormatter();
        formatter = NumberFormat.getIntegerInstance();
        font_color = settings.getFontColor();
        highlight = settings.getHighlightColor();
        toggleImg = settings.getImageLoad();
        toggleAns = settings.getAnswerLoad();
        homeId = settings.getUserId();
        ui = new WeakReference<>(context);
        answers = new ArrayList<>();
        this.mode = mode;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() == null) return;
        if (mode == Mode.LOAD && toggleAns) {
            SwipeRefreshLayout ansReload = ui.get().findViewById(R.id.answer_reload);
            ansReload.setRefreshing(true);
        }
    }


    @Override
    protected Void doInBackground(Long... data) {
        final long TWEETID = data[0];
        long sinceId = TWEETID;
        boolean updateStatus = false;
        try {
            switch (mode) {
                case LOAD:
                    tweet = database.getStatus(TWEETID);
                    if(tweet != null) {
                        answers = database.getAnswers(TWEETID);
                        publishProgress();
                        updateStatus = true;
                    }
                case ANS:
                    tweet = mTwitter.getStatus(TWEETID);
                    if (!updateStatus)
                        updateStatus = database.containStatus(TWEETID);

                    if (mode == Mode.ANS || toggleAns) {
                        if (answerAdapter.getItemCount() > 0)
                            sinceId = answerAdapter.getItemId(0);
                        answers = mTwitter.getAnswers(tweet.getUser().getScreenname(), TWEETID, sinceId);
                        if (updateStatus && !answers.isEmpty())
                            database.storeReplies(answers);
                        answers.addAll(answerAdapter.getData());
                    }
                    publishProgress();

                    if (updateStatus)
                        database.updateStatus(tweet);
                    break;

                case DELETE:
                    mTwitter.deleteTweet(TWEETID);
                    database.removeStatus(TWEETID);
                    break;

                case RETWEET:
                    tweet = mTwitter.retweet(TWEETID);
                    publishProgress();

                    if (!tweet.retweeted())
                        database.removeRetweet(TWEETID);
                    database.updateStatus(tweet);
                    break;

                case FAVORITE:
                    tweet = mTwitter.favorite(TWEETID);
                    publishProgress();

                    if (tweet.favored())
                        database.storeFavorite(TWEETID);
                    else
                        database.removeFavorite(TWEETID);
                    break;
            }
        } catch (TwitterException err) {
            this.err = err;
            int rCode = err.getErrorCode();
            if (rCode == 144 || rCode == 34 || rCode == 63)
                database.removeStatus(TWEETID);
            failure = true;
        } catch (Exception err) {
            if(err.getMessage() != null)
                Log.e("Status Loader", err.getMessage());
            failure = true;
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(Void... v) {
        if (ui.get() == null) return;

        if(!answers.isEmpty()) {
            SwipeRefreshLayout ansReload = ui.get().findViewById(R.id.answer_reload);
            ansReload.setRefreshing(false);
            answerAdapter.setData(answers);
            answerAdapter.notifyDataSetChanged();
        }

        TextView username = ui.get().findViewById(R.id.usernamedetail);
        TextView scrName = ui.get().findViewById(R.id.scrnamedetail);
        TextView replyName = ui.get().findViewById(R.id.answer_reference_detail);
        TextView txtRet = ui.get().findViewById(R.id.no_rt_detail);
        TextView txtFav = ui.get().findViewById(R.id.no_fav_detail);
        TextView txtAns = ui.get().findViewById(R.id.no_ans_detail);
        ImageView profile_img = ui.get().findViewById(R.id.profileimage_detail);
        ImageView retweetButton = ui.get().findViewById(R.id.rt_button_detail);
        ImageView favoriteButton = ui.get().findViewById(R.id.fav_button_detail);

        if(mode == Mode.LOAD) {
            View tweet_header = ui.get().findViewById(R.id.tweet_head);
            if (tweet_header.getVisibility() != VISIBLE) {
                TextView tweetText = ui.get().findViewById(R.id.tweet_detailed);
                TextView tweetDate = ui.get().findViewById(R.id.timedetail);
                TextView tweet_api = ui.get().findViewById(R.id.used_api);
                View tweet_verify = ui.get().findViewById(R.id.tweet_verify);
                View tweet_locked = ui.get().findViewById(R.id.tweet_locked);
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
                    tweet_verify.setVisibility(VISIBLE);
                }
                if (tweet.getUser().isLocked()) {
                    tweet_locked.setVisibility(VISIBLE);
                }
                if (tweet.getMediaLinks() != null && tweet.getMediaLinks().length > 0) {
                    View mediaButton = ui.get().findViewById(R.id.image_attach);
                    mediaButton.setVisibility(VISIBLE);
                    mediaButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ui.get().imageClick(tweet.getMediaLinks());
                        }
                    });
                }
                profile_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profile = new Intent(ui.get(), UserProfile.class);
                        profile.putExtra("userID", tweet.getUser().getId());
                        profile.putExtra("username", tweet.getUser().getScreenname());
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

        txtFav.setText(formatter.format(tweet.getFavorCount()));
        txtRet.setText(formatter.format(tweet.getRetweetCount()));
        txtAns.setText(formatter.format(answerAdapter.getItemCount()));

        if (tweet.getReplyId() > 1) {
            String reply = ui.get().getString(R.string.answering);
            reply += tweet.getReplyName();
            replyName.setText(reply);
            replyName.setVisibility(VISIBLE);
            if(!replyName.isClickable()) {
                replyName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ui.get(), TweetDetail.class);
                        intent.putExtra("tweetID", tweet.getReplyId());
                        intent.putExtra("username", tweet.getReplyName());
                        ui.get().startActivity(intent);
                    }
                });
            }
        }

        if (toggleImg) {
            Picasso.get().load(tweet.getUser().getImageLink() + "_bigger").into(profile_img);
        }
        if (tweet.retweeted()) {
            retweetButton.setImageResource(R.drawable.retweet_enabled);
        } else {
            retweetButton.setImageResource(R.drawable.retweet);
        }
        if (tweet.favored()) {
            favoriteButton.setImageResource(R.drawable.favorite_enabled);
        } else {
            favoriteButton.setImageResource(R.drawable.favorite);
        }
        if(tweet.getUser().getId() == homeId) {
            ui.get().enableDelete();
        }
    }


    @Override
    protected void onPostExecute(Void v) {
        if (ui.get() == null) return;

        if(answers.isEmpty()) {
            SwipeRefreshLayout ansReload = ui.get().findViewById(R.id.answer_reload);
            ansReload.setRefreshing(false);
        }

        if (!failure) {
            if (mode == Mode.DELETE) {
                Toast.makeText(ui.get(), R.string.tweet_removed, Toast.LENGTH_SHORT).show();
                ui.get().setResult(STAT_CHANGED);
                ui.get().finish();
            }
        } else {
            if (err != null) {
                boolean killActivity = ErrorHandler.printError(ui.get(), err);
                if (killActivity)
                    ui.get().finish();
            }
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() == null) return;

        SwipeRefreshLayout ansReload = ui.get().findViewById(R.id.answer_reload);
        ansReload.setRefreshing(false);
    }
}