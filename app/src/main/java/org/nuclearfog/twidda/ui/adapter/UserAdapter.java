package org.nuclearfog.twidda.ui.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_ID;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.backend.helper.Users;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.adapter.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.holder.PlaceHolder;
import org.nuclearfog.twidda.ui.adapter.holder.UserHolder;

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

	private GlobalSettings settings;
	private Picasso picasso;

	private UserClickListener listener;
	private boolean enableDelete;

	private Users users;
	private int loadingIndex;

	/**
	 * @param listener     click listener
	 * @param enableDelete true to enable delete button
	 */
	public UserAdapter(Context context, UserClickListener listener, boolean enableDelete) {
		settings = GlobalSettings.getInstance(context);
		picasso = PicassoBuilder.get(context);
		users = new Users(0L, 0L);
		loadingIndex = NO_LOADING;
		this.enableDelete = enableDelete;
		this.listener = listener;
	}


	@Override
	public int getItemCount() {
		return users.size();
	}


	@Override
	public long getItemId(int index) {
		User user = users.get(index);
		if (user != null)
			return user.getId();
		return NO_ID;
	}


	@Override
	public int getItemViewType(int index) {
		if (users.get(index) == null)
			return ITEM_GAP;
		return ITEM_USER;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == ITEM_USER) {
			return new UserHolder(parent, settings, picasso, this, enableDelete);
		} else {
			return new PlaceHolder(parent, settings, false, this);
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
		if (holder instanceof UserHolder) {
			User user = users.get(index);
			if (user != null) {
				((UserHolder) holder).setContent(user);
			}
		} else if (holder instanceof PlaceHolder) {
			PlaceHolder placeHolder = (PlaceHolder) holder;
			placeHolder.setLoading(loadingIndex == index);
		}
	}


	@Override
	public boolean onPlaceholderClick(int position) {
		boolean actionPerformed = listener.onPlaceholderClick(users.getNext());
		if (actionPerformed) {
			loadingIndex = position;
			return true;
		}
		return false;
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		switch (type) {
			case OnHolderClickListener.USER_CLICK:
				User user = users.get(position);
				if (user != null) {
					listener.onUserClick(user);
				}
				break;

			case OnHolderClickListener.USER_REMOVE:
				user = users.get(position);
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
	public void addItems(@NonNull Users newUsers) {
		disableLoading();
		// add empty list
		if (newUsers.isEmpty()) {
			// remove placeholder if there isn't a next page
			if (!users.isEmpty() && users.peekLast() == null) {
				int end = users.size() - 1;
				users.remove(end);
				notifyItemRemoved(end);
			}
		}
		// add items to the top of the list
		else if (users.isEmpty() || !newUsers.hasPrevious()) {
			users.replace(newUsers);
			// add placeholder if there is a next page
			if (newUsers.hasNext()) {
				users.add(null);
			}
			notifyDataSetChanged();
		}
		// add items to the end of the list
		else {
			int end = users.size() - 1;
			// remove placeholder if there isn't a next page
			if (!newUsers.hasNext()) {
				users.remove(end);
				notifyItemRemoved(end);
			}
			users.addAt(newUsers, end);
			notifyItemRangeInserted(end, newUsers.size());
		}
	}

	/**
	 * update user information
	 *
	 * @param user User update
	 */
	public void updateItem(User user) {
		int index = users.indexOf(user);
		if (index >= 0) {
			users.set(index, user);
			notifyItemChanged(index);
		}
	}

	/**
	 * remove user from adapter
	 *
	 * @param user screen name of the user to remove
	 */
	public void removeItem(User user) {
		int pos = users.indexOf(user);
		if (pos >= 0) {
			users.remove(pos);
			notifyItemRemoved(pos);
		}
	}

	/**
	 * @return true if adapter doesn't contain any items
	 */
	public boolean isEmpty() {
		return users.isEmpty();
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
		 * @return true if click was handled
		 */
		boolean onPlaceholderClick(long cursor);

		/**
		 * remove user from a list
		 *
		 * @param user user to remove from the list
		 */
		void onDelete(User user);
	}
}