package org.nuclearfog.twidda.viewadapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimelineRecycler extends Adapter<TimelineRecycler.ItemHolder> implements View.OnClickListener {
    private ViewGroup parent;
    private OnItemClicked mListener;
    private List<Tweet> tweets;
    private int highlight = 0xFFFFFFFF;
    private int font_color = 0xFFFFFFFF;
    private boolean img_ldr = true;

    /**
     * @param mListener Item Click Listener
     */
    public TimelineRecycler(List<Tweet> tweets, OnItemClicked mListener) {
        this.tweets = tweets;
        this.mListener = mListener;
    }


    public void setColor(int highlight, int font_color) {
        this.highlight = highlight;
        this.font_color = font_color;
    }

    public void toggleImage(boolean image_load) {
        img_ldr = image_load;
    }


    public List<Tweet> getData() {
        return tweets;
    }


    @Override
    public int getItemCount(){
        return tweets.size();
    }


    @Override
    public long getItemId(int pos){
        return tweets.get(pos).tweetID;
    }


    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int index) {
        this.parent = parent;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet, parent,false);
        v.setOnClickListener(this);
        return new ItemHolder(v);
    }


    @Override
    public void onBindViewHolder(ItemHolder vh, int index) {
        Tweet tweet = tweets.get(index);
        String retweet = Integer.toString(tweet.retweet);
        String favorit = Integer.toString(tweet.favorit);
        if(tweet.embedded != null) {
            String retweeter = "RT "+tweet.user.screenname;
            vh.retweeter.setText(retweeter);
            tweet = tweet.embedded;
        } else {
            vh.retweeter.setText("");
        }
        vh.tweet.setTextColor(font_color);
        vh.username.setText(tweet.user.username);
        vh.screenname.setText(tweet.user.screenname);
        vh.tweet.setText(highlight(tweet.tweet));
        vh.retweet.setText(retweet);
        vh.favorite.setText(favorit);
        vh.time.setText(stringTime(tweet.time));
        if(img_ldr) {
            Picasso.with(parent.getContext()).load(tweet.user.profileImg+"_mini").into(vh.profile);
        }
        if(tweet.user.isVerified) {
            vh.verify.setVisibility(View.VISIBLE);
        } else {
            vh.verify.setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View view) {
        ViewGroup p = TimelineRecycler.this.parent;
        RecyclerView rv = (RecyclerView) p;
        int position = rv.getChildLayoutPosition(view);
        mListener.onItemClick(view, p, position);
    }


    private String stringTime(long mills) {
        Calendar now = Calendar.getInstance();
        long diff = now.getTimeInMillis() - mills;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        if(weeks > 4) {
            Date tweetDate = new Date(mills);
            return SimpleDateFormat.getDateInstance().format(tweetDate);
        }
        if(weeks > 0)
            return "vor "+weeks+" w";
        if(days > 0)
            return "vor "+days+" d";
        if(hours > 0)
            return "vor "+hours+" h";
        if(minutes > 0)
            return "vor "+minutes+" m";
        else
            return "vor "+seconds+" s";
    }


    public SpannableStringBuilder highlight(String tweet) {
        SpannableStringBuilder sTweet = new SpannableStringBuilder(tweet);
        int start = 0;
        boolean marked = false;
        for(int i = 0 ; i < tweet.length() ; i++) {
            char current = tweet.charAt(i);
            switch(current){
                case '@':
                    start = i;
                    marked = true;
                    break;
                case '#':
                    start = i;
                    marked = true;
                    break;

                case '\'':
                case '\"':
                case '\n':
                case ')':
                case '(':
                case ':':
                case ' ':
                case '.':
                case ',':
                case '!':
                case '?':
                case '-':
                    if(marked)
                        sTweet.setSpan(new ForegroundColorSpan(highlight),start,i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    marked = false;
                    break;
            }
            if(i == tweet.length()-1 && marked) {
                sTweet.setSpan(new ForegroundColorSpan(highlight),start,i+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return sTweet;
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