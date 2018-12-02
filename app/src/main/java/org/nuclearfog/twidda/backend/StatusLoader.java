package org.nuclearfog.twidda.backend;

import android.content.Intent;
import android.os.AsyncTask;
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

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import twitter4j.TwitterException;

public class StatusLoader extends AsyncTask<Long, Void, Long> {

    public static final long LOAD = 0;
    public static final long RETWEET = 1;
    public static final long FAVORITE = 2;
    public static final long DELETE = 3;
    private static final long ERROR = -1;

    private TwitterEngine mTwitter;
    private WeakReference<TweetDetail> ui;
    private TimelineAdapter answerAdapter;
    private DatabaseAdapter database;
    private SimpleDateFormat sdf;
    private List<Tweet> answers;
    private Tweet tweet;
    private int highlight, font_color;
    private boolean toggleImg;
    private String errMsg = "E Status load: ";
    private int returnCode = 0;


    public StatusLoader(TweetDetail context) {
        mTwitter = TwitterEngine.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        sdf = settings.getDateFormatter();
        font_color = settings.getFontColor();
        highlight = settings.getHighlightColor();
        toggleImg = settings.loadImages();
        ui = new WeakReference<>(context);
        answers = new ArrayList<>();
        RecyclerView replyList = context.findViewById(R.id.answer_list);
        answerAdapter = (TimelineAdapter) replyList.getAdapter();
        database = new DatabaseAdapter(context);
    }


    /**
     * @param data [0] TWEET ID , [1] Mode
     */
    @Override
    protected Long doInBackground(Long... data) {
        final long TWEETID = data[0];
        final long MODE = data[1];
        long sinceId = TWEETID;

        try {
            if (MODE == LOAD) {
                if (database.containStatus(TWEETID) && answerAdapter.getItemCount() == 0) {
                    tweet = database.getStatus(TWEETID);
                    answers = database.getAnswers(TWEETID);
                    publishProgress();
                }

                tweet = mTwitter.getStatus(TWEETID);
                if (answerAdapter.getItemCount() > 0)
                    sinceId = answerAdapter.getItemId(0);
                answers = mTwitter.getAnswers(tweet.getUser().getScreenname(), TWEETID, sinceId);
                publishProgress();

                if (database.containStatus(TWEETID)) {
                    database.updateStatus(tweet);
                    if (!answers.isEmpty())
                        database.storeReplies(answers);
                }

            } else if (MODE == DELETE) {
                mTwitter.deleteTweet(TWEETID);
                database.removeStatus(TWEETID);

            } else if (MODE == RETWEET) {
                tweet = mTwitter.retweet(TWEETID);
                if (!tweet.retweeted())
                    database.removeStatus(tweet.getMyRetweetId());
                publishProgress();

            } else if (MODE == FAVORITE) {
                tweet = mTwitter.favorite(TWEETID);
                if (tweet.favorized())
                    database.storeFavorite(TWEETID);
                else
                    database.removeFavorite(TWEETID);
                publishProgress();
            }

        } catch (TwitterException err) {
            returnCode = err.getErrorCode();
            if (returnCode == 144 || returnCode == 34)
                database.removeStatus(TWEETID);
            else
                errMsg += err.getMessage();
            return ERROR;

        } catch (Exception err) {
            err.printStackTrace();
            errMsg += err.getMessage();
            Log.e("Status Loader", errMsg);
            return ERROR;
        }
        return MODE;
    }


