package org.nuclearfog.twidda.viewadapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.Tweet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimelineAdapter extends Adapter<TimelineAdapter.ItemHolder> {

    private OnItemClicked mListener;
    private List<Tweet> tweets;
    private int highlight = 0xFFFFFFFF;
    private int font_color = 0xFFFFFFFF;
    private boolean img_ldr = true;


    public TimelineAdapter(OnItemClicked mListener) {
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
        newTweets.addAll(tweets);
        tweets = newTweets;
    }


    @Override
    public long getItemId(int pos) {
        return tweets.get(pos).tweetID;
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
                mListener.onItemClick(parent, position);
            }
        });
        return new ItemHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, int index) {
        Tweet tweet = tweets.get(index);
        String retweet = Integer.toString(tweet.retweet);
        String favorit = Integer.toString(tweet.favorit);
        if (tweet.embedded != null) {
            String retweeter = "RT " + tweet.user.screenname;
            vh.retweeter.setText(retweeter);
            tweet = tweet.embedded;
        } else {
            vh.retweeter.setText("");
        }

        vh.username.setText(tweet.user.username);
        vh.screenname.setText(tweet.user.screenname);
        vh.tweet.setText(highlight(tweet.tweet));
        vh.retweet.setText(retweet);
        vh.favorite.setText(favorit);
        vh.time.setText(stringTime(tweet.time));

        vh.username.setTextColor(font_color);
        vh.screenname.setTextColor(font_color);
        vh.tweet.setTextColor(font_color);
        vh.time.setTextColor(font_color);

        if (img_ldr) {
            Picasso.get().load(tweet.user.profileImg + "_mini").into(vh.profile);
        }
        if (tweet.user.isVerified) {
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


    public SpannableStringBuilder highlight(String tweet) {
        SpannableStringBuilder sTweet = new SpannableStringBuilder(tweet);
        int start = 0;
        boolean marked = false;
        for (int i = 0; i < tweet.length(); i++) {
            char current = tweet.charAt(i);
            switch (current) {
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
                    if (marked)
                        sTweet.setSpan(new ForegroundColorSpan(highlight), start, i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    marked = false;
                    break;
            }
            if (i == tweet.length() - 1 && marked) {
                sTweet.setSpan(new ForegroundColorSpan(highlight), start, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return sTweet;
    }


    /**
     * Custom Click Listener
     */
    public interface OnItemClicked {
        void onItemClick(ViewGroup parent, int position);
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