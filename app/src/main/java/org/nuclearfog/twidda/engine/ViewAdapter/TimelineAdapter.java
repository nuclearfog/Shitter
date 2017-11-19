package org.nuclearfog.twidda.engine.ViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.engine.TweetDatabase;

public class TimelineAdapter extends ArrayAdapter {
    private TweetDatabase mTweets;
    private Context context;

    public TimelineAdapter(Context context, int layout, TweetDatabase mTweets) {
        super(context, layout);
        this.mTweets = mTweets;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mTweets.getSize();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(v == null) {
            LayoutInflater inf=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.tweet, null);
        }
        ((TextView) v.findViewById(R.id.username)).setText(mTweets.getUsername(position));
        ((TextView) v.findViewById(R.id.tweettext)).setText(mTweets.getTweet(position));
        ((TextView) v.findViewById(R.id.answer_number)).setText(mTweets.getAnswer(position));
        ((TextView) v.findViewById(R.id.retweet_number)).setText(mTweets.getRetweet(position));
        ((TextView) v.findViewById(R.id.favorite_number)).setText(mTweets.getFavorite(position));
        ((TextView) v.findViewById(R.id.time)).setText(mTweets.getDate(position));
        return v;
    }
}