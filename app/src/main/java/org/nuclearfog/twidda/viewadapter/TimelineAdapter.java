package org.nuclearfog.twidda.viewadapter;

import android.content.Context;
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

public class TimelineAdapter extends ArrayAdapter {
    private TweetDatabase mTweets;
    private ColorPreferences mcolor;
    private Context context;
    private ViewGroup p;

    public TimelineAdapter(Context context, TweetDatabase mTweets) {
        super(context, R.layout.tweet);
        this.mTweets = mTweets;
        this.context = context;
        mcolor = ColorPreferences.getInstance(context);
    }

    public TweetDatabase getAdapter() {
        return mTweets;
    }

    @Override
    public int getCount() {
        return mTweets.getSize();
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        p = parent;
        if(v == null) {
            LayoutInflater inf=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.tweet, parent,false);
            v.setBackgroundColor(mcolor.getBackgroundColor());
        }
        ((TextView) v.findViewById(R.id.username)).setText(mTweets.getUsername(position));
        ((TextView) v.findViewById(R.id.screenname)).setText(" "+mTweets.getScreenname(position));
        ((TextView) v.findViewById(R.id.tweettext)).setText(mTweets.getTweet(position));
        ((TextView) v.findViewById(R.id.answer_number)).setText(""+mTweets.getAnswer(position));
        ((TextView) v.findViewById(R.id.retweet_number)).setText(""+mTweets.getRetweet(position));
        ((TextView) v.findViewById(R.id.favorite_number)).setText(""+mTweets.getFavorite(position));
        ((TextView) v.findViewById(R.id.time)).setText(mTweets.getDate(position));
        ImageView imgView = v.findViewById(R.id.tweetPb);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView parent = ((ListView)p);
                int position = parent.getPositionForView(v);
                parent.performItemClick(v,position,0);
            }
        });
        if(mTweets.loadImages()) {
            ImageDownloader imgDl = new ImageDownloader(imgView);
            imgDl.execute(mTweets.getPbImg(position));
        }
        return v;
    }
}