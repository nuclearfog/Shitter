package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.model.lists.UserLists;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.PlaceHolder;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.UserlistHolder;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter implementation to show userlists
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.fragments.UserListFragment
 */
public class UserlistAdapter extends Adapter<ViewHolder> implements OnHolderClickListener {

	/**
	 * "index" used to replace the whole list with new items
	 */
	public static final int CLEAR_LIST = -1;

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

	private UserLists items = new UserLists();
	private int loadingIndex = NO_LOADING;

	/**
	 * @param listener item click listener
	 */
	public UserlistAdapter(ListClickListener listener) {
		this.listener = listener;
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public int getItemViewType(int position) {
		if (items.get(position) == null)
			return ITEM_PLACEHOLDER;
		return ITEM_LIST;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == ITEM_LIST) {
			return new UserlistHolder(parent, this);
		} else {
			return new PlaceHolder(parent, this, false);
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
		if (holder instanceof UserlistHolder) {
			UserlistHolder vh = (UserlistHolder) holder;
			UserList item = items.get(index);
			if (item != null) {
				vh.setContent(item);
			}
		} else if (holder instanceof PlaceHolder) {
			PlaceHolder placeHolder = (PlaceHolder) holder;
			placeHolder.setLoading(loadingIndex == index);
		}
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		UserList item = items.get(position);
		if (item != null && type == OnHolderClickListener.LIST_CLICK) {
			listener.onListClick(item);
		}
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		boolean actionPerformed = listener.onPlaceholderClick(items.getNextCursor(), index);
		if (actionPerformed)
			loadingIndex = index;
		return actionPerformed;
	}

	/**
	 * adds new data to the list
	 *
	 * @param newUserlists new items to add
	 * @param index        index where to insert new items
	 */
	public void addItems(UserLists newUserlists, int index) {
		disableLoading();
		if (index < 0) {
			items.replaceAll(newUserlists);
			if (items.getNextCursor() != 0L) {
				// Add placeholder
				items.add(null);
			}
			notifyDataSetChanged();
		} else {
			items.addAll(index, newUserlists);
			if (items.getNextCursor() != 0L && items.peekLast() != null) {
				items.add(null);
				notifyItemRangeInserted(index, newUserlists.size() + 1);
			} else if (items.getNextCursor() == 0L && items.peekLast() == null) {
				items.pollLast();
				notifyItemRangeInserted(index, newUserlists.size() - 1);
			} else {
				notifyItemRangeInserted(index, newUserlists.size());
			}
		}
	}

	/**
	 * replace all adapter items
	 *
	 * @param newUserlists new items to replace old items
	 */
	public void setItems(UserLists newUserlists) {
		items.replaceAll(newUserlists);
		if (items.getNextCursor() != 0L && items.peekLast() != null) {
			// Add placeholder
			items.add(null);
		}
		notifyDataSetChanged();
	}

	/**
	 * get all adapter items
	 *
	 * @return adapter items
	 */
	public UserLists getItems() {
		return new UserLists(items);
	}

	/**
	 * update a single item
	 *
	 * @param list updated list
	 */
	public void updateItem(UserList list) {
		int index = items.indexOf(list);
		if (index >= 0) {
			items.set(index, list);
			notifyItemChanged(index);
		}
	}

	/**
	 * remove user list item from list
	 *
	 * @param itemId user list id to remove
	 */
	public void removeItem(long itemId) {
		int index = items.removeItem(itemId);
		if (index >= 0) {
			notifyItemRemoved(index);
		}
	}

	/**
	 * clear adapter data
	 */
	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}

	/**
	 * @return true if adapter doesn't contain any items
	 */
	public boolean isEmpty() {
		return items.isEmpty();
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
		 * called when the placeholder is clicked
		 *
		 * @param cursor next cursor of the list
		 * @param index  index of the placeholder
		 */
		boolean onPlaceholderClick(long cursor, int index);
	}
}