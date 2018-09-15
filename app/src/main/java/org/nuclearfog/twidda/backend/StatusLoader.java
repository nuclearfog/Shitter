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
import java.util.List;

import twitter4j.TwitterException;

public class StatusLoader extends AsyncTask<Long, Void, Long> {

    public static final long RETWEET = 1;
    public static final long FAVORITE = 2;
    public static final long DELETE = 3;
    private static final long ERROR = -1;
    private TwitterEngine mTwitter;
    private TimelineAdapter answerAdapter;
    private DatabaseAdapter database;
    private SimpleDateFormat sdf;
    private Tweet tweet;
    private int highlight;
    private boolean toggleImg;
    private String errorMessage = "Status load: ";
    private int returnCode = 0;

    private WeakReference<TweetDetail> ui;

    public StatusLoader(TweetDetail context) {
        mTwitter = TwitterEngine.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        sdf = settings.getDateFormatter();
        int font = settings.getFontColor();
        highlight = settings.getHighlightColor();
        toggleImg = settings.loadImages();
        ui = new WeakReference<>(context);
        RecyclerView replyList = context.findViewById(R.id.answer_list);
        answerAdapter = (TimelineAdapter) replyList.getAdapter();
        database = new DatabaseAdapter(context);
        if (answerAdapter == null) {
            answerAdapter = new TimelineAdapter(context);
            replyList.setAdapter(answerAdapter);
            answerAdapter.toggleImage(toggleImg);
            answerAdapter.setColor(highlight, font);
        }
    }


    /**
     * @param data [0] TWEET ID , [1] Mode
     */
    @Override
    protected Long doInBackground(Long... data) {
        long tweetID = data[0];
        try {
            if (data.length == 1) {
                tweet = database.getStatus(tweetID);
                List<Tweet> answers = database.getAnswers(tweetID);
                answerAdapter.setData(answers);
                if (tweet != null)
                    publishProgress();

                tweet = mTwitter.getStatus(tweetID);
                if (database.containStatus(tweetID))
                    database.updateStatus(tweet);

                if (answerAdapter.getItemCount() > 0) {
                    long sinceId = answerAdapter.getItemId(0);
                    answers = mTwitter.getAnswers(tweet.user.screenname, tweetID, sinceId);
                    answerAdapter.addNew(answers);
                } else {
                    answers = mTwitter.getAnswers(tweet.user.screenname, tweetID, tweetID);
                    answerAdapter.setData(answers);
                }
                publishProgress();

                if (answers.size() > 0 && database.containStatus(tweetID))
                    database.storeReplies(answers);

            } else if (data[1] == DELETE) {
                mTwitter.deleteTweet(tweetID);
                database.removeStatus(tweetID);
                return DELETE;

            } else if (data[1] == RETWEET) {
                Tweet tweet = mTwitter.retweet(tweetID);
                if (!tweet.retweeted)
                    database.removeStatus(tweet.retweetId);
                publishProgress();

            } else if (data[1] == FAVORITE) {
                Tweet tweet = mTwitter.favorite(tweetID);
                if (tweet.favorized) {
                    database.storeFavorite(tweetID);
                } else {
                    database.removeFavorite(tweetID);
                }
                publishProgress();
            }
        } catch (TwitterException e) {
            returnCode = e.getErrorCode();
            if (returnCode > 0) {
                if (returnCode == 144)
                    database.removeStatus(tweetID);
                else if (returnCode != 136)
                    errorMessage += e.getMessage();
            }
            return ERROR;
        } catch (Exception err) {
            Log.e("Status Loader", err.getMessage());
            return ERROR;
        }
        return 0L;
    }


    @Override
    protected void onProgressUpdate(Void... v) {
        final TweetDetail connect = ui.get();
        if (ui.get() == null) return;

        TextView tweetText = connect.findViewById(R.id.tweet_detailed);
        TextView username = connect.findViewById(R.id.usernamedetail);
        TextView scrName = connect.findViewById(R.id.scrnamedetail);
        TextView date = connect.findViewById(R.id.timedetail);
        TextView replyName = connect.findViewById(R.id.answer_reference_detail);
        TextView used_api = connect.findViewById(R.id.used_api);
        TextView txtRet = connect.findViewById(R.id.no_rt_detail);
        TextView txtFav = connect.findViewById(R.id.no_fav_detail);
        TextView txtAns = connect.findViewById(R.id.no_ans_detail);
        ImageView profile_img = connect.findViewById(R.id.profileimage_detail);
        Button retweetButton = connect.findViewById(R.id.rt_button_detail);
        Button favoriteButton = connect.findViewById(R.id.fav_button_detail);
        View mediaButton = connect.findViewById(R.id.image_attach);
        View tweet_verify = connect.findViewById(R.id.tweet_verify);

        tweetText.setText(highlight(tweet.tweet));
        username.setText(tweet.user.username);
        scrName.setText(tweet.user.screenname);
        date.setText(sdf.format(tweet.time));
        used_api.setText(R.string.sent_from);
        used_api.append(tweet.source);

        String ansStr = Integer.toString(answerAdapter.getItemCount());
        String favStr = Integer.toString(tweet.favorit);
        String rtStr = Integer.toString(tweet.retweet);
        txtFav.setText(favStr);
        txtRet.setText(rtStr);
        txtAns.setText(ansStr);

        if (tweet.replyID > 1) {
            String reply = connect.getString(R.string.answering);
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
                    connect.startActivity(intent);
                }
            });
        }
        if (tweet.media != null && tweet.media.length != 0) {
            mediaButton.setVisibility(View.VISIBLE);
            mediaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connect.onMediaClicked(tweet.media);
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
        answerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPostExecute(Long mode) {
        if (ui.get() != null) {
            if (mode == DELETE) {
                Toast.makeText(ui.get(), R.string.tweet_removed, Toast.LENGTH_SHORT).show();
                ui.get().finish();
            } else if (mode == ERROR) {
                if (returnCode > 0) {
                    if (returnCode == 420)
                        Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_LONG).show();
                    else if (returnCode == 144)
                        Toast.makeText(ui.get(), R.string.tweet_not_found, Toast.LENGTH_LONG).show();
                    else if (returnCode != 136)
                        Toast.makeText(ui.get(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            SwipeRefreshLayout ansReload = ui.get().findViewById(R.id.answer_reload);
            ansReload.setRefreshing(false);
        }
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