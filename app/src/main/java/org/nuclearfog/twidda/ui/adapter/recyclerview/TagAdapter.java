package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.model.Tag;
import org.nuclearfog.twidda.model.lists.Tags;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.PlaceHolder;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.TagHolder;
import org.nuclearfog.twidda.ui.fragments.TagFragment;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter implementation to show trends
 *
 * @author nuclearfog
 * @see TagFragment
 */
public class TagAdapter extends Adapter<ViewHolder> implements OnHolderClickListener {

	/**
	 * "index" used to replace the whole list with new items
	 */
	public static final int CLEAR_LIST = -1;

	private static final int TYPE_TREND = 0;

	private static final int TYPE_PLACEHOLDER = 1;

	private static final int NO_LOADING = -1;

	private OnTagClickListener itemClickListener;

	private Tags items = new Tags();
	private int loadingIndex = NO_LOADING;
	private boolean enableDelete = false;

	/**
	 * @param itemClickListener Listener for item click
	 */
	public TagAdapter(OnTagClickListener itemClickListener) {
		this.itemClickListener = itemClickListener;
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public int getItemViewType(int position) {
		if (items.get(position) != null)
			return TYPE_TREND;
		return TYPE_PLACEHOLDER;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == TYPE_TREND) {
			return new TagHolder(parent, this, enableDelete);
		} else {
			return new PlaceHolder(parent, this, false);
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder vh, int index) {
		if (vh instanceof TagHolder) {
			TagHolder holder = (TagHolder) vh;
			Tag tag = items.get(index);
			if (tag != null) {
				holder.setContent(tag, index);
			}
		} else if (vh instanceof PlaceHolder) {
			PlaceHolder placeHolder = (PlaceHolder) vh;
			placeHolder.setLoading(loadingIndex == index);
		}
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		if (type == TAG_CLICK) {
			itemClickListener.onTagClick(items.get(position), OnTagClickListener.SELECT);
		} else if (type == TAG_REMOVE) {
			itemClickListener.onTagClick(items.get(position), OnTagClickListener.REMOVE);
		}
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		boolean actionPerformed = itemClickListener.onPlaceholderClick(items.getNextCursor(), index);
		if (actionPerformed) {
			loadingIndex = index;
		}
		return actionPerformed;
	}

	/**
	 * get adapter items
	 *
	 * @return a copy of the items
	 */
	public Tags getItems() {
		return new Tags(items);
	}

	/**
	 * replace data from list
	 *
	 * @param newItems array of trend items
	 */
	public void addItems(Tags newItems, int index) {
		disableLoading();
		if (index < 0) {
			this.items.replaceAll(newItems);
			if (items.getNextCursor() != 0L) {
				items.add(null);
			}
			notifyDataSetChanged();
		} else {
			items.addAll(index, newItems);
			if (items.getNextCursor() != 0L && items.peekLast() != null) {
				items.add(null);
				notifyItemRangeInserted(index, newItems.size() + 1);
			} else if (items.getNextCursor() == 0L && items.peekLast() == null) {
				items.pollLast();
				notifyItemRangeInserted(index, newItems.size() - 1);
			} else if (!newItems.isEmpty()) {
				notifyItemRangeInserted(index, newItems.size());
			}
		}
	}

	/**
	 * remove item from adapter
	 *
	 * @param tag item to remove
	 */
	public void removeItem(Tag tag) {
		int index = items.indexOf(tag);
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
	 * check if adapter is empty
	 *
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
	 *
	 */
	public void enableDelete() {
		enableDelete = true;
	}

	/**
	 * Listener for tag items
	 */
	public interface OnTagClickListener {

		int SELECT = 1;

		int REMOVE = 2;

		/**
		 * called when a trend item is clicked
		 *
		 * @param tag    tag name
		 * @param action action to take {@link #SELECT,#REMOVE}
		 */
		void onTagClick(Tag tag, int action);

		/**
		 *
		 */
		boolean onPlaceholderClick(long cursor, int index);
	}
}