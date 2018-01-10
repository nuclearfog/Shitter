package org.nuclearfog.twidda.viewadapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.ImageDownloader;
import org.nuclearfog.twidda.database.ColorPreferences;
import org.nuclearfog.twidda.database.TweetDatabase;

public class TimelineAdapter extends ArrayAdapter implements View.OnClickListener {
    private TweetDatabase mTweets;
    private ColorPreferences mcolor;
    private ViewGroup p;
    private LayoutInflater inf;

    public TimelineAdapter(Context context, TweetDatabase mTweets) {
        super(context, R.layout.tweet);
        this.mTweets = mTweets;
        inf = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mcolor = ColorPreferences.getInstance(context);
    }

    public TweetDatabase getAdapter() {
        return mTweets;
    }

    @Override
    public int getCount() {
        return mTweets.getSize();
    }

    @NonNull
    @Override
    public View getView(int position, View v, @NonNull ViewGroup parent) {
        p = parent;
        if(v == null) {
            v = inf.inflate(R.layout.tweet, parent,false);
            v.setBackgroundColor(mcolor.getBackgroundColor());
        }
        String answerStr = Integer.toString(mTweets.getAnswer(position));
        String retweetStr = Integer.toString(mTweets.getRetweet(position));
        String favoriteStr = Integer.toString(mTweets.getFavorite(position));

        ((TextView) v.findViewById(R.id.username)).setText(mTweets.getUsername(position));
        ((TextView) v.findViewById(R.id.screenname)).setText(mTweets.getScreenname(position));
        ((TextView) v.findViewById(R.id.tweettext)).setText(mTweets.getTweet(position));
        ((TextView) v.findViewById(R.id.answer_number)).setText(answerStr);
        ((TextView) v.findViewById(R.id.retweet_number)).setText(retweetStr);
        ((TextView) v.findViewById(R.id.favorite_number)).setText(favoriteStr);
        ((TextView) v.findViewById(R.id.time)).setText(mTweets.getDate(position));
        ImageView imgView = v.findViewById(R.id.tweetPb);
        v.setOnClickListener(this);
        imgView.setImageResource(R.mipmap.pb);
        if(mTweets.loadImages())
            new ImageDownloader(imgView).execute(mTweets.getPbImg(position));
        return v;
    }

    @Override
    public void onClick(View v) {
        ListView parent = ((ListView)p);
        int position = parent.getPositionForView(v);
        parent.performItemClick(v,position,0);
    }
}