package org.nuclearfog.twidda.engine.ViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import twitter4j.Status;
import java.util.Date;
import java.util.List;

import org.nuclearfog.twidda.R;

public class TimelineAdapter extends ArrayAdapter {
    private List<Status> stats;
    private Context c;
    private Date now;

    public TimelineAdapter(Context c, int layout, List<Status> stats) {
        super(c, layout);
        this.c = c;
        this.stats = stats;
        now = new Date();
    }

    @Override
    public int getCount() {
        return stats.size();
    }

    @Override
    public Object getItem(int position) {
        return stats.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(v == null) {
            LayoutInflater inf=(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.tweet, null);
        }
        Status status = stats.get(position);
        Date date = status.getCreatedAt();
        String username = status.getUser().getName();
        String twittername = status.getUser().getScreenName();
        String tweet = status.getText();
        String retweets = Integer.toString(status.getRetweetCount());
        String favorites = Integer.toString(status.getFavoriteCount());
        String answers = ""; //TODO
        String tweetdate = getTweetTime(date);

        ((TextView) v.findViewById(R.id.username)).setText(username+"  @"+twittername);
        ((TextView) v.findViewById(R.id.tweettext)).setText(tweet);
        ((TextView) v.findViewById(R.id.answer_number)).setText(answers);
        ((TextView) v.findViewById(R.id.retweet_number)).setText(retweets);
        ((TextView) v.findViewById(R.id.favorite_number)).setText(favorites);
        ((TextView) v.findViewById(R.id.time)).setText(tweetdate);

        return v;
    }

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