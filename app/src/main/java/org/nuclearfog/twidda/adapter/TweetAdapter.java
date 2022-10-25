package org.nuclearfog.twidda.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_ID;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.content.res.Resources;
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
import org.nuclearfog.twidda.adapter.holder.PlaceHolder;
import org.nuclearfog.twidda.adapter.holder.TweetHolder;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Tweet;
import org.nuclearfog.twidda.model.User;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter to show tweets
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.fragments.TweetFragment
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
	private Resources resources;
	private Picasso picasso;

	private final List<Tweet> tweets = new LinkedList<>();
	private int loadingIndex = NO_LOADING;

	/**
	 * @param itemClickListener listener for item click
	 */
	public TweetAdapter(Context context, TweetClickListener itemClickListener) {
		this.itemClickListener = itemClickListener;
		settings = GlobalSettings.getInstance(context);
		picasso = PicassoBuilder.get(context);
		resources = context.getResources();
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
					if (position != NO_POSITION) {
						Tweet tweet = tweets.get(position);
						if (tweet != null) {
							itemClickListener.onTweetClick(tweet);
						}
					}
				}
			});
			return vh;
		} else {
			final PlaceHolder placeHolder = new PlaceHolder(parent, settings, false);
			placeHolder.loadBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = placeHolder.getLayoutPosition();
					if (position != NO_POSITION) {
						long sinceId = 0;
						long maxId = 0;
						if (position == 0) {
							Tweet tweet = tweets.get(position + 1);
							if (tweet != null) {
								sinceId = tweet.getId();
							}
						} else if (position == tweets.size() - 1) {
							Tweet tweet = tweets.get(position - 1);
							if (tweet != null) {
								maxId = tweet.getId() - 1;
							}
						} else {
							Tweet tweet = tweets.get(position + 1);
							if (tweet != null) {
								sinceId = tweet.getId();
							}
							tweet = tweets.get(position - 1);
							if (tweet != null) {
								maxId = tweet.getId() - 1;
							}
						}
						boolean success = itemClickListener.onPlaceholderClick(sinceId, maxId, position);
						if (success) {
							placeHolder.setLoading(true);
							loadingIndex = position;
						}
					}
				}
			});
			return placeHolder;
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
		if (holder instanceof TweetHolder) {
			Tweet tweet = tweets.get(index);
			if (tweet != null) {
				TweetHolder tweetItem = (TweetHolder) holder;
				User user = tweet.getAuthor();
				if (tweet.getEmbeddedTweet() != null) {
					tweetItem.retweeter.setText(user.getScreenname());
					tweetItem.retweeter.setVisibility(VISIBLE);
					tweetItem.rtUser.setVisibility(VISIBLE);
					tweet = tweet.getEmbeddedTweet();
					user = tweet.getAuthor();
				} else {
					tweetItem.retweeter.setVisibility(GONE);
					tweetItem.rtUser.setVisibility(GONE);
				}
				tweetItem.username.setText(user.getUsername());
				tweetItem.screenname.setText(user.getScreenname());
				tweetItem.retweet.setText(NUM_FORMAT.format(tweet.getRetweetCount()));
				tweetItem.favorite.setText(NUM_FORMAT.format(tweet.getFavoriteCount()));
				tweetItem.created.setText(StringTools.formatCreationTime(resources, tweet.getTimestamp()));
				if (!tweet.getText().isEmpty()) {
					Spanned text = Tagger.makeTextWithLinks(tweet.getText(), settings.getHighlightColor());
					tweetItem.tweettext.setText(text);
					tweetItem.tweettext.setVisibility(VISIBLE);
				} else {
					tweetItem.tweettext.setVisibility(GONE);
				}
				if (tweet.isRetweeted()) {
					tweetItem.rtIcon.setColorFilter(settings.getRetweetIconColor());
				} else {
					tweetItem.rtIcon.setColorFilter(settings.getIconColor());
				}
				if (tweet.isFavorited()) {
					tweetItem.favIcon.setColorFilter(settings.getFavoriteIconColor());
				} else {
					tweetItem.favIcon.setColorFilter(settings.getIconColor());
				}
				if (user.isVerified()) {
					tweetItem.verifiedIcon.setVisibility(VISIBLE);
				} else {
					tweetItem.verifiedIcon.setVisibility(GONE);
				}
				if (user.isProtected()) {
					tweetItem.lockedIcon.setVisibility(VISIBLE);
				} else {
					tweetItem.lockedIcon.setVisibility(GONE);
				}
				if (settings.imagesEnabled() && !user.getImageUrl().isEmpty()) {
					String profileImageUrl;
					if (!user.hasDefaultProfileImage()) {
						profileImageUrl = StringTools.buildImageLink(user.getImageUrl(), settings.getImageSuffix());
					} else {
						profileImageUrl = user.getImageUrl();
					}
					picasso.load(profileImageUrl).transform(new RoundedCornersTransformation(2, 0)).error(R.drawable.no_image).into(tweetItem.profile);
				} else {
					tweetItem.profile.setImageResource(0);
				}
				if (tweet.getRepliedTweetId() > 0) {
					tweetItem.replyIcon.setVisibility(VISIBLE);
					tweetItem.replyname.setVisibility(VISIBLE);
					tweetItem.replyname.setText(tweet.getReplyName());
				} else {
					tweetItem.replyIcon.setVisibility(GONE);
					tweetItem.replyname.setVisibility(GONE);
				}
				if (settings.tweetIndicatorsEnabled()) {
					if (tweet.getLocationName() != null && !tweet.getLocationName().isEmpty()) {
						tweetItem.location.setVisibility(VISIBLE);
					} else {
						tweetItem.location.setVisibility(GONE);
					}
					if (tweet.getMediaType() != Tweet.MEDIA_NONE) {
						if (tweet.getMediaType() == Tweet.MEDIA_PHOTO) {
							tweetItem.media.setImageResource(R.drawable.image);
						} else if (tweet.getMediaType() == Tweet.MEDIA_VIDEO) {
							tweetItem.media.setImageResource(R.drawable.video);
						} else if (tweet.getMediaType() == Tweet.MEDIA_GIF) {
							tweetItem.media.setImageResource(R.drawable.gif);
						}
						tweetItem.media.setColorFilter(settings.getIconColor());
						tweetItem.media.setVisibility(VISIBLE);
					} else {
						tweetItem.media.setVisibility(GONE);
					}
				} else {
					tweetItem.location.setVisibility(GONE);
					tweetItem.media.setVisibility(GONE);
				}
			}
		} else if (holder instanceof PlaceHolder) {
			PlaceHolder placeHolder = (PlaceHolder) holder;
			placeHolder.setLoading(loadingIndex == index);
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
		 * called then the user clicks on the placeholder
		 *
		 * @param minId the highest tweet ID below the placeholder or '0' if there is none
		 * @param maxId the lowest tweet ID above the placeholder or '0' if there is none
		 * @param pos   position of the placeholder
		 * @return true  if click was handled
		 */
		boolean onPlaceholderClick(long minId, long maxId, int pos);
	}
}