package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;

import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.window.TweetDetail;

public class ShowStatus extends AsyncTask<Long, Void, Boolean> {

    public static final long RETWEET = 0;
    public static final long FAVORITE = 1;

    private Context c;
    private Twitter twitter;
    private ListView replyList;
    private TextView  username,scrName, tweet, txtAns, txtRet, txtFav;
    private Button retweetButton, favoriteButton;
    private ImageView profile_img, tweet_img;
    private ArrayList<twitter4j.Status> answers;
    private String usernameStr, scrNameStr, tweetStr;
    private String ansStr, rtStr, favStr;
    private boolean retweeted, favorited, toggleImg;
    private SharedPreferences settings;
    private int load, ansNo;
    private Bitmap profile_btm, tweet_btm;

    public ShowStatus(Context c) {
        twitter = TwitterResource.getInstance(c).getTwitter();
        answers = new ArrayList<>();
        settings = c.getSharedPreferences("settings", 0);
        load = settings.getInt("preload", 10);
        toggleImg = settings.getBoolean("image_load", false);
        this.c = c;
        ansNo = 0;
    }

    @Override
    protected void onPreExecute() {
        replyList = (ListView) ((TweetDetail)c).findViewById(R.id.answer_list);
        tweet = (TextView) ((TweetDetail)c).findViewById(R.id.tweet_detailed);
        username = (TextView) ((TweetDetail)c).findViewById(R.id.usernamedetail);
        scrName = (TextView) ((TweetDetail)c).findViewById(R.id.scrnamedetail);

        txtAns = (TextView) ((TweetDetail)c).findViewById(R.id.no_ans_detail);
        txtRet = (TextView) ((TweetDetail)c).findViewById(R.id.no_rt_detail);
        txtFav = (TextView) ((TweetDetail)c).findViewById(R.id.no_fav_detail);

        profile_img = (ImageView) ((TweetDetail)c).findViewById(R.id.profileimage_detail);
        tweet_img   = (ImageView) ((TweetDetail)c).findViewById(R.id.tweet_image);

        retweetButton = (Button) ((TweetDetail)c).findViewById(R.id.rt_button_detail);
        favoriteButton = (Button) ((TweetDetail)c).findViewById(R.id.fav_button_detail);
    }

    /**
     * @param id [0] TWEET ID , [1] Mode
     */
    @Override
    protected Boolean doInBackground(Long... id) {
        long tweetID = id[0];
        try {
            twitter4j.Status currentTweet = twitter.showStatus(tweetID);
            rtStr = Integer.toString(currentTweet.getRetweetCount());
            favStr = Integer.toString(currentTweet.getFavoriteCount());
            retweeted = currentTweet.isRetweetedByMe();
            favorited = currentTweet.isFavorited();
            if(id.length == 1) {
                tweetStr = currentTweet.getText();
                usernameStr = currentTweet.getUser().getName();
                scrNameStr = currentTweet.getUser().getScreenName();

                Query query = new Query("to:"+scrNameStr+" since_id:"+tweetID+" +exclude:retweets");
                query.setCount(load);

                QueryResult result = twitter.search(query);
                List<twitter4j.Status> stats = result.getTweets();

                for(twitter4j.Status reply : stats) {
                    if(reply.getInReplyToStatusId() == tweetID) {
                        answers.add(reply);
                        ansNo++;
                    }
                }
                if(toggleImg)
                    setMedia(currentTweet);
                return true;
            } else {
                if(id[1]==RETWEET) {
                    if(retweeted) {
                        //TODO
                    } else {
                        twitter.retweetStatus(tweetID);
                        retweeted = true;
                    }
                } else if(id[1]==FAVORITE) {
                    if(favorited) {
                        twitter.destroyFavorite(tweetID);
                        favorited = false;
                    } else {
                        twitter.createFavorite(tweetID);
                        favorited = true;
                    }
                }
                return false;
            }
        } catch(Exception err){ err.printStackTrace(); }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean tweetLoaded) {
        if(tweetLoaded) {
            ansStr = Integer.toString(ansNo);
            tweet.setText(tweetStr);
            username.setText(usernameStr);
            scrName.setText(scrNameStr);
            txtAns.setText(ansStr);
            TweetDatabase tweetDatabase = new TweetDatabase(answers,c);
            TimelineAdapter tlAdp = new TimelineAdapter(c, tweetDatabase);
            replyList.setAdapter(tlAdp);
        }
        if(toggleImg) {
            profile_img.setImageBitmap(profile_btm);
            tweet_img.setImageBitmap(tweet_btm);
        }

        setIcons();
        txtRet.setText(rtStr);
        txtFav.setText(favStr);
    }

    private void setMedia(twitter4j.Status tweet) throws Exception {
        String pbLink = tweet.getUser().getMiniProfileImageURL();
        InputStream iStream = new URL(pbLink).openStream();
        profile_btm = BitmapFactory.decodeStream(iStream);

        MediaEntity[] media = tweet.getMediaEntities();
        if( media.length > 0 ) {
            InputStream mediaStream = new URL(media[0].getMediaURL()).openStream();
            tweet_btm = BitmapFactory.decodeStream(mediaStream);
        }
    }

    private void setIcons() {
        if(favorited) {
            favoriteButton.setBackgroundResource(R.drawable.favorite_enabled);
        } else {
            favoriteButton.setBackgroundResource(R.drawable.favorite);
        } if(retweeted) {
            retweetButton.setBackgroundResource(R.drawable.retweet_enabled);
        } else {
            retweetButton.setBackgroundResource(R.drawable.retweet);
        }
    }
}