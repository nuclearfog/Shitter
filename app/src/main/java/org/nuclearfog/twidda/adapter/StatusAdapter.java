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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.holder.PlaceHolder;
import org.nuclearfog.twidda.adapter.holder.StatusHolder;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter to show statuses
 *
 * @author nuclearfog
 * @see StatusFragment
 */
public class StatusAdapter extends Adapter<ViewHolder> {

	/**
	 * index of {@link #loadingIndex} if no index is defined
	 */
	private static final int NO_LOADING = -1;

	/**
	 * View type for a status item
	 */
	private static final int VIEW_STATUS = 0;

	/**
	 * View type for a placeholder item
	 */
	private static final int VIEW_PLACEHOLDER = 1;

	/**
	 * Minimum count of new statuses to insert a placeholder.
	 */
	private static final int MIN_COUNT = 2;

	/**
	 * Locale specific number format
	 */
	private static final NumberFormat NUM_FORMAT = NumberFormat.getIntegerInstance();


	private StatusSelectListener itemClickListener;
	private GlobalSettings settings;
	private Resources resources;
	private Picasso picasso;

	private final List<Status> statuses = new LinkedList<>();
	private int loadingIndex = NO_LOADING;

	/**
	 * @param itemClickListener listener for item click
	 */
	public StatusAdapter(Context context, StatusSelectListener itemClickListener) {
		this.itemClickListener = itemClickListener;
		settings = GlobalSettings.getInstance(context);
		picasso = PicassoBuilder.get(context);
		resources = context.getResources();
	}


	@Override
	public long getItemId(int index) {
		Status status = statuses.get(index);
		if (status != null)
			return status.getId();
		return NO_ID;
	}


	@Override
	public int getItemCount() {
		return statuses.size();
	}


