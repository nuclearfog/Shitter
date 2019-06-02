package org.nuclearfog.twidda.adapter;

import android.graphics.Color;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Tweet;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class TweetAdapter extends Adapter<TweetAdapter.ItemHolder> {

    private WeakReference<OnItemClickListener> itemClickListener;
    private Tweet[] tweets;

    private NumberFormat formatter;
    private int highlight;
    private int font_color;
    private boolean image_load;


    public TweetAdapter(OnItemClickListener l) {
        itemClickListener = new WeakReference<>(l);
        formatter = NumberFormat.getIntegerInstance();
        tweets = new Tweet[0];
        highlight = Color.WHITE;
        font_color = Color.WHITE;
        image_load = true;
    }


    public void setColor(int highlight, int font_color) {
        this.highlight = highlight;
        this.font_color = font_color;
    }


    public void toggleImage(boolean image_load) {
        this.image_load = image_load;
    }


    public Tweet getData(int pos) {
        return tweets[pos];
    }


    public List<Tweet> getData() {
        List<Tweet> data = new LinkedList<>();
        for (Tweet tweet : tweets)
            data.add(tweet);
        return data;
    }


    public void setData(@NonNull List<Tweet> newTweets) {
        tweets = newTweets.toArray(new Tweet[0]);
    }


    public boolean isEmpty() {
        return tweets.length == 0;
    }


    @Override
    public long getItemId(int pos) {
        return tweets[pos].getId();
    }


    @Override
    public int getItemCount() {
        return tweets.length;
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
                if (itemClickListener.get() != null)
                    itemClickListener.get().onItemClick(position);
            }
        });
        return new ItemHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, int index) {
        Tweet tweet = tweets[index];
        if (tweet.getEmbeddedTweet() != null) {
            String retweeter = "RT " + tweet.getUser().getScreenname();
            vh.retweeter.setText(retweeter);
            tweet = tweet.getEmbeddedTweet();
        } else {
            vh.retweeter.setText("");
        }
        Spanned text = Tagger.makeText(tweet.getTweet(), highlight);
        vh.username.setText(tweet.getUser().getUsername());
        vh.screenname.setText(tweet.getUser().getScreenname());
        vh.tweet.setText(text);
        vh.retweet.setText(formatter.format(tweet.getRetweetCount()));
        vh.favorite.setText(formatter.format(tweet.getFavorCount()));
        vh.time.setText(stringTime(tweet.getTime()));

        vh.username.setTextColor(font_color);
        vh.screenname.setTextColor(font_color);
        vh.tweet.setTextColor(font_color);
        vh.time.setTextColor(font_color);

        if (tweet.retweeted())
            vh.retweet.setCompoundDrawablesWithIntrinsicBounds(R.drawable.retweet_enabled, 0, 0, 0);
        else
            vh.retweet.setCompoundDrawablesWithIntrinsicBounds(R.drawable.retweet, 0, 0, 0);
        if (tweet.favored())
            vh.favorite.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_enabled, 0, 0, 0);
        else
            vh.favorite.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite, 0, 0, 0);
        if (tweet.getUser().isVerified())
            vh.username.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
        else
            vh.username.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        if (tweet.getUser().isLocked())
            vh.screenname.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
        else
            vh.screenname.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        if (image_load)
            Picasso.get().load(tweet.getUser().getImageLink() + "_mini").into(vh.profile);
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
        return seconds + " s";
    }


    class ItemHolder extends ViewHolder {
        final TextView username, screenname, tweet, retweet;
        final TextView favorite, retweeter, time;
        final ImageView profile;

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
        }
    }
}