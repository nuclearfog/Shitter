package org.nuclearfog.twidda.adapter;

import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.utils.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_ID;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static org.nuclearfog.twidda.backend.utils.TimeString.getTimeString;

/**
 * Adapter class for tweet list
 *
 * @see org.nuclearfog.twidda.fragment.TweetFragment
 */
public class TweetAdapter extends Adapter<ViewHolder> {

    private static final int NO_INDEX = -1;
    private static final int VIEW_TWEET = 0;
    private static final int VIEW_GAP = 1;
    private static final int MIN_COUNT = 2;

    private TweetClickListener itemClickListener;
    private NumberFormat formatter;
    private GlobalSettings settings;

    private List<Tweet> tweets;
    private int loadingIndex;


    public TweetAdapter(TweetClickListener itemClickListener, GlobalSettings settings) {
        this.itemClickListener = itemClickListener;
        formatter = NumberFormat.getIntegerInstance();
        tweets = new ArrayList<>();
        this.settings = settings;
        loadingIndex = NO_INDEX;
    }

    /**
     * Insert data at specific index of the list
     *
     * @param data  Tweet data
     * @param index position to insert
     */
    @MainThread
    public void insertAt(@NonNull List<Tweet> data, int index) {
        disableLoading();
        if (data.size() > MIN_COUNT) {
            if (tweets.isEmpty() || tweets.get(index) != null) {
                // Add placeholder
                tweets.add(index, null);
                notifyItemInserted(index);
            }
        } else {
            if (!tweets.isEmpty() && tweets.get(index) == null) {
                // remove placeholder
                tweets.remove(index);
                notifyItemRemoved(index);
            }
        }
        if (!data.isEmpty()) {
            tweets.addAll(index, data);
            notifyItemRangeInserted(index, data.size());
        }
    }

    /**
     * Replace all items in the list
     *
     * @param data tweet data
     */
    @MainThread
    public void replaceAll(@NonNull List<Tweet> data) {
        tweets.clear();
        tweets.addAll(data);
        if (data.size() > MIN_COUNT) {
            tweets.add(null);
        }
        loadingIndex = NO_INDEX;
        notifyDataSetChanged();
    }

    /**
     * t
     * Remove specific tweet from list
     *
     * @param id ID of the tweet
     */
    @MainThread
    public void remove(long id) {
        for (int pos = 0; pos < tweets.size(); pos++) {
            Tweet tweet = tweets.get(pos);
            if (tweet != null && tweet.getId() == id) {
                tweets.remove(pos);
                notifyItemRemoved(pos);
                break;
            }
        }
    }

    /**
     * removes all items from adapter
     */
    @MainThread
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    /**
     * check if list is empty
     *
     * @return true if list is empty
     */
    public boolean isEmpty() {
        return tweets.isEmpty();
    }

    /**
     * disable placeholder load animation
     */
    public void disableLoading() {
        if (loadingIndex != NO_INDEX) {
            int oldIndex = loadingIndex;
            loadingIndex = NO_INDEX;
            notifyItemChanged(oldIndex);
        }
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
                    int position = vh.getLayoutPosition();
                    Tweet tweet = tweets.get(position);
                    if (position != NO_POSITION && tweet != null) {
                        itemClickListener.onTweetClick(tweet);
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
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        long sinceId = 0;
                        long maxId = 0;
                        if (position == 0) {
                            sinceId = tweets.get(position + 1).getId();
                        } else if (position == tweets.size() - 1) {
                            maxId = tweets.get(position - 1).getId();
                        } else {
                            sinceId = tweets.get(position + 1).getId();
                            maxId = tweets.get(position - 1).getId();
                        }
                        itemClickListener.onHolderClick(sinceId, maxId, position);
                        vh.loadCircle.setVisibility(VISIBLE);
                        vh.loadBtn.setVisibility(INVISIBLE);
                        loadingIndex = position;
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
                vh.retweeter.setText(user.getScreenname());
                vh.retweeter.setVisibility(VISIBLE);
                tweet = tweet.getEmbeddedTweet();
                user = tweet.getUser();
            } else {
                vh.retweeter.setVisibility(INVISIBLE);
            }
            Spanned text = Tagger.makeTextWithLinks(tweet.getTweet(), settings.getHighlightColor());
            vh.tweet.setText(text);
            vh.username.setText(user.getUsername());
            vh.screenname.setText(user.getScreenname());
            vh.retweet.setText(formatter.format(tweet.getRetweetCount()));
            vh.favorite.setText(formatter.format(tweet.getFavorCount()));
            vh.time.setText(getTimeString(tweet.getTime()));

            setIcon(vh.retweet, tweet.retweeted() ? R.drawable.retweet_enabled : R.drawable.retweet);
            setIcon(vh.favorite, tweet.favored() ? R.drawable.favorite_enabled : R.drawable.favorite);
            setIcon(vh.username, user.isVerified() ? R.drawable.verify : 0);
            setIcon(vh.screenname, user.isLocked() ? R.drawable.lock : 0);

            if (settings.getImageLoad()) {
                String pbLink = user.getImageLink();
                if (!user.hasDefaultProfileImage())
                    pbLink += settings.getImageSuffix();
                Picasso.get().load(pbLink).error(R.drawable.no_image).into(vh.profile);
            } else {
                vh.profile.setImageResource(0);
            }
        } else if (holder instanceof PlaceHolder) {
            PlaceHolder vh = (PlaceHolder) holder;
            if (loadingIndex != NO_INDEX) {
                vh.loadCircle.setVisibility(VISIBLE);
                vh.loadBtn.setVisibility(INVISIBLE);
            } else {
                vh.loadCircle.setVisibility(INVISIBLE);
                vh.loadBtn.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * sets an icon for a textview
     *
     * @param tv       TextView to add an icon
     * @param drawable icon
     */
    private void setIcon(TextView tv, @DrawableRes int drawable) {
        tv.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
    }

    /**
     * Holder class for the tweet view
     */
    private final class TweetHolder extends ViewHolder {
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

    /**
     * Holder class for the placeholder view
     */
    private final class PlaceHolder extends ViewHolder {
        final Button loadBtn;
        final ProgressBar loadCircle;

        PlaceHolder(@NonNull View v) {
            super(v);
            loadBtn = v.findViewById(R.id.placeholder_button);
            loadCircle = v.findViewById(R.id.placeholder_loading);
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

        /**
         * called on placeholder click
         *
         * @param sinceId the tweet ID of the tweet below the holder
         * @param maxId   the tweet ID of the tweet over the holder
         * @param pos     position of the holder
         */
        void onHolderClick(long sinceId, long maxId, int pos);
    }
}