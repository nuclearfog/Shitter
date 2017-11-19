package org.nuclearfog.twidda.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import twitter4j.Status;

public class TweetDatabase {


    private Date now;
    private List<String> user,tweet,noRT,noFav,noAns,date;

    /**
     * @param stats   Twitter Home
     */
    public TweetDatabase(List<Status> stats) {
        initArray();
        fillArray(stats);
    }

    /**
     * Set Manually Data using setData()
     */
    public TweetDatabase() {
        initArray();
    }

    /**
     * Save Data
     * @param usr Username
     * @param tw Tweet
     * @param rt Number Retweets
     * @param fav Number Favorites
     * @param time Tweet Time
     * @param ans Number Answers
     */
    public void setData(String usr,String tw,String rt,String fav,String time,String ans) {
        user.add(usr);
        tweet.add(tw);
        noRT.add(rt);
        noFav.add(fav);
        noAns.add(ans);
        date.add(time);
    }

    public String getUsername(int pos) {return user.get(pos);}
    public String getTweet(int pos){return tweet.get(pos);}
    public String getRetweet(int pos){return noRT.get(pos);}
    public String getFavorite(int pos){return noFav.get(pos);}
    public String getDate(int pos){return date.get(pos);}
    public String getAnswer(int pos){return noAns.get(pos);}
    public int getSize(){return user.size();}

    private String getTweetTime(Date time) {
        int tweetHour = now.getHours() - time.getHours();
        int tweetMin  = now.getMinutes() - time.getMinutes();
        int tweetSec  = now.getSeconds() - time.getSeconds();
        if (tweetHour > 0)
            return "vor "+tweetHour+" h";
        else if ( tweetMin > 0)
            return "vor "+tweetMin+" min";
        else
            return "vor "+tweetSec+" sec";
    }

    private void initArray() {
        now   = new Date();
        user  = new ArrayList<>();
        tweet = new ArrayList<>();
        noRT  = new ArrayList<>();
        noFav = new ArrayList<>();
        noAns = new ArrayList<>();
        date  = new ArrayList<>();
    }

    private void fillArray(List<Status> stats) {
        for(short pos = 0; pos < getSize(); pos++) {
            tweet.add( stats.get(pos).getText() );
            noRT.add( Integer.toString(stats.get(pos).getRetweetCount()) );
            noFav.add( Integer.toString(stats.get(pos).getFavoriteCount()) );
            date.add( getTweetTime(stats.get(pos).getCreatedAt()) );
            String name = stats.get(pos).getUser().getScreenName();
            String twUsr = stats.get(pos).getUser().getName();
            user.add( name +" @"+twUsr );
        }
    }
}