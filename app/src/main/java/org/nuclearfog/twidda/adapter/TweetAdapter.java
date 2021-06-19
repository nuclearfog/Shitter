package org.nuclearfog.twidda.adapter;

import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.holder.Footer;
import org.nuclearfog.twidda.adapter.holder.TweetHolder;
import org.nuclearfog.twidda.backend.model.Tweet;
import org.nuclearfog.twidda.backend.model.User;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static android.graphics.PorterDuff.Mode.SRC_IN;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_ID;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static org.nuclearfog.twidda.backend.utils.StringTools.formatCreationTime;

/**
 * Adapter class for tweet list
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.fragment.TweetFragment
 */
public class TweetAdapter extends Adapter<ViewHolder> {

    /**
     * index of {@link #loadingIndex} if no index is defined
     */
    private static final int NO_LOADING = -1;

    /**
     * View type for a tweet item
     */
    private static final int VIEW_TWEET = 0;

    /**
     * View type for a placeholder item
     */
    private static final int VIEW_GAP = 1;

    /**
     * Minimum count of new Tweets to insert a placeholder.
     */
    private static final int MIN_COUNT = 2;

    /**
     * Locale specific number format
     */
    private static final NumberFormat NUM_FORMAT = NumberFormat.getIntegerInstance();


    private TweetClickListener itemClickListener;
    private GlobalSettings settings;

    private final List<Tweet> tweets = new LinkedList<>();
    private int loadingIndex = NO_LOADING;

    /**
     * @param settings          App settings for theme
     * @param itemClickListener listener for item click
     */
    public TweetAdapter(GlobalSettings settings, TweetClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        this.settings = settings;
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
        loadingIndex = NO_LOADING;
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
        for (int pos = tweets.size() - 1; pos >= 0; pos--) {
            Tweet tweet = tweets.get(pos);
            if (tweet != null) {
                Tweet embedded = tweet.getEmbeddedTweet();
                // remove tweet and any retweet of it
                if (tweet.getId() == id || (embedded != null && embedded.getId() == id)) {
                    tweets.remove(pos);
                    notifyItemRemoved(pos);
                }
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
        if (loadingIndex != NO_LOADING) {
            int oldIndex = loadingIndex;
            loadingIndex = NO_LOADING;
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
            final TweetHolder vh = new TweetHolder(parent, settings);
            vh.itemView.setOnClickListener(new OnClickListener() {
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
            final Footer footer = new Footer(parent, settings, false);
            footer.loadBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = footer.getLayoutPosition();
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
                        boolean success = itemClickListener.onHolderClick(sinceId, maxId, position);
                        if (success) {
                            footer.setLoading(true);
                            loadingIndex = position;
                        }
                    }
                }
            });
            return footer;
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        Tweet tweet = tweets.get(index);
        if (holder instanceof TweetHolder && tweet != null) {
            TweetHolder tweetItem = (TweetHolder) holder;
            User user = tweet.getUser();
            if (tweet.getEmbeddedTweet() != null) {
                tweetItem.textViews[5].setText(user.getScreenname());
                tweetItem.textViews[5].setVisibility(VISIBLE);
                tweetItem.rtUser.setVisibility(VISIBLE);
                tweet = tweet.getEmbeddedTweet();
                user = tweet.getUser();
            } else {
                tweetItem.textViews[5].setVisibility(INVISIBLE);
                tweetItem.rtUser.setVisibility(INVISIBLE);
            }
            Spanned text = Tagger.makeTextWithLinks(tweet.getTweet(), settings.getHighlightColor());
            tweetItem.textViews[2].setText(text);
            tweetItem.textViews[0].setText(user.getUsername());
            tweetItem.textViews[1].setText(user.getScreenname());
            tweetItem.textViews[3].setText(NUM_FORMAT.format(tweet.getRetweetCount()));
            tweetItem.textViews[4].setText(NUM_FORMAT.format(tweet.getFavoriteCount()));
            tweetItem.textViews[6].setText(formatCreationTime(tweet.getTime()));

            if (tweet.retweeted()) {
                tweetItem.rtIcon.setColorFilter(settings.getRetweetIconColor(), SRC_IN);
            } else {
                tweetItem.rtIcon.setColorFilter(settings.getIconColor(), SRC_IN);
            }
            if (tweet.favored()) {
                tweetItem.favIcon.setColorFilter(settings.getFavoriteIconColor(), SRC_IN);
            } else {
                tweetItem.favIcon.setColorFilter(settings.getIconColor(), SRC_IN);
            }
            if (user.isVerified()) {
                tweetItem.verifiedIcon.setVisibility(VISIBLE);
            } else {
                tweetItem.verifiedIcon.setVisibility(GONE);
            }
            if (user.isLocked()) {
                tweetItem.lockedIcon.setVisibility(VISIBLE);
            } else {
                tweetItem.lockedIcon.setVisibility(GONE);
            }
            if (settings.imagesEnabled() && user.hasProfileImage()) {
                String pbLink = user.getImageLink();
                if (!user.hasDefaultProfileImage())
                    pbLink += settings.getImageSuffix();
                Picasso.get().load(pbLink).transform(new RoundedCornersTransformation(2, 0))
                        .error(R.drawable.no_image).into(tweetItem.profile);
            } else {
                tweetItem.profile.setImageResource(0);
            }
        } else if (holder instanceof Footer) {
            Footer footer = (Footer) holder;
            footer.setLoading(loadingIndex != NO_LOADING);
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
         * @return true if click was handled
         */
        boolean onHolderClick(long sinceId, long maxId, int pos);
    }
}