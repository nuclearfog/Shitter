package org.nuclearfog.twidda.adapter;

import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_ID;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class TweetAdapter extends Adapter<ViewHolder> {

    private static final int VIEW_TWEET = 0;
    private static final int VIEW_GAP = 1;

    private WeakReference<TweetClickListener> itemClickListener;
    private NumberFormat formatter;
    private List<Tweet> tweets;
    private GlobalSettings settings;

    public TweetAdapter(TweetClickListener l, GlobalSettings settings) {
        itemClickListener = new WeakReference<>(l);
        formatter = NumberFormat.getIntegerInstance();
        tweets = new ArrayList<>();
        this.settings = settings;
    }

    @MainThread
    public void insert(@NonNull List<Tweet> data, int index) {
        int maxSize = settings.getRowLimit();
        int dataSize = data.size();
        if (!tweets.isEmpty() && index < tweets.size()) {
            if (dataSize == maxSize) {
                if (tweets.get(index) != null) {
                    tweets.add(index, null);
                    dataSize++;
                }
            } else if (tweets.get(index) == null) {
                tweets.remove(index);
                dataSize--;
            }
            tweets.addAll(index, data);
            notifyItemRangeInserted(index, dataSize);
        } else {
            tweets.addAll(data);
            if (dataSize == maxSize)
                tweets.add(null);
            notifyDataSetChanged();
        }
    }

    @MainThread
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    @MainThread
    public void remove(long id) {
        int index = -1;
        for (int pos = 0; pos < tweets.size() && index < 0; pos++) {
            Tweet tweet = tweets.get(pos);
            if (tweet != null && tweet.getId() == id) {
                tweets.remove(pos);
                index = pos;
            }
        }
        if (index != -1) {
            notifyItemRemoved(index);
        }
    }

    public boolean isEmpty() {
        return tweets.isEmpty();
    }

    @Override
    public long getItemId(int index) {
        Tweet tweet = tweets.get(index);
        if (tweet != null)
            return tweet.getId();
        return NO_ID;
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    @Override
    public int getItemViewType(int index) {
        if (tweets.get(index) == null)
            return VIEW_GAP;
        return VIEW_TWEET;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TWEET) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tweet, parent, false);
            FontTool.setViewFontAndColor(settings, v);
            final TweetHolder vh = new TweetHolder(v);
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener.get() != null) {
                        int position = vh.getLayoutPosition();
                        if (position != NO_POSITION && tweets.get(position) != null) {
                            itemClickListener.get().onTweetClick(tweets.get(position));
                        }
                    }
                }
            });
            return vh;
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_placeholder, parent, false);
            final PlaceHolder vh = new PlaceHolder(v);
            vh.loadBtn.setTypeface(settings.getFontFace());
            vh.loadBtn.setTextColor(settings.getFontColor());
            vh.loadBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener.get() != null) {
                        int position = vh.getLayoutPosition();
                        if (position != NO_POSITION) {
                            long sinceId = 0;
                            long maxId = 0;
                            if (position == 0)
                                sinceId = tweets.get(position + 1).getId();
                            else if (position == tweets.size() - 1)
                                maxId = tweets.get(position - 1).getId();
                            else {
                                sinceId = tweets.get(position + 1).getId();
                                maxId = tweets.get(position - 1).getId();
                            }
                            itemClickListener.get().onHolderClick(sinceId, maxId, position);
                        }
                    }
                }
            });
            return vh;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        Tweet tweet = tweets.get(index);
        if (holder instanceof TweetHolder && tweet != null) {
            TweetHolder vh = (TweetHolder) holder;
            TwitterUser user = tweet.getUser();
            if (tweet.getEmbeddedTweet() != null) {
                String retweeter = "RT " + user.getScreenname();
                vh.retweeter.setText(retweeter);
                tweet = tweet.getEmbeddedTweet();
                user = tweet.getUser();
            } else {
                vh.retweeter.setText("");
            }
            Spanned text = Tagger.makeTextWithLinks(tweet.getTweet(), settings.getHighlightColor());
            vh.tweet.setText(text);
            vh.username.setText(user.getUsername());
            vh.screenname.setText(user.getScreenname());
            vh.retweet.setText(formatter.format(tweet.getRetweetCount()));
            vh.favorite.setText(formatter.format(tweet.getFavorCount()));
            vh.time.setText(getTimeString(tweet.getTime()));
            if (tweet.retweeted())
                vh.retweet.setCompoundDrawablesWithIntrinsicBounds(R.drawable.retweet_enabled, 0, 0, 0);
            else
                vh.retweet.setCompoundDrawablesWithIntrinsicBounds(R.drawable.retweet, 0, 0, 0);
            if (tweet.favored())
                vh.favorite.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_enabled, 0, 0, 0);
            else
                vh.favorite.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite, 0, 0, 0);
            if (user.isVerified())
                vh.username.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
            else
                vh.username.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            if (user.isLocked())
                vh.screenname.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
            else
                vh.screenname.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            if (settings.getImageLoad()) {
                String pbLink = user.getImageLink();
                if (!user.hasDefaultProfileImage())
                    pbLink += "_mini";
                Picasso.get().load(pbLink).into(vh.profile);
            } else {
                vh.profile.setImageResource(0);
            }
        }
    }

    private String getTimeString(long time) {
        long diff = new Date().getTime() - time;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        if (weeks > 4) {
            Date tweetDate = new Date(time);
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

    class TweetHolder extends ViewHolder {
        final TextView username, screenname, tweet, retweet;
        final TextView favorite, retweeter, time;
        final ImageView profile;

        TweetHolder(@NonNull View v) {
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

    class PlaceHolder extends ViewHolder {

        final Button loadBtn;

        PlaceHolder(@NonNull View v) {
            super(v);
            loadBtn = v.findViewById(R.id.item_placeholder);
        }
    }

    /**
     * Listener for tweet click
     */
    public interface TweetClickListener {

        /**
         * handle click action
         *
         * @param tweet clicked tweet
         */
        void onTweetClick(Tweet tweet);

        void onHolderClick(long sinceId, long maxId, int pos);
    }
}