	@Override
	public int getItemViewType(int index) {
		if (statuses.get(index) == null)
			return VIEW_PLACEHOLDER;
		return VIEW_STATUS;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == VIEW_STATUS) {
			final StatusHolder vh = new StatusHolder(parent, settings);
			vh.itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = vh.getLayoutPosition();
					if (position != NO_POSITION) {
						Status status = statuses.get(position);
						if (status != null) {
							itemClickListener.onStatusSelected(status);
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
							Status status = statuses.get(position + 1);
							if (status != null) {
								sinceId = status.getId();
							}
						} else if (position == statuses.size() - 1) {
							Status status = statuses.get(position - 1);
							if (status != null) {
								maxId = status.getId() - 1;
							}
						} else {
							Status status = statuses.get(position + 1);
							if (status != null) {
								sinceId = status.getId();
							}
							status = statuses.get(position - 1);
							if (status != null) {
								maxId = status.getId() - 1;
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
		if (holder instanceof StatusHolder) {
			Status status = statuses.get(index);
			if (status != null) {
				StatusHolder statusHolder = (StatusHolder) holder;
				User user = status.getAuthor();
				if (status.getEmbeddedStatus() != null) {
					statusHolder.reposter.setText(user.getScreenname());
					statusHolder.reposter.setVisibility(VISIBLE);
					statusHolder.rpUser.setVisibility(VISIBLE);
					status = status.getEmbeddedStatus();
					user = status.getAuthor();
				} else {
					statusHolder.reposter.setVisibility(GONE);
					statusHolder.rpUser.setVisibility(GONE);
				}
				statusHolder.username.setText(user.getUsername());
				statusHolder.screenname.setText(user.getScreenname());
				statusHolder.repost.setText(NUM_FORMAT.format(status.getRepostCount()));
				statusHolder.favorite.setText(NUM_FORMAT.format(status.getFavoriteCount()));
				statusHolder.created.setText(StringTools.formatCreationTime(resources, status.getTimestamp()));
				if (!status.getText().isEmpty()) {
					Spanned text = Tagger.makeTextWithLinks(status.getText(), settings.getHighlightColor());
					statusHolder.text.setText(text);
					statusHolder.text.setVisibility(VISIBLE);
				} else {
					statusHolder.text.setVisibility(GONE);
				}
				if (status.isReposted()) {
					statusHolder.rtIcon.setColorFilter(settings.getRepostIconColor());
				} else {
					statusHolder.rtIcon.setColorFilter(settings.getIconColor());
				}
				if (status.isFavorited()) {
					statusHolder.favIcon.setColorFilter(settings.getFavoriteIconColor());
				} else {
					statusHolder.favIcon.setColorFilter(settings.getIconColor());
				}
				if (user.isVerified()) {
					statusHolder.verifiedIcon.setVisibility(VISIBLE);
				} else {
					statusHolder.verifiedIcon.setVisibility(GONE);
				}
				if (user.isProtected()) {
					statusHolder.lockedIcon.setVisibility(VISIBLE);
				} else {
					statusHolder.lockedIcon.setVisibility(GONE);
				}
				if (settings.imagesEnabled() && !user.getImageUrl().isEmpty()) {
					String profileImageUrl;
					if (!user.hasDefaultProfileImage()) {
						profileImageUrl = StringTools.buildImageLink(user.getImageUrl(), settings.getImageSuffix());
					} else {
						profileImageUrl = user.getImageUrl();
					}
					picasso.load(profileImageUrl).transform(new RoundedCornersTransformation(2, 0)).error(R.drawable.no_image).into(statusHolder.profile);
				} else {
					statusHolder.profile.setImageResource(0);
				}
				if (status.getRepliedStatusId() > 0) {
					statusHolder.replyIcon.setVisibility(VISIBLE);
					statusHolder.replyname.setVisibility(VISIBLE);
					statusHolder.replyname.setText(status.getReplyName());
				} else {
					statusHolder.replyIcon.setVisibility(GONE);
					statusHolder.replyname.setVisibility(GONE);
				}
				if (settings.statusIndicatorsEnabled()) {
					if (status.getLocationName() != null && !status.getLocationName().isEmpty()) {
						statusHolder.location.setVisibility(VISIBLE);
					} else {
						statusHolder.location.setVisibility(GONE);
					}
					if (status.getMediaType() != Status.MEDIA_NONE) {
						if (status.getMediaType() == Status.MEDIA_PHOTO) {
							statusHolder.media.setImageResource(R.drawable.image);
						} else if (status.getMediaType() == Status.MEDIA_VIDEO) {
							statusHolder.media.setImageResource(R.drawable.video);
						} else if (status.getMediaType() == Status.MEDIA_GIF) {
							statusHolder.media.setImageResource(R.drawable.gif);
						}
						statusHolder.media.setColorFilter(settings.getIconColor());
						statusHolder.media.setVisibility(VISIBLE);
					} else {
						statusHolder.media.setVisibility(GONE);
					}
				} else {
					statusHolder.location.setVisibility(GONE);
					statusHolder.media.setVisibility(GONE);
				}
			}
		} else if (holder instanceof PlaceHolder) {
			PlaceHolder placeHolder = (PlaceHolder) holder;
			placeHolder.setLoading(loadingIndex == index);
		}
	}

	/**
	 * Insert data at specific index of the list
	 *
	 * @param statuses list of statuses to insert
	 * @param index    position to insert
	 */
	public void addItems(@NonNull List<Status> statuses, int index) {
		disableLoading();
		if (statuses.size() > MIN_COUNT) {
			if (this.statuses.isEmpty() || this.statuses.get(index) != null) {
				// Add placeholder
				this.statuses.add(index, null);
				notifyItemInserted(index);
			}
		} else {
			if (!this.statuses.isEmpty() && this.statuses.get(index) == null) {
				// remove placeholder
				this.statuses.remove(index);
				notifyItemRemoved(index);
			}
		}
		if (!statuses.isEmpty()) {
			this.statuses.addAll(index, statuses);
			notifyItemRangeInserted(index, statuses.size());
		}
	}

	/**
	 * Replace all items in the list
	 *
	 * @param statuses list of statuses to add
	 */
	public void replaceItems(@NonNull List<Status> statuses) {
		this.statuses.clear();
		this.statuses.addAll(statuses);
		if (statuses.size() > MIN_COUNT) {
			this.statuses.add(null);
		}
		loadingIndex = NO_LOADING;
		notifyDataSetChanged();
	}

	/**
	 * update a single item
	 *
	 * @param status status to update
	 */
	public void updateItem(Status status) {
		int index = statuses.indexOf(status);
		if (index >= 0) {
			statuses.set(index, status);
			notifyItemChanged(index);
		}
	}

	/**
	 * Remove specific status from list
	 *
	 * @param id ID of the status
	 */
	public void removeItem(long id) {
		for (int pos = statuses.size() - 1; pos >= 0; pos--) {
			Status status = statuses.get(pos);
			if (status != null) {
				Status embedded = status.getEmbeddedStatus();
				// remove status and any repost of it
				if (status.getId() == id || (embedded != null && embedded.getId() == id)) {
					statuses.remove(pos);
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
		return statuses.isEmpty();
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

	/**
	 * Listener for status click
	 */
	public interface StatusSelectListener {

		/**
		 * handle click action
		 *
		 * @param status clicked status
		 */
		void onStatusSelected(Status status);

		/**
		 * called then the user clicks on the placeholder
		 *
		 * @param minId the highest status ID below the placeholder or '0' if there is none
		 * @param maxId the lowest status ID above the placeholder or '0' if there is none
		 * @param pos   position of the placeholder
		 * @return true  if click was handled
		 */
		boolean onPlaceholderClick(long minId, long maxId, int pos);
	}
}