    @Override
    protected void onProgressUpdate(Void... v) {
        if (ui.get() == null) return;

        TextView tweetText = ui.get().findViewById(R.id.tweet_detailed);
        TextView username = ui.get().findViewById(R.id.usernamedetail);
        TextView scrName = ui.get().findViewById(R.id.scrnamedetail);
        TextView date = ui.get().findViewById(R.id.timedetail);
        TextView replyName = ui.get().findViewById(R.id.answer_reference_detail);
        TextView used_api = ui.get().findViewById(R.id.used_api);
        TextView txtRet = ui.get().findViewById(R.id.no_rt_detail);
        TextView txtFav = ui.get().findViewById(R.id.no_fav_detail);
        TextView txtAns = ui.get().findViewById(R.id.no_ans_detail);
        ImageView profile_img = ui.get().findViewById(R.id.profileimage_detail);
        ImageView retweetButton = ui.get().findViewById(R.id.rt_button_detail);
        ImageView favoriteButton = ui.get().findViewById(R.id.fav_button_detail);
        View mediaButton = ui.get().findViewById(R.id.image_attach);
        View tweet_verify = ui.get().findViewById(R.id.tweet_verify);

        Spannable sTweet = Tagger.makeText(tweet.getText(), highlight, ui.get());
        tweetText.setMovementMethod(LinkMovementMethod.getInstance());
        tweetText.setText(sTweet);
        tweetText.setTextColor(font_color);
        username.setText(tweet.getUser().getUsername());
        username.setTextColor(font_color);
        scrName.setText(tweet.getUser().getScreenname());
        scrName.setTextColor(font_color);
        date.setText(sdf.format(tweet.getTime()));
        date.setTextColor(font_color);
        used_api.setText(R.string.sent_from);
        used_api.append(tweet.getSource());
        used_api.setTextColor(font_color);

        String ansStr = Integer.toString(answerAdapter.getItemCount() + answers.size());
        String favStr = Integer.toString(tweet.getFavorCount());
        String rtStr = Integer.toString(tweet.getRetweetCount());
        txtFav.setText(favStr);
        txtRet.setText(rtStr);
        txtAns.setText(ansStr);

        if (tweet.getReplyId() > 1) {
            String reply = ui.get().getString(R.string.answering);
            reply += tweet.getReplyName();
            replyName.setText(reply);
            replyName.setVisibility(View.VISIBLE);
            replyName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ui.get(), TweetDetail.class);
                    intent.putExtra("tweetID", tweet.getReplyId());
                    intent.putExtra("userID", tweet.getReplyUserId());
                    intent.putExtra("username", tweet.getReplyName());
                    ui.get().startActivity(intent);
                }
            });
        }
        if (tweet.getMediaLinks() != null && tweet.getMediaLinks().length != 0) {
            mediaButton.setVisibility(View.VISIBLE);
            mediaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ui.get().imageClick(tweet.getMediaLinks());
                }
            });
        }
        if (tweet.getUser().isVerified()) {
            tweet_verify.setVisibility(View.VISIBLE);
        }
        if (toggleImg) {
            Picasso.get().load(tweet.getUser().getImageLink() + "_bigger").into(profile_img);
        }
        if (tweet.retweeted()) {
            retweetButton.setImageResource(R.drawable.retweet_enabled);
        } else {
            retweetButton.setImageResource(R.drawable.retweet);
        }
        if (tweet.favorized()) {
            favoriteButton.setImageResource(R.drawable.favorite_enabled);
        } else {
            favoriteButton.setImageResource(R.drawable.favorite);
        }
        answerAdapter.setData(answers);
        answerAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onPostExecute(Long mode) {
        if (ui.get() == null) return;

        SwipeRefreshLayout ansReload = ui.get().findViewById(R.id.answer_reload);
        ansReload.setRefreshing(false);

        if (mode == DELETE) {
            ui.get().deleteTweet();

        } else if (mode == ERROR) {

            switch (returnCode) {
                case 136:
                    break;

                case 420:
                    Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_LONG).show();
                    break;

                case 34:
                case 144:
                    Toast.makeText(ui.get(), R.string.tweet_not_found, Toast.LENGTH_LONG).show();
                    ui.get().setResult(TweetDetail.CHANGED);
                    ui.get().finish();
                    break;

                default:
                    Toast.makeText(ui.get(), errMsg, Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onCancelled(Long l) {
        if (ui.get() == null) return;

        SwipeRefreshLayout ansReload = ui.get().findViewById(R.id.answer_reload);
        ansReload.setRefreshing(false);
    }
}