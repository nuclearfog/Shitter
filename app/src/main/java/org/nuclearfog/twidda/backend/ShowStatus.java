package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.TwitterException;

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
    public static final long LOAD_REPLY = 4;

    private Context c;
    private TwitterEngine mTwitter;
    private ListView replyList;
    private TextView  username,scrName,replyName,tweet;
    private TextView used_api,txtAns,txtRet,txtFav,date;
    private Button retweetButton,favoriteButton, mediabutton;
    private ImageView profile_img,tweet_verify;
    private List<twitter4j.Status> answers;
    private SwipeRefreshLayout ansReload;
    private TimelineAdapter tlAdp;
    private Bitmap profile_btm;
    private String errMSG = "";
    private String usernameStr, scrNameStr, tweetStr, dateString;
    private String repliedUsername, apiName;
    private boolean retweeted, favorited, toggleImg, verified;
    private int rt, fav, ansNo = 0;
    private int highlight;
    private long userReply, tweetReplyID;
    private String medialinks[];

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
        ansReload = (SwipeRefreshLayout) ((TweetDetail)c).findViewById(R.id.answer_reload);

        profile_img = (ImageView) ((TweetDetail)c).findViewById(R.id.profileimage_detail);
        tweet_verify =(ImageView)((TweetDetail)c).findViewById(R.id.tweet_verify);

        retweetButton = (Button) ((TweetDetail)c).findViewById(R.id.rt_button_detail);
        favoriteButton = (Button) ((TweetDetail)c).findViewById(R.id.fav_button_detail);
        mediabutton = (Button) ((TweetDetail)c).findViewById(R.id.image_attach);
    }

    /**
     * @param data [0] TWEET ID , [1] Mode
     */
    @Override
    protected Long doInBackground(Long... data) {
        long tweetID = data[0];
        long mode = data[1];
        try {
            twitter4j.Status currentTweet = mTwitter.getStatus(tweetID);
            scrNameStr = '@'+currentTweet.getUser().getScreenName();
            rt = currentTweet.getRetweetCount();
            fav = currentTweet.getFavoriteCount();
            retweeted = currentTweet.isRetweetedByMe();
            favorited = currentTweet.isFavorited();

            if(mode == LOAD_TWEET) {
                userReply = currentTweet.getInReplyToUserId();
                tweetReplyID = currentTweet.getInReplyToStatusId();
                verified = currentTweet.getUser().isVerified();
                tweetStr = currentTweet.getText();
                usernameStr = currentTweet.getUser().getName();
                scrNameStr = '@'+currentTweet.getUser().getScreenName();
                apiName = formatString(currentTweet.getSource());
                dateString = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss").format(currentTweet.getCreatedAt());

                if(userReply > 0)
                    repliedUsername = "Antwort an @"+currentTweet.getInReplyToScreenName();
                if(toggleImg) {
                    String pbLink = currentTweet.getUser().getProfileImageURL();
                    InputStream iStream = new URL(pbLink).openStream();
                    profile_btm = BitmapFactory.decodeStream(iStream);

                    MediaEntity[] media = currentTweet.getMediaEntities();
                    medialinks = new String[media.length];
                    for(int i = 0 ; i < media.length ; i++) {
                        medialinks[i] = media[i].getMediaURL();
                    }
                }
            }
            else if(mode == RETWEET) {
                if(retweeted) {
                    mTwitter.retweet(tweetID, true);
                    TweetDatabase.delete(c, tweetID);
                    retweeted = false;
                    rt--;
                } else {
                    mTwitter.retweet(tweetID, false);
                    retweeted = true;
                    rt++;
                }
            }
            else if(mode == FAVORITE) {
                if(favorited) {
                    mTwitter.favorite(tweetID, true);
                    favorited = false;
                    fav--;
                } else {
                    mTwitter.favorite(tweetID, false);
                    favorited = true;
                    fav++;
                }
            }
            else if(mode == LOAD_REPLY) {
                tlAdp = (TimelineAdapter) replyList.getAdapter();
                if(tlAdp != null)
                    tweetID = tlAdp.getItemId(0);
                answers = mTwitter.getAnswers(scrNameStr, tweetID);
                ansNo = answers.size();
            }
            else if(mode == DELETE) {
                mTwitter.deleteTweet(tweetID);
            }
        }catch(TwitterException e) {
            int err = e.getErrorCode();
            if(err == 144) { // gelöscht
                TweetDatabase.delete(c,tweetID);
            }
            e.printStackTrace();
            return ERROR;
        } catch(Exception err) {
            errMSG = err.getMessage();
            return ERROR;
        }
        return mode;
    }

    @Override
    protected void onPostExecute(Long mode) {
        if(mode == LOAD_TWEET) {
            tweet.setText(highlight(tweetStr));
            username.setText(usernameStr);
            scrName.setText(scrNameStr);
            date.setText(dateString);
            used_api.setText(apiName);

            String favStr = Integer.toString(fav);
            String rtStr = Integer.toString(rt);
            txtFav.setText(favStr);
            txtRet.setText(rtStr);
            txtAns.setText("0");

            setIcons();
            if(repliedUsername != null) {
                replyName.setText(repliedUsername);
                replyName.setVisibility(View.VISIBLE);
            }
            if(verified) {
                tweet_verify.setVisibility(View.VISIBLE);
            }
            if(toggleImg) {
                profile_img.setImageBitmap(profile_btm);
                if(medialinks.length != 0) {
                    mediabutton.setVisibility(View.VISIBLE);
                    mediabutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new ImagePopup(c).execute(medialinks);
                        }
                    });
                }
            }
            setIcons();
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
        else if(mode == RETWEET) {
            String rtStr = Integer.toString(rt);
            txtRet.setText(rtStr);
            setIcons();
        }
        else if(mode == FAVORITE) {
            String favStr = Integer.toString(fav);
            txtFav.setText(favStr);
            setIcons();
        }
        else if(mode == LOAD_REPLY) {
            if(tlAdp == null || tlAdp.getCount() == 0) {
                TweetDatabase tweetDatabase = new TweetDatabase(answers,c);
                tlAdp = new TimelineAdapter(c, tweetDatabase);
                replyList.setAdapter(tlAdp);
            } else {
                TweetDatabase twDb = tlAdp.getData();
                twDb.addHot(answers);
                tlAdp.notifyDataSetChanged();
                ansReload.setRefreshing(false);
            }
            String ansStr = Integer.toString(ansNo);
            txtAns.setText(ansStr);
        }
        else if(mode == DELETE) {
            Toast.makeText(c, "Tweet gelöscht", Toast.LENGTH_LONG).show();
            ((TweetDetail)c).finish();
        }
        else {
            Toast.makeText(c, "Fehler beim Laden: "+errMSG, Toast.LENGTH_LONG).show();
            if(ansReload.isRefreshing()) {
                ansReload.setRefreshing(false);
            }
        }
    }

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
                case '#':
                    start = i;
                    marked = true;
                    break;

                case '\'':
                case '\"':
                case ':':
                case ' ':
                case '.':
                case ',':
                case '!':
                case '?':
                    if(marked) {
                        sTweet.setSpan(new ForegroundColorSpan(highlight),start,i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    marked = false;
                    break;
            }
        }
        if(marked) {
            sTweet.setSpan(new ForegroundColorSpan(highlight),start,tweet.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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