package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.ErrorLog;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.window.SearchPage;
import org.nuclearfog.twidda.window.TweetDetail;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;

import twitter4j.TwitterException;

public class StatusLoader extends AsyncTask<Long, Void, Long> {

    private static final long ERROR     =-1;
    public static final long RETWEET    = 0;
    public static final long FAVORITE   = 1;
    public static final long DELETE     = 2;
    public static final long LOAD_TWEET = 3;
    public static final long LOAD_REPLY = 4;
    public static final long LOAD_DB    = 5;
    private static final long IGNORE    = 6;

    private TwitterEngine mTwitter;
    private TimelineRecycler answerAdapter;
    private DatabaseAdapter database;
    private ErrorLog errorLog;
    private SimpleDateFormat sdf;
    private String usernameStr, scrNameStr, tweetStr, dateString;
    private String repliedUsername, apiName, profile_pb;
    private String medialinks[];
    private boolean retweeted, favorited, toggleImg, verified;
    private long tweetReplyID, replyUserId;
    private int rtCount, favCount;
    private int highlight, font;
    private String errorMessage = "Status load: ";
    private int returnCode = 0;

    private WeakReference<TweetDetail> ui;

    public StatusLoader(Context context) {
        mTwitter = TwitterEngine.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        sdf = settings.getDateFormatter();
        font = settings.getFontColor();
        highlight = settings.getHighlightColor();
        toggleImg = settings.loadImages();
        ui = new WeakReference<>((TweetDetail)context);
        RecyclerView replyList = ui.get().findViewById(R.id.answer_list);
        answerAdapter = (TimelineRecycler) replyList.getAdapter();
        database = new DatabaseAdapter(context);
        errorLog = new ErrorLog(context);
        if(answerAdapter == null) {
            answerAdapter = new TimelineRecycler(ui.get());
            replyList.setAdapter(answerAdapter);
        }
    }


    /**
     * @param data [0] TWEET ID , [1] Mode
     */
    @Override
    protected Long doInBackground(Long... data) {
        long tweetID = data[0];
        final long MODE = data[1];
        try {
            Tweet tweet;
            if(MODE == DELETE) {
                mTwitter.deleteTweet(tweetID);
                database.removeStatus(tweetID);
                return DELETE;
            }

            if( MODE == LOAD_DB ) {
                tweet = database.getStatus(tweetID);
                List<Tweet> answers = database.getAnswers(tweetID);
                answerAdapter.setData(answers);
                answerAdapter.setColor(highlight, font);
                if(tweet == null)
                    return IGNORE; // NOT FOUND
            } else {
                tweet = mTwitter.getStatus(tweetID);
                if(database.containStatus(tweetID)) {
                    database.updateStatus(tweet);
                }
            }

            tweetReplyID = tweet.replyID;
            verified = tweet.user.isVerified;
            tweetStr = tweet.tweet;

            usernameStr = tweet.user.username;
            scrNameStr = tweet.user.screenname;
            apiName = formatString(tweet.source);
            rtCount = tweet.retweet;
            favCount = tweet.favorit;
            retweeted = tweet.retweeted;
            favorited = tweet.favorized;
            dateString = sdf.format(tweet.time);
            repliedUsername = tweet.replyName;
            replyUserId = tweet.replyUserId;
            profile_pb = tweet.user.profileImg + "_bigger";
            medialinks = tweet.media;

            if(MODE == RETWEET) {
                mTwitter.retweet(tweet);
                if(!retweeted) {
                    rtCount++;
                    retweeted = true;
                } else {
                    if(rtCount > 0)
                        rtCount--;
                    retweeted = false;
                    database.removeStatus(tweet.retweetId);
                }
            }
            else if(MODE == FAVORITE) {
                mTwitter.favorite(tweet);
                if(!favorited) {
                    favCount++;
                    favorited = true;
                    database.storeFavorite(tweet);
                } else {
                    if(favCount > 0)
                        favCount--;
                    favorited = false;
                    database.removeFavorite(tweetID);
                }
            }
            else if(MODE == LOAD_REPLY) {
                List<Tweet> answers;
                if(answerAdapter.getItemCount() > 0) {
                    long sinceId = answerAdapter.getItemId(0);
                    answers = mTwitter.getAnswers(scrNameStr, tweetID, sinceId);
                    answers.addAll(answerAdapter.getData());
                } else {
                    answers = mTwitter.getAnswers(scrNameStr, tweetID, tweetID);
                }
                answerAdapter.setData(answers);
                answerAdapter.toggleImage(toggleImg);
                answerAdapter.setColor(highlight, font);
                if(answers.size() > 0 && database.containStatus(tweetID))
                    database.storeReplies(answers);
            }
        }
        catch(TwitterException e) {
            returnCode = e.getErrorCode();
            if (returnCode > 0) {
                if (returnCode == 144)
                    database.removeStatus(tweetID);
                else if (returnCode != 136)
                    errorMessage += e.getMessage();
                return ERROR;
            }
        }
        catch(Exception err) {
            errorMessage += err.getMessage();
            errorLog.add(errorMessage);
            return ERROR;
        }
        return MODE;
    }

    @Override
    protected void onPostExecute(Long mode) {
        final TweetDetail connect = ui.get();
        if(connect == null)
            return;

        if(mode == LOAD_TWEET || mode == LOAD_DB) {
            TextView tweet = connect.findViewById(R.id.tweet_detailed);
            TextView username = connect.findViewById(R.id.usernamedetail);
            TextView scrName = connect.findViewById(R.id.scrnamedetail);
            TextView date = connect.findViewById(R.id.timedetail);
            TextView replyName = connect.findViewById(R.id.answer_reference_detail);
            TextView used_api = connect.findViewById(R.id.used_api);
            TextView txtRet = connect.findViewById(R.id.no_rt_detail);
            TextView txtFav = connect.findViewById(R.id.no_fav_detail);
            TextView txtAns = connect.findViewById(R.id.no_ans_detail);
            ImageView profile_img = connect.findViewById(R.id.profileimage_detail);

            tweet.setMovementMethod(LinkMovementMethod.getInstance());
            tweet.setText(highlight(tweetStr));
            username.setText(usernameStr);
            scrName.setText(scrNameStr);
            date.setText(dateString);
            used_api.setText(apiName);

            String ansStr = Integer.toString(answerAdapter.getItemCount());
            String favStr = Integer.toString(favCount);
            String rtStr = Integer.toString(rtCount);
            txtFav.setText(favStr);
            txtRet.setText(rtStr);
            txtAns.setText(ansStr);

            if(tweetReplyID > 1) {
                String reply = connect.getString(R.string.answering);
                reply += repliedUsername;
                replyName.setText(reply);
                replyName.setVisibility(View.VISIBLE);
                replyName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ui.get(),TweetDetail.class);
                        intent.putExtra("tweetID", tweetReplyID);
                        intent.putExtra("userID", replyUserId);
                        intent.putExtra("username", repliedUsername);
                        connect.startActivity(intent);
                    }
                });
            }
            if(verified) {
                View tweet_verify = connect.findViewById(R.id.tweet_verify);
                tweet_verify.setVisibility(View.VISIBLE);
            }
            if(toggleImg) {
                Picasso.get().load(profile_pb).into(profile_img);
            }
            if (medialinks != null && medialinks.length != 0) {
                View mediaButton = connect.findViewById(R.id.image_attach);
                mediaButton.setVisibility(View.VISIBLE);
                mediaButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connect.onMediaClicked(medialinks);
                    }
                });
            }
            Button retweetButton = connect.findViewById(R.id.rt_button_detail);
            Button favoriteButton = connect.findViewById(R.id.fav_button_detail);
            setIcons(favoriteButton, retweetButton);
        }
        else if(mode == RETWEET) {
            TextView txtRet = connect.findViewById(R.id.no_rt_detail);
            Button retweetButton = connect.findViewById(R.id.rt_button_detail);
            Button favoriteButton = connect.findViewById(R.id.fav_button_detail);
            setIcons(favoriteButton, retweetButton);
            String rtStr = Integer.toString(rtCount);
            txtRet.setText(rtStr);
            int textId;
            if(retweeted) {
                textId = R.string.retweeted;
            } else {
                textId = R.string.unretweet;
            }
            Toast.makeText(ui.get(), textId, Toast.LENGTH_SHORT).show();
        }
        else if(mode == FAVORITE) {
            Button retweetButton = connect.findViewById(R.id.rt_button_detail);
            Button favoriteButton = connect.findViewById(R.id.fav_button_detail);
            TextView txtFav = connect.findViewById(R.id.no_fav_detail);
            setIcons(favoriteButton, retweetButton);
            String favStr = Integer.toString(favCount);
            txtFav.setText(favStr);
            int textId;
            if(favorited)
                textId = R.string.favorited;
            else
                textId = R.string.unfavorited;
            Toast.makeText(ui.get(), textId, Toast.LENGTH_SHORT).show();
        }
        else if(mode == LOAD_REPLY) {
            SwipeRefreshLayout ansReload = connect.findViewById(R.id.answer_reload);
            ansReload.setRefreshing(false);
            String ansStr = Integer.toString(answerAdapter.getItemCount());
            TextView txtAns = connect.findViewById(R.id.no_ans_detail);
            answerAdapter.notifyDataSetChanged();
            txtAns.setText(ansStr);
        }
        else if(mode == DELETE) {
            Toast.makeText(ui.get(), R.string.tweet_removed, Toast.LENGTH_SHORT).show();
            ui.get().finish();
        }
        else if(mode == ERROR) {
            if (returnCode > 0) {
                if (returnCode == 420) {
                    Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_LONG).show();
                } else if (returnCode == 144) {
                    Toast.makeText(ui.get(), R.string.tweet_not_found, Toast.LENGTH_LONG).show();
                } else if (returnCode != 136) {
                    Toast.makeText(ui.get(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            SwipeRefreshLayout ansReload = connect.findViewById(R.id.answer_reload);
            if(ansReload.isRefreshing()) {
                ansReload.setRefreshing(false);
            }
        }
    }


    private String formatString(String text) {
        String prefix = ui.get().getString(R.string.sent_from);
        text = text.substring(text.indexOf('>') + 1);
        text = text.substring(0, text.indexOf('<'));
        return prefix + text;
    }


    private Spannable highlight(String tweet) {
        Spannable sTweet = new SpannableStringBuilder(tweet);
        int start = 0;
        boolean marked = false;
        for(int i = 0 ; i < tweet.length() ; i++) {
            char current = tweet.charAt(i);
            switch(current){
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
                    if(marked && start != i-1) {
                        sTweet = spanning(sTweet, start, i);
                    }
                    marked = false;
                    break;
            }
        }
        if(marked && start != tweet.length()-1) {
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
                if(search.startsWith("#"))
                    intent.putExtra("Addition", search);
                intent.putExtra("search", search);
                ui.get().startActivity(intent);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(highlight);
                ds.setUnderlineText(false);
            }
        },start,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sTweet;
    }


    private void setIcons(Button favoriteButton, Button retweetButton) {
        if(favorited)
            favoriteButton.setBackgroundResource(R.drawable.favorite_enabled);
        else
            favoriteButton.setBackgroundResource(R.drawable.favorite);
        if(retweeted)
            retweetButton.setBackgroundResource(R.drawable.retweet_enabled);
        else
            retweetButton.setBackgroundResource(R.drawable.retweet);
    }

    public interface OnMediaClick {
        void onMediaClicked(String mediaLinks[]);
    }
}