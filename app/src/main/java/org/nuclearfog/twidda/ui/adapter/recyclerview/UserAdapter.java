package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.lists.Users;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.PlaceHolder;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.UserHolder;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter implementation to show users
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.fragments.UserFragment
 */
public class UserAdapter extends Adapter<ViewHolder> implements OnHolderClickListener {

	/**
	 * index of {@link #loadingIndex} if no index is defined
	 */
	private static final int NO_LOADING = -1;

	/**
	 * View type for an user item
	 */
	private static final int ITEM_USER = 0;

	/**
	 * View type for a placeholder item
	 */
	private static final int ITEM_GAP = 1;

	/**
	 * index indicator used to replace the whole list with new items
	 */
	public static final int CLEAR_LIST = -1;

	private UserClickListener listener;
	private boolean enableDelete;

	private Users items = new Users();
	private int loadingIndex = NO_LOADING;

	/**
	 * @param listener item click listener
	 */
	public UserAdapter(UserClickListener listener) {
		this.listener = listener;
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public int getItemViewType(int index) {
		if (items.get(index) == null)
			return ITEM_GAP;
		return ITEM_USER;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == ITEM_USER) {
			return new UserHolder(parent, this, false, enableDelete);
		} else {
			return new PlaceHolder(parent, this, false);
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
		if (holder instanceof UserHolder) {
			User user = items.get(index);
			if (user != null) {
				((UserHolder) holder).setContent(user);
			}
		} else if (holder instanceof PlaceHolder) {
			PlaceHolder placeHolder = (PlaceHolder) holder;
			placeHolder.setLoading(loadingIndex == index);
		}
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		boolean actionPerformed = listener.onPlaceholderClick(items.getNextCursor(), index);
		if (actionPerformed) {
			loadingIndex = index;
			return true;
		}
		return false;
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		switch (type) {
			case OnHolderClickListener.USER_CLICK:
				User user = items.get(position);
				if (user != null) {
					listener.onUserClick(user);
				}
				break;

			case OnHolderClickListener.USER_REMOVE:
				user = items.get(position);
				if (user != null) {
					listener.onDelete(user);
				}
				break;
		}
	}

	/**
	 * insert an user list depending on cursor to the top or bottom
	 *
	 * @param newUsers new userlist
	 */
	public void addItems(@NonNull Users newUsers, int index) {
		disableLoading();
		if (index < 0) {
			items.replaceAll(newUsers);
			if (items.getNextCursor() != 0L) {
				items.add(null);
			}
			notifyDataSetChanged();
		} else {
			items.addAll(index, newUsers);
			if (items.getNextCursor() != 0L && items.peekLast() != null) {
				items.add(null);
				notifyItemRangeInserted(index, newUsers.size() + 1);
			} else if (items.getNextCursor() == 0L && items.peekLast() == null) {
				items.pollLast();
				notifyItemRangeInserted(index, newUsers.size() - 1);
			} else if (!newUsers.isEmpty()) {
				notifyItemRangeInserted(index, newUsers.size());
			}
		}
	}

	/**
	 * replace all user items
	 */
	public void setItems(Users newUsers) {
		items.replaceAll(newUsers);
		if (items.getNextCursor() != 0L && items.peekLast() != null) {
			items.add(null);
		}
		notifyDataSetChanged();
	}

	/**
	 * get adapter items
	 *
	 * @return user items
	 */
	public Users getItems() {
		return new Users(items);
	}

	/**
	 * update user information
	 *
	 * @param user User update
	 */
	public void updateItem(User user) {
		int index = items.indexOf(user);
		if (index >= 0) {
			items.set(index, user);
			notifyItemChanged(index);
		}
	}

	/**
	 * remove user from adapter
	 *
	 * @param user screen name of the user to remove
	 */
	public void removeItem(User user) {
		int index = items.indexOf(user);
		if (index >= 0) {
			items.remove(index);
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
	 * @return true if adapter is empty
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
	 * enable delete button
	 *
	 * @param enableDelete true to enable delete button
	 */
	public void enableDeleteButton(boolean enableDelete) {
		this.enableDelete = enableDelete;
	}

	/**
	 * Listener for list click
	 */
	public interface UserClickListener {

		/**
		 * handle list item click
		 *
		 * @param user user item
		 */
		void onUserClick(User user);

		/**
		 * handle placeholder click
		 *
		 * @param cursor next cursor of the list
		 * @param index  index of the placeholder
		 * @return true if click was handled
		 */
		boolean onPlaceholderClick(long cursor, int index);

		/**
		 * remove user from a list
		 *
		 * @param user user to remove from the list
		 */
		void onDelete(User user);
	}
}