package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;

import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.window.ColorPreferences;
import org.nuclearfog.twidda.window.TweetDetail;

public class ShowStatus extends AsyncTask<Long, Void, Long> {

    private static final long ERROR = -1;
    public static final long RETWEET = 0;
    public static final long FAVORITE = 1;
    public static final long DELETE = 2;
    public static final long LOAD_TWEET = 3;

    private Context c;
    private ListView replyList;
    private TextView  username,scrName,replyName,tweet;
    private TextView used_api,txtAns,txtRet,txtFav,date;
    private Button retweetButton,favoriteButton;
    private ImageView profile_img,tweet_img,tweet_verify;
    private List<twitter4j.Status> answers;
    private String usernameStr, scrNameStr, tweetStr, dateString;
    private String ansStr, rtStr, favStr, repliedUsername, apiName;
    private TwitterEngine mTwitter;
    private boolean retweeted, favorited, toggleImg, verified;
    private int rt, fav, ansNo = 0;
    private int highlight;
    private long userReply, tweetReplyID;
    private Bitmap profile_btm, tweet_btm;

    public ShowStatus(Context c) {
        mTwitter = TwitterEngine.getInstance(c);
        answers = new ArrayList<>();
        SharedPreferences settings = c.getSharedPreferences("settings", 0);
        toggleImg = settings.getBoolean("image_load", false);
        highlight = ColorPreferences.getInstance(c).getColor(ColorPreferences.HIGHLIGHTING);
        this.c = c;
    }

    @Override
    protected void onPreExecute() {
        replyList = (ListView) ((TweetDetail)c).findViewById(R.id.answer_list);
        tweet = (TextView) ((TweetDetail)c).findViewById(R.id.tweet_detailed);
        username = (TextView) ((TweetDetail)c).findViewById(R.id.usernamedetail);
        scrName = (TextView) ((TweetDetail)c).findViewById(R.id.scrnamedetail);
        date = (TextView) ((TweetDetail)c).findViewById(R.id.timedetail);
        replyName = (TextView) ((TweetDetail)c).findViewById(R.id.answer_reference_detail);
        txtAns = (TextView) ((TweetDetail)c).findViewById(R.id.no_ans_detail);
        txtRet = (TextView) ((TweetDetail)c).findViewById(R.id.no_rt_detail);
        txtFav = (TextView) ((TweetDetail)c).findViewById(R.id.no_fav_detail);
        used_api = (TextView) ((TweetDetail)c).findViewById(R.id.used_api);

        profile_img = (ImageView) ((TweetDetail)c).findViewById(R.id.profileimage_detail);
        tweet_img   = (ImageView) ((TweetDetail)c).findViewById(R.id.tweet_image);
        tweet_verify =(ImageView)((TweetDetail)c).findViewById(R.id.tweet_verify);

        retweetButton = (Button) ((TweetDetail)c).findViewById(R.id.rt_button_detail);
        favoriteButton = (Button) ((TweetDetail)c).findViewById(R.id.fav_button_detail);
    }

    /**
     * @param data [0] TWEET ID , [1] Mode
     * @returns false if Tweet is already loaded.
     */
    @Override
    protected Long doInBackground(Long... data) {
        long tweetID = data[0];
        long mode = data[1];
        try {
            twitter4j.Status currentTweet = mTwitter.getStatus(tweetID);
            rt = currentTweet.getRetweetCount();
            fav = currentTweet.getFavoriteCount();
            userReply = currentTweet.getInReplyToUserId();
            tweetReplyID = currentTweet.getInReplyToStatusId();
            verified = currentTweet.getUser().isVerified();
            retweeted = currentTweet.isRetweetedByMe();
            favorited = currentTweet.isFavorited();
            if(mode == LOAD_TWEET) {
                tweetStr = currentTweet.getText();
                usernameStr = currentTweet.getUser().getName();
                scrNameStr = '@'+currentTweet.getUser().getScreenName();
                apiName = formatString(currentTweet.getSource());

                if(userReply > 0) {
                    repliedUsername = "Antwort an @"+currentTweet.getInReplyToScreenName();
                }

                dateString = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss").format(currentTweet.getCreatedAt());
                answers = mTwitter.getAnswers(scrNameStr, tweetID);
                ansNo = answers.size();

                if(toggleImg) {
                    setMedia(currentTweet);
                }

            } else if(mode == RETWEET) {
                if(retweeted) {
                    mTwitter.retweet(tweetID, true);
                    retweeted = false;
                    rt--;
                } else {
                    mTwitter.retweet(tweetID, false);
                    retweeted = true;
                    rt++;
                }
            } else if(mode == FAVORITE) {
                if(favorited) {
                    mTwitter.favorite(tweetID, true);
                    favorited = false;
                    fav--;
                } else {
                    mTwitter.favorite(tweetID, false);
                    favorited = true;
                    fav++;
                }
            } else if(mode == DELETE) {
                mTwitter.deleteTweet(tweetID);
            }
        } catch(Exception err) {
            err.printStackTrace();
            return ERROR;
        }
        return mode;
    }

