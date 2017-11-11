package org.nuclearfog.twidda.engine;

import java.util.Date;
import java.util.List;

import twitter4j.Status;

public class TweetDatabase {

    private List<Status> stats;
    private Date now;

    public TweetDatabase(List<Status> stats) {
        this.stats=stats;
        now = new Date();
    }

    public String getUsername(int pos) {
        String twittername = stats.get(pos).getUser().getScreenName();
        String username = stats.get(pos).getUser().getName();
        return twittername +" @"+username;
    }
    public String getTweet(int pos){return stats.get(pos).getText();}
    public String getRetweet(int pos){return Integer.toString(stats.get(pos).getRetweetCount());}
    public String getFavorite(int pos){return Integer.toString(stats.get(pos).getFavoriteCount());}
    public String getDate(int pos){return getTweetTime(stats.get(pos).getCreatedAt());}
    public String getAnswer(int pos){return "";/* TODO */}
    public int getSize(){return stats.size();}

    public void store(){}

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
}
