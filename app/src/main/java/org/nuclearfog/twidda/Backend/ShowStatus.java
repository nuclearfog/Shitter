package org.nuclearfog.twidda.Backend;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.TextView;

import org.nuclearfog.twidda.DataBase.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.ViewAdapter.TimelineAdapter;
import org.nuclearfog.twidda.Window.TweetDetail;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;


public class ShowStatus extends AsyncTask<Long, Void, Void> {

    private Context c;
    private Twitter twitter;
    private ListView replyList;
    private TextView  username,scrName, tweet;
    private ArrayList<twitter4j.Status> answers;
    private String usernameStr, scrNameStr, tweetStr;

    public ShowStatus( Context c) {
        twitter = TwitterResource.getInstance(c).getTwitter();
        answers = new ArrayList<>();
        this.c=c;
    }

    @Override
    protected void onPreExecute() {
        replyList = (ListView) ((TweetDetail)c).findViewById(R.id.answer_list);
        tweet = (TextView) ((TweetDetail)c).findViewById(R.id.tweet_detailed);
        username = (TextView) ((TweetDetail)c).findViewById(R.id.usernamedetail);
        scrName = (TextView) ((TweetDetail)c).findViewById(R.id.scrnamedetail);
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

            Query query = new Query('@'+scrNameStr+" since_id:"+tweetID);
            query.setCount(10);//TODO
            QueryResult result= null;
            do {
                result = twitter.search(query);
                List<twitter4j.Status> stats = result.getTweets();

                for(twitter4j.Status reply : stats) {
                    if(reply.getInReplyToStatusId() == tweetID)
                        answers.add(reply);
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

        TweetDatabase tweetDatabase = new TweetDatabase(answers,c);
        TimelineAdapter tlAdp = new TimelineAdapter(c, tweetDatabase);
        replyList.setAdapter(tlAdp);
    }
}