    @Override
    protected void onPostExecute(Long mode) {
        if(mode == LOAD_TWEET) {
            ansStr = Integer.toString(ansNo);
            rtStr = Integer.toString(rt);
            favStr = Integer.toString(fav);
            tweet.setText(highlight(tweetStr));
            username.setText(usernameStr);
            scrName.setText(scrNameStr);
            txtAns.setText(ansStr);
            date.setText(dateString);
            used_api.setText(apiName);
            if(repliedUsername != null) {
                replyName.setText(repliedUsername);
                replyName.setVisibility(View.VISIBLE);
            }
            if(verified) {
                tweet_verify.setVisibility(View.VISIBLE);
            }
            TweetDatabase tweetDatabase = new TweetDatabase(answers,c);
            TimelineAdapter tlAdp = new TimelineAdapter(c, tweetDatabase);
            replyList.setAdapter(tlAdp);
            if(toggleImg) {
                profile_img.setImageBitmap(profile_btm);
                tweet_img.setImageBitmap(tweet_btm);
            }
            replyName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(c, TweetDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("tweetID",tweetReplyID);
                    bundle.putLong("userID",userReply);
                    intent.putExtras(bundle);
                    c.startActivity(intent);
                }
            });
        }

        setIcons();
        txtRet.setText(rtStr);
        txtFav.setText(favStr);


    }

    private void setMedia(twitter4j.Status tweet) throws Exception {
        String pbLink = tweet.getUser().getProfileImageURL();
        MediaEntity[] media = tweet.getMediaEntities();

        InputStream iStream = new URL(pbLink).openStream();
        profile_btm = BitmapFactory.decodeStream(iStream);

        if( media.length > 0 ) {
            InputStream mediaStream = new URL(media[0].getMediaURL()).openStream();
            tweet_btm = BitmapFactory.decodeStream(mediaStream);
        }
    }

    /**
     * @param input xml Tag
     * @return formatted String
     */
    private String formatString(String input) {
        String output = "gesendet von: ";
        boolean openTag = false;
        for(int i = 0 ; i < input.length() ; i++){
            char current = input.charAt(i);
            if(current == '>' && !openTag){
                openTag = true;
            } else if(current == '<'){
                openTag = false;
            } else if(openTag) {
                output += current;
            }
        }
        return output;
    }

    private SpannableStringBuilder highlight(String tweet) {
        SpannableStringBuilder sTweet = new SpannableStringBuilder(tweet);
        int start = 0;
        boolean marked = false;
        for(int i = 0 ; i < tweet.length() ; i++) {
            char current = tweet.charAt(i);
            switch(current){
                case '@':
                    start = i;
                    marked = true;
                    break;
                case '#':
                    start = i;
                    marked = true;
                    break;

                case '\'':
                case ':':
                case ' ':
                case '.':
                case ',':
                case '!':
                case '?':
                    if(marked)
                        sTweet.setSpan(new ForegroundColorSpan(highlight),start,i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    marked = false;
                    break;
            }
            if(i == tweet.length()-1 && marked) {
                sTweet.setSpan(new ForegroundColorSpan(highlight),start,i+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return sTweet;
    }

    private void setIcons() {
        if(favorited)
            favoriteButton.setBackgroundResource(R.drawable.favorite_enabled);
        else
            favoriteButton.setBackgroundResource(R.drawable.favorite);
        if(retweeted)
            retweetButton.setBackgroundResource(R.drawable.retweet_enabled);
        else
            retweetButton.setBackgroundResource(R.drawable.retweet);
    }
}