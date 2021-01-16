package org.nuclearfog.twidda.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.User;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.PorterDuff.Mode.SRC_ATOP;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_ID;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static org.nuclearfog.twidda.backend.utils.StringTools.getTimeString;

/**
 * Adapter class for tweet list
 *
 * @see org.nuclearfog.twidda.fragment.TweetFragment
 */
public class TweetAdapter extends Adapter<ViewHolder> {

    /**
     * index of {@link #loadingIndex} if no index is defined
     */
    private static final int NO_INDEX = -1;

    /**
     * View type for a tweet item
     */
    private static final int VIEW_TWEET = 0;

    /**
     * View type for a placeholder item
     */
    private static final int VIEW_GAP = 1;

    /**
     * Threshold to set up a placeholder
     */
    private static final int MIN_COUNT = 2;

    private TweetClickListener itemClickListener;
    private GlobalSettings settings;
    private Drawable[] icons;

    private final List<Tweet> tweets = new ArrayList<>();
    private NumberFormat formatter = NumberFormat.getIntegerInstance();
    private int loadingIndex = NO_INDEX;


    public TweetAdapter(Context context, TweetClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        settings = GlobalSettings.getInstance(context);

        TypedArray tArray = context.getResources().obtainTypedArray(R.array.tweet_item_icons);
        icons = new Drawable[tArray.length()];
        for (int index = 0; index < icons.length; index++)
            icons[index] = tArray.getDrawable(index);
        tArray.recycle();
        setIconColors();
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
     * update a single item
     *
     * @param tweet updated tweet
     */
    @MainThread
    public void updateItem(Tweet tweet) {
        int index = tweets.indexOf(tweet);
        if (index >= 0) {
            tweets.set(index, tweet);
            notifyItemChanged(index);
        }
    }

    /**
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
        setIconColors();
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
            final TweetHolder vh = new TweetHolder(v);
            vh.retweeterName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.retweet, 0, 0, 0);
            AppStyles.setTheme(settings, v);
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
            AppStyles.setTheme(settings, v);
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
            User user = tweet.getUser();
            if (tweet.getEmbeddedTweet() != null) {
                vh.retweeterName.setText(user.getScreenname());
                vh.retweeterName.setVisibility(VISIBLE);
                tweet = tweet.getEmbeddedTweet();
                user = tweet.getUser();
            } else {
                vh.retweeterName.setVisibility(INVISIBLE);
            }
            Spanned text = Tagger.makeTextWithLinks(tweet.getTweet(), settings.getHighlightColor());
            vh.tweet.setText(text);
            vh.username.setText(user.getUsername());
            vh.screenname.setText(user.getScreenname());
            vh.retweetCount.setText(formatter.format(tweet.getRetweetCount()));
            vh.favoriteCount.setText(formatter.format(tweet.getFavorCount()));
            vh.time.setText(getTimeString(tweet.getTime()));

            if (tweet.retweeted()) {
                setIcon(vh.retweetCount, icons[3]);
            } else {
                setIcon(vh.retweetCount, icons[2]);
            }
            if (tweet.favored()) {
                setIcon(vh.favoriteCount, icons[5]);
            } else {
                setIcon(vh.favoriteCount, icons[4]);
            }
            if (user.isVerified()) {
                setIcon(vh.username, icons[0]);
            } else {
                setIcon(vh.username, null);
            }
            if (user.isLocked()) {
                setIcon(vh.screenname, icons[1]);
            } else {
                setIcon(vh.screenname, null);
            }
            if (settings.getImageLoad() && user.hasProfileImage()) {
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
     * set color filter for icons
     */
    private void setIconColors() {
        icons[0].setColorFilter(settings.getIconColor(), SRC_ATOP);
        icons[1].setColorFilter(settings.getIconColor(), SRC_ATOP);
        icons[2].setColorFilter(settings.getIconColor(), SRC_ATOP);
        icons[3].setColorFilter(Color.GREEN, SRC_ATOP);
        icons[4].setColorFilter(settings.getIconColor(), SRC_ATOP);
        icons[5].setColorFilter(Color.YELLOW, SRC_ATOP);
    }

    /**
     * set TextView icon on the left
     *
     * @param tv   TextView to set/remove icon
     * @param icon icon drawable
     */
    private void setIcon(TextView tv, @Nullable Drawable icon) {
        if (icon != null)
            icon = icon.mutate();
        tv.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
    }

    /**
     * Holder class for the tweet view
     */
    private final class TweetHolder extends ViewHolder {
        final TextView username, screenname, tweet, retweetCount;
        final TextView favoriteCount, retweeterName, time;
        final ImageView profile;

        TweetHolder(@NonNull View v) {
            super(v);
            username = v.findViewById(R.id.username);
            screenname = v.findViewById(R.id.screenname);
            tweet = v.findViewById(R.id.tweettext);
            retweetCount = v.findViewById(R.id.retweet_number);
            favoriteCount = v.findViewById(R.id.favorite_number);
            retweeterName = v.findViewById(R.id.retweeter);
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