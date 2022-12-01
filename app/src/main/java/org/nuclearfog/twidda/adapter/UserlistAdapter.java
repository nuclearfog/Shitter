package org.nuclearfog.twidda.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.adapter.holder.PlaceHolder;
import org.nuclearfog.twidda.adapter.holder.PlaceHolder.OnHolderClickListener;
import org.nuclearfog.twidda.adapter.holder.UserlistHolder;
import org.nuclearfog.twidda.adapter.holder.UserlistHolder.OnListClickListener;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter implementation to show userlists
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.fragments.UserListFragment
 */
public class UserlistAdapter extends Adapter<ViewHolder> implements OnListClickListener, OnHolderClickListener {

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

	private ListClickListener listener;
	private GlobalSettings settings;
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
			return new UserlistHolder(parent, settings, picasso, this);
		} else {
			PlaceHolder placeHolder = new PlaceHolder(parent, settings, false);
			placeHolder.setOnHolderClickListener(this);
			return placeHolder;
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
		if (holder instanceof UserlistHolder) {
			UserlistHolder vh = (UserlistHolder) holder;
			UserList item = userlists.get(index);
			if (item != null) {
				vh.setContent(item);
			}
		} else if (holder instanceof PlaceHolder) {
			PlaceHolder placeHolder = (PlaceHolder) holder;
			placeHolder.setLoading(loadingIndex == index);
		}
	}


	@Override
	public void onUserlistClick(int position, int type) {
		UserList item = userlists.get(position);
		if (item != null) {
			switch (type) {
				case OnListClickListener.LIST_CLICK:
					listener.onListClick(item);
					break;

				case OnListClickListener.PROFILE_CLICK:
					listener.onProfileClick(item.getListOwner());
					break;
			}
		}
	}


	@Override
	public boolean onHolderClick(int position) {
		boolean actionPerformed = listener.onPlaceholderClick(userlists.getNext());
		if (actionPerformed)
			loadingIndex = position;
		return actionPerformed;
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