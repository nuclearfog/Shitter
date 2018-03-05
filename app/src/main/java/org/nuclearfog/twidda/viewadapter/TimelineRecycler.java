package org.nuclearfog.twidda.viewadapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.TweetDatabase;


public class TimelineRecycler extends Adapter<TimelineRecycler.ItemHolder> implements View.OnClickListener {

    private TweetDatabase mTweets;
    private ViewGroup parent;
    private OnItemClicked mListener;
    private int background = 0x00000000;
    private int font_color = 0xFFFFFFFF;

    /**
     * @param mListener Item Click Listener
     */
    public TimelineRecycler(TweetDatabase mTweets, OnItemClicked mListener) {
        this.mListener = mListener;
        this.mTweets = mTweets;
    }

    public void setColor(int background, int font_color) {
        this.background = background;
        this.font_color = font_color;
    }

    public TweetDatabase getData() {
        return mTweets;
    }


    @Override
    public int getItemCount(){
        return mTweets.getSize();
    }


    @Override
    public long getItemId(int pos){
        return mTweets.getTweetId(pos);
    }


    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int index) {
        this.parent = parent;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet, parent,false);
        v.setBackgroundColor(background);
        v.setOnClickListener(this);
        return new ItemHolder(v);
    }


    @Override
    public void onBindViewHolder(ItemHolder vh, int index) {
        vh.tweet.setTextColor(font_color);
        vh.username.setText(mTweets.getUsername(index));
        vh.screenname.setText(mTweets.getScreenname(index));
        vh.tweet.setText(mTweets.getHighlightedTweet(parent.getContext(),index));
        vh.retweet.setText(Integer.toString(mTweets.getRetweet(index)));
        vh.favorite.setText(Integer.toString(mTweets.getFavorite(index)));
        vh.retweeter.setText(mTweets.getRetweeter(index));
        vh.time.setText(mTweets.getDate(index));
        if(mTweets.loadImages()) {
            Picasso.with(parent.getContext()).load(mTweets.getPbLink(index)).into(vh.profile);
        }
        if(mTweets.isVerified(index)) {
            vh.verify.setVisibility(View.VISIBLE);
        } else {
            vh.verify.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onClick(View view) {
        ViewGroup p = TimelineRecycler.this.parent;
        RecyclerView rv = (RecyclerView) p;
        int position = rv.getChildLayoutPosition(view);
        mListener.onItemClick(view, p, position);
    }


    class ItemHolder extends ViewHolder {
        public TextView username, screenname, tweet, retweet;
        public TextView favorite, retweeter, time;
        public ImageView profile, verify;
        public ItemHolder(View v) {
            super(v);
            username = v.findViewById(R.id.username);
            screenname = v.findViewById(R.id.screenname);
            tweet = v.findViewById(R.id.tweettext);
            retweet = v.findViewById(R.id.retweet_number);
            favorite = v.findViewById(R.id.favorite_number);
            retweeter = v.findViewById(R.id.retweeter);
            time = v.findViewById(R.id.time);
            profile = v.findViewById(R.id.tweetPb);
            verify = v.findViewById(R.id.list_verify);
        }
    }


    /**
     * Custom Click Listener
     */
    public interface OnItemClicked {
        void onItemClick(View v, ViewGroup parent, int position);
    }
}
