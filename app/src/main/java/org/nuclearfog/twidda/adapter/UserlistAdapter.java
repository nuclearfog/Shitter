package org.nuclearfog.twidda.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static org.nuclearfog.twidda.backend.utils.StringTools.formatCreationTime;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.holder.PlaceHolder;
import org.nuclearfog.twidda.adapter.holder.UserlistHolder;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

import java.text.NumberFormat;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter implementation to show userlists
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.fragments.UserListFragment
 */
public class UserlistAdapter extends Adapter<ViewHolder> {

	/**
	 * indicator if there is no loading progress
	 */
	private static final int NO_LOADING = -1;

	/**
	 * View type for a placeholder
	 */
	private static final int ITEM_PLACEHOLDER = 0;

	/**
	 * View type for an userlist item
	 */
	private static final int ITEM_LIST = 1;

	/**
	 * locale specific number format
	 */
	private static final NumberFormat NUM_FORMAT = NumberFormat.getIntegerInstance();

	private ListClickListener listener;
	private GlobalSettings settings;
	private Resources resources;
	private Picasso picasso;

	private UserLists userlists = new UserLists(0L, 0L);
	private int loadingIndex = NO_LOADING;

	/**
	 * @param listener item click listener
	 */
	public UserlistAdapter(Context context, ListClickListener listener) {
		this.listener = listener;
		settings = GlobalSettings.getInstance(context);
		picasso = PicassoBuilder.get(context);
		resources = context.getResources();
	}


	@Override
	public int getItemCount() {
		return userlists.size();
	}


	@Override
	public int getItemViewType(int position) {
		if (userlists.get(position) == null)
			return ITEM_PLACEHOLDER;
		return ITEM_LIST;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == ITEM_LIST) {
			final UserlistHolder itemHolder = new UserlistHolder(parent, settings);
			itemHolder.profile.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = itemHolder.getLayoutPosition();
					if (position != NO_POSITION) {
						UserList item = userlists.get(position);
						if (item != null) {
							listener.onProfileClick(item.getListOwner());
						}
					}
				}
			});
			itemHolder.itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = itemHolder.getLayoutPosition();
					if (position != NO_POSITION) {
						UserList item = userlists.get(position);
						if (item != null) {
							listener.onListClick(item);
						}
					}
				}
			});
			return itemHolder;
		} else {
			final PlaceHolder placeHolder = new PlaceHolder(parent, settings, false);
			placeHolder.loadBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = placeHolder.getLayoutPosition();
					if (position != NO_POSITION) {
						boolean actionPerformed = listener.onPlaceholderClick(userlists.getNext());
						if (actionPerformed) {
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
		if (holder instanceof UserlistHolder) {
			UserlistHolder vh = (UserlistHolder) holder;
			UserList item = userlists.get(index);
			if (item != null) {
				User owner = item.getListOwner();
				vh.title.setText(item.getTitle());
				vh.description.setText(item.getDescription());
				vh.username.setText(owner.getUsername());
				vh.screenname.setText(owner.getScreenname());
				vh.date.setText(formatCreationTime(resources, item.getTimestamp()));
				vh.member.setText(NUM_FORMAT.format(item.getMemberCount()));
				vh.subscriber.setText(NUM_FORMAT.format(item.getSubscriberCount()));
				if (settings.imagesEnabled() && !owner.getImageUrl().isEmpty()) {
					String profileImageUrl;
					if (!owner.hasDefaultProfileImage()) {
						profileImageUrl = StringTools.buildImageLink(owner.getImageUrl(), settings.getImageSuffix());
					} else {
						profileImageUrl = owner.getImageUrl();
					}
					picasso.load(profileImageUrl).transform(new RoundedCornersTransformation(3, 0)).error(R.drawable.no_image).into(vh.profile);
				} else {
					vh.profile.setImageResource(0);
				}
				if (!item.getListOwner().isCurrentUser() && item.isFollowing()) {
					vh.follow.setVisibility(VISIBLE);
					vh.followList.setVisibility(VISIBLE);
				} else {
					vh.follow.setVisibility(GONE);
					vh.followList.setVisibility(GONE);
				}
				if (owner.isVerified()) {
					vh.verified.setVisibility(VISIBLE);
				} else {
					vh.verified.setVisibility(GONE);
				}
				if (owner.isProtected()) {
					vh.locked.setVisibility(VISIBLE);
				} else {
					vh.locked.setVisibility(GONE);
				}
				if (item.isPrivate()) {
					vh.privateList.setVisibility(VISIBLE);
				} else {
					vh.privateList.setVisibility(GONE);
				}
			}
		} else if (holder instanceof PlaceHolder) {
			PlaceHolder placeHolder = (PlaceHolder) holder;
			placeHolder.setLoading(loadingIndex == index);
		}
	}

	/**
	 * adds new data to the list
	 *
	 * @param newUserlists new list to add
	 */
	public void addItems(UserLists newUserlists) {
		disableLoading();
		if (newUserlists.isEmpty()) {
			if (!userlists.isEmpty() && userlists.peekLast() == null) {
				// remove placeholder
				int end = userlists.size() - 1;
				userlists.remove(end);
				notifyItemRemoved(end);
			}
		} else if (userlists.isEmpty() || !newUserlists.hasPrevious()) {
			userlists.replace(newUserlists);
			if (userlists.hasNext()) {
				// Add placeholder
				userlists.add(null);
			}
			notifyDataSetChanged();
		} else {
			int end = userlists.size() - 1;
			if (!userlists.hasNext()) {
				// remove placeholder
				userlists.remove(end);
				notifyItemRemoved(end);
			}
			userlists.addAt(newUserlists, end);
			notifyItemRangeInserted(end, newUserlists.size());
		}
	}

	/**
	 * update a single item
	 *
	 * @param list updated list
	 */
	public void updateItem(UserList list) {
		int index = userlists.indexOf(list);
		if (index >= 0) {
			userlists.set(index, list);
			notifyItemChanged(index);
		}
	}

	/**
	 * remove user list item from list
	 *
	 * @param itemId user list id to remove
	 */
	public void removeItem(long itemId) {
		int index = userlists.removeItem(itemId);
		if (index >= 0) {
			notifyItemRemoved(index);
		}
	}

	/**
	 * disable placeholder view loading animation
	 */
	public void disableLoading() {
		if (loadingIndex != NO_LOADING) {
			int oldIndex = loadingIndex;
			loadingIndex = NO_LOADING;
			notifyItemChanged(oldIndex);
		}
	}

	/**
	 * Listener for an item
	 */
	public interface ListClickListener {

		/**
		 * called when an item is clicked
		 *
		 * @param listItem Item data and information
		 */
		void onListClick(UserList listItem);

		/**
		 * called when the profile image of the owner was clicked
		 *
		 * @param user user information
		 */
		void onProfileClick(User user);

		/**
		 * called when the placeholder is clicked
		 *
		 * @param cursor next cursor of the list
		 */
		boolean onPlaceholderClick(long cursor);
	}
}