package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.TextView;

import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.window.TweetDetail;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;


public class ShowStatus extends AsyncTask<Long, Void, Void> {

    private Context c;
    private Twitter twitter;
    private ListView replyList;
    private TextView  username,scrName, tweet, txtAns, txtRet, txtFav;
    private ArrayList<twitter4j.Status> answers;
    private String usernameStr, scrNameStr, tweetStr;
    private String ansStr, rtStr, favStr;
    private SharedPreferences settings;
    private int load, ansNo;

    public ShowStatus(Context c) {
        twitter = TwitterResource.getInstance(c).getTwitter();
        answers = new ArrayList<>();
        settings = c.getSharedPreferences("settings", 0);
        load = settings.getInt("preload", 10);
        this.c=c;
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
    }

    /**
     * @param id TWEET ID
     */
    @Override
    protected Void doInBackground(Long... id) {
        long tweetID = id[0];
        try {
            twitter4j.Status currentTweet = twitter.showStatus(tweetID);
            tweetStr = currentTweet.getText();
            usernameStr = currentTweet.getUser().getName();
            scrNameStr = currentTweet.getUser().getScreenName();
            ansStr = ""; //todo
            rtStr = Integer.toString(currentTweet.getRetweetCount());
            favStr = Integer.toString(currentTweet.getFavoriteCount());

            Query query = new Query('@'+scrNameStr+" since_id:"+tweetID+" +exclude:retweets");
            query.setCount(load);
            QueryResult result= null;
            do {
                result = twitter.search(query);
                List<twitter4j.Status> stats = result.getTweets();

                for(twitter4j.Status reply : stats) {
                    if(reply.getInReplyToStatusId() == tweetID) {
                        answers.add(reply);
                        ansNo++;
                    }
                }
            } while((query = result.nextQuery()) != null);
        } catch(Exception err){err.printStackTrace();}
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        tweet.setText(tweetStr);
        username.setText(usernameStr);
        scrName.setText(scrNameStr);

        ansStr = Integer.toString(ansNo);
        txtAns.setText(ansStr);
        txtRet.setText(rtStr);
        txtFav.setText(favStr);

        TweetDatabase tweetDatabase = new TweetDatabase(answers,c);
        TimelineAdapter tlAdp = new TimelineAdapter(c, tweetDatabase);
        replyList.setAdapter(tlAdp);
    }
}