package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
    private String errorMessage = "Status load: ";
    private boolean retweeted, favorited, toggleImg, verified;
    private long tweetReplyID, replyUserId;
    private int rtCount, favCount;
    private int highlight, font;

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
            int errCode = e.getErrorCode();
            if(errCode == 144) {
                database.removeStatus(tweetID); //TODO
                errorMessage = "Tweet nicht gefunden!\nID:"+tweetID;
            } else if(errCode == 420) {
                int retry = e.getRetryAfter(); //TODO
                errorMessage = "Rate limit erreicht!\n Weiter in "+retry+" Sekunden";
            } else {
                errorMessage += e.getMessage();
            }
            return ERROR;
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
                String reply = "antwort @"+repliedUsername;
                replyName.setText(reply);
                replyName.setVisibility(View.VISIBLE);
                replyName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ui.get(),TweetDetail.class);
                        Bundle bundle = new Bundle();

                        bundle.putLong("tweetID",tweetReplyID);
                        bundle.putLong("userID",replyUserId);
                        bundle.putString("username",repliedUsername);

                        intent.putExtras(bundle);
                        ui.get().startActivity(intent);
                    }
                });
            }
            if(verified) {
                View tweet_verify = connect.findViewById(R.id.tweet_verify);
                tweet_verify.setVisibility(View.VISIBLE);
            }
            if(toggleImg) {
                Picasso.with(ui.get()).load(profile_pb).into(profile_img);
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
            String toastMsg;
            TextView txtRet = connect.findViewById(R.id.no_rt_detail);
            Button retweetButton = connect.findViewById(R.id.rt_button_detail);
            Button favoriteButton = connect.findViewById(R.id.fav_button_detail);
            setIcons(favoriteButton, retweetButton);
            String rtStr = Integer.toString(rtCount);
            txtRet.setText(rtStr);
            if(retweeted) {
                toastMsg = "retweeted";
            } else {
                toastMsg = "retweet entfernt!";
            }
            Toast.makeText(ui.get(), toastMsg, Toast.LENGTH_SHORT).show();
        }
        else if(mode == FAVORITE) {
            String toastMsg;
            Button retweetButton = connect.findViewById(R.id.rt_button_detail);
            Button favoriteButton = connect.findViewById(R.id.fav_button_detail);
            TextView txtFav = connect.findViewById(R.id.no_fav_detail);
            setIcons(favoriteButton, retweetButton);
            String favStr = Integer.toString(favCount);
            txtFav.setText(favStr);
            if(favorited)
                toastMsg = "zu favoriten hinzugefügt!";
            else
                toastMsg = "aus favoriten entfernt!";
            Toast.makeText(ui.get(), toastMsg, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(ui.get(), "Tweet gelöscht", Toast.LENGTH_LONG).show();
            ui.get().finish();
        }
        else if(mode == ERROR) {
            Toast.makeText(ui.get(),errorMessage,Toast.LENGTH_LONG).show();
            SwipeRefreshLayout ansReload = connect.findViewById(R.id.answer_reload);
            if(ansReload.isRefreshing()) {
                ansReload.setRefreshing(false);
            }
        }
    }


    private String formatString(String input) {
        StringBuilder output = new StringBuilder("gesendet von: ");
        boolean openTag = false;
        for(int i = 0 ; i < input.length() ; i++){
            char current = input.charAt(i);
            if(current == '>' && !openTag){
                openTag = true;
            } else if(current == '<'){
                openTag = false;
            } else if(openTag) {
                output.append(current);
            }
        }
        return output.toString();
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
                Bundle bundle = new Bundle();
                if(search.startsWith("#"))
                    bundle.putString("Addition", search);
                bundle.putString("search", search);
                intent.putExtras(bundle);
                ui.get().startActivity(intent);
            }
            @Override
            public void updateDrawState(TextPaint ds){
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