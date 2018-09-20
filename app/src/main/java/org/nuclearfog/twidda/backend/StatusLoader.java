package org.nuclearfog.twidda.backend;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.window.SearchPage;
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
                answers = mTwitter.getAnswers(tweet.user.screenname, TWEETID, sinceId);
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
                if (!tweet.retweeted)
                    database.removeStatus(tweet.retweetId);
                publishProgress();

            } else if (MODE == FAVORITE) {
                tweet = mTwitter.favorite(TWEETID);
                if (tweet.favorized)
                    database.storeFavorite(TWEETID);
                else
                    database.removeFavorite(TWEETID);
                publishProgress();
            }

        } catch (TwitterException err) {
            returnCode = err.getErrorCode();
            if (returnCode == 144)
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
        Button retweetButton = ui.get().findViewById(R.id.rt_button_detail);
        Button favoriteButton = ui.get().findViewById(R.id.fav_button_detail);
        View mediaButton = ui.get().findViewById(R.id.image_attach);
        View tweet_verify = ui.get().findViewById(R.id.tweet_verify);

        tweetText.setText(highlight(tweet.tweet));
        tweetText.setTextColor(font_color);
        username.setText(tweet.user.username);
        username.setTextColor(font_color);
        scrName.setText(tweet.user.screenname);
        scrName.setTextColor(font_color);
        date.setText(sdf.format(tweet.time));
        date.setTextColor(font_color);
        used_api.setText(R.string.sent_from);
        used_api.append(tweet.source);
        used_api.setTextColor(font_color);

        String ansStr = Integer.toString(answerAdapter.getItemCount() + answers.size());
        String favStr = Integer.toString(tweet.favorit);
        String rtStr = Integer.toString(tweet.retweet);
        txtFav.setText(favStr);
        txtRet.setText(rtStr);
        txtAns.setText(ansStr);

        if (tweet.replyID > 1) {
            String reply = ui.get().getString(R.string.answering);
            reply += tweet.replyName;
            replyName.setText(reply);
            replyName.setVisibility(View.VISIBLE);
            replyName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ui.get(), TweetDetail.class);
                    intent.putExtra("tweetID", tweet.replyID);
                    intent.putExtra("userID", tweet.replyUserId);
                    intent.putExtra("username", tweet.replyName);
                    ui.get().startActivity(intent);
                }
            });
        }
        if (tweet.media != null && tweet.media.length != 0) {
            mediaButton.setVisibility(View.VISIBLE);
            mediaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ui.get().onMediaClicked(tweet.media);
                }
            });
        }
        if (tweet.user.isVerified) {
            tweet_verify.setVisibility(View.VISIBLE);
        }
        if (toggleImg) {
            Picasso.get().load(tweet.user.profileImg + "_bigger").into(profile_img);
        }
        if (tweet.retweeted) {
            retweetButton.setBackgroundResource(R.drawable.retweet_enabled);
        } else {
            retweetButton.setBackgroundResource(R.drawable.retweet);
        }
        if (tweet.favorized) {
            favoriteButton.setBackgroundResource(R.drawable.favorite_enabled);
        } else {
            favoriteButton.setBackgroundResource(R.drawable.favorite);
        }
        answerAdapter.setData(answers);
        answerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPostExecute(Long mode) {
        if (ui.get() == null) return;

        if (mode == DELETE) {
            Toast.makeText(ui.get(), R.string.tweet_removed, Toast.LENGTH_SHORT).show();
            ui.get().finish();
        } else if (mode == ERROR) {
            switch (returnCode) {
                case 136:
                    break;
                case 420:
                    Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_LONG).show();
                    break;
                case 144:
                    Toast.makeText(ui.get(), R.string.tweet_not_found, Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(ui.get(), errMsg, Toast.LENGTH_LONG).show();
            }
        }
        SwipeRefreshLayout ansReload = ui.get().findViewById(R.id.answer_reload);
        ansReload.setRefreshing(false);

    }


    private Spannable highlight(String tweet) {
        Spannable sTweet = new SpannableStringBuilder(tweet);
        int start = 0;
        boolean marked = false;
        for (int i = 0; i < tweet.length(); i++) {
            char current = tweet.charAt(i);
            switch (current) {
                case '@':
                case '#':
                    start = i;
                    marked = true;
                    break;
                case '\'':
                case '\"':
                case '\n':
                case ')':
                case '(':
                case ':':
                case ' ':
                case '.':
                case ',':
                case '!':
                case '?':
                case '-':
                    if (marked && start != i - 1)
                        sTweet = spanning(sTweet, start, i);
                    marked = false;
                    break;
            }
        }
        if (marked && start != tweet.length() - 1) {
            sTweet = spanning(sTweet, start, tweet.length());
        }
        return sTweet;
    }


    private Spannable spanning(Spannable sTweet, final int start, final int end) {
        sTweet.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                TextView tv = (TextView) widget;
                Spanned s = (Spanned) tv.getText();
                String search = s.subSequence(start, end).toString();
                Intent intent = new Intent(ui.get(), SearchPage.class);
                if (search.startsWith("#"))
                    intent.putExtra("Addition", search);
                intent.putExtra("search", search);
                ui.get().startActivity(intent);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(highlight);
                ds.setUnderlineText(false);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sTweet;
    }
}