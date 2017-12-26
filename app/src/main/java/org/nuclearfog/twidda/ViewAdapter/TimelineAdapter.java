package org.nuclearfog.twidda.ViewAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.nuclearfog.twidda.Engine.ImageDownloader;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.DataBase.TweetDatabase;

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

    @NonNull
    @Override
    public View getView(int position, View v,@NonNull ViewGroup parent) {
        if(v == null) {
            LayoutInflater inf=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.tweet, parent,false);
        }
        ((TextView) v.findViewById(R.id.username)).setText(mTweets.getUsername(position));
        ((TextView) v.findViewById(R.id.tweettext)).setText(mTweets.getTweet(position));
        ((TextView) v.findViewById(R.id.answer_number)).setText(mTweets.getAnswer(position));
        ((TextView) v.findViewById(R.id.retweet_number)).setText(mTweets.getRetweet(position));
        ((TextView) v.findViewById(R.id.favorite_number)).setText(mTweets.getFavorite(position));
        ((TextView) v.findViewById(R.id.time)).setText(mTweets.getDate(position));
        if(mTweets.loadImages()) {
            ImageView imgView = v.findViewById(R.id.tweetPb);
            ImageDownloader imgDl = new ImageDownloader(imgView);
            imgDl.execute(mTweets.getPbImg(position));
        }
        return v;
    }
}