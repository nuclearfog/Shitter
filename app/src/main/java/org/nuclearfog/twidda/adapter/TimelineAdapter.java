package org.nuclearfog.twidda.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Tweet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimelineAdapter extends Adapter<TimelineAdapter.ItemHolder> {

    private OnItemClickListener mListener;
    private List<Tweet> tweets;
    private int highlight = 0xFFFFFFFF;
    private int font_color = 0xFFFFFFFF;
    private boolean img_ldr = true;


    public TimelineAdapter(OnItemClickListener mListener) {
        tweets = new ArrayList<>();
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


    public void setData(List<Tweet> newTweets) {
        List<Tweet> result = new ArrayList<>(newTweets);
        result.addAll(tweets);
        tweets = result;
    }


    @Override
    public long getItemId(int pos) {
        return tweets.get(pos).getId();
    }


    @Override
    public int getItemCount() {
        return tweets.size();
    }


    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tweet, parent, false);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView rv = (RecyclerView) parent;
                int position = rv.getChildLayoutPosition(v);
                mListener.onItemClick(rv, position);
            }
        });
        return new ItemHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, int index) {
        Tweet tweet = tweets.get(index);
        String retweet = Integer.toString(tweet.getRetweetCount());
        String favorit = Integer.toString(tweet.getFavorCount());
        if (tweet.getEmbeddedTweet() != null) {
            String retweeter = "RT " + tweet.getUser().getScreenname();
            vh.retweeter.setText(retweeter);
            tweet = tweet.getEmbeddedTweet();
        } else {
            vh.retweeter.setText("");
        }
        Spanned text = Tagger.makeText(tweet.getText(), highlight);
        vh.username.setText(tweet.getUser().getUsername());
        vh.screenname.setText(tweet.getUser().getScreenname());
        vh.tweet.setText(text);
        vh.retweet.setText(retweet);
        vh.favorite.setText(favorit);
        vh.time.setText(stringTime(tweet.getTime()));

        vh.username.setTextColor(font_color);
        vh.screenname.setTextColor(font_color);
        vh.tweet.setTextColor(font_color);
        vh.time.setTextColor(font_color);

        if (img_ldr) {
            Picasso.get().load(tweet.getUser().getImageLink() + "_mini").into(vh.profile);
        }
        if (tweet.getUser().isVerified()) {
            vh.verify.setVisibility(View.VISIBLE);
        } else {
            vh.verify.setVisibility(View.GONE);
        }
    }


    private String stringTime(long mills) {
        Calendar now = Calendar.getInstance();
        long diff = now.getTimeInMillis() - mills;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        if (weeks > 4) {
            Date tweetDate = new Date(mills);
            return SimpleDateFormat.getDateInstance().format(tweetDate);
        }
        if (weeks > 0)
            return weeks + " w";
        if (days > 0)
            return days + " d";
        if (hours > 0)
            return hours + " h";
        if (minutes > 0)
            return minutes + " m";
        else
            return seconds + " s";
    }


    class ItemHolder extends ViewHolder {
        final TextView username, screenname, tweet, retweet;
        final TextView favorite, retweeter, time;
        final ImageView profile, verify;

        ItemHolder(View v) {
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
}