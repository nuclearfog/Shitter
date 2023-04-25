package org.nuclearfog.twidda.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.helper.lists.Statuses;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.adapter.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.holder.PlaceHolder;
import org.nuclearfog.twidda.ui.adapter.holder.StatusHolder;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter to show statuses
 *
 * @author nuclearfog
 * @see StatusFragment
 */
public class StatusAdapter extends Adapter<ViewHolder> implements OnHolderClickListener {

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

	private TextEmojiLoader emojiLoader;
	private StatusSelectListener listener;
	private GlobalSettings settings;
	private Picasso picasso;

	private Statuses items;
	private int loadingIndex;

	/**
	 * @param itemClickListener listener for item click
	 */
	public StatusAdapter(Context context, StatusSelectListener itemClickListener) {
		settings = GlobalSettings.getInstance(context);
		picasso = PicassoBuilder.get(context);
		emojiLoader = new TextEmojiLoader(context);
		loadingIndex = NO_LOADING;
		items = new Statuses();
		this.listener = itemClickListener;
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public int getItemViewType(int index) {
		if (items.get(index) == null)
			return VIEW_PLACEHOLDER;
		return VIEW_STATUS;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == VIEW_STATUS) {
			return new StatusHolder(parent, settings, picasso, emojiLoader, this);
		} else {
			return new PlaceHolder(parent, settings, false, this);
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
		if (holder instanceof StatusHolder) {
			Status status = items.get(index);
			if (status != null) {
				StatusHolder statusHolder = ((StatusHolder) holder);
				statusHolder.setContent(status);
			}
		} else if (holder instanceof PlaceHolder) {
			PlaceHolder placeHolder = (PlaceHolder) holder;
			placeHolder.setLoading(loadingIndex == index);
		}
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		long minId = 0L;
		long maxId = 0L;
		if (index == 0) {
			minId = items.getMinId();
		} else if (index == items.size() - 1) {
			maxId = items.getMaxId();
		} else {
			Status status = items.get(index + 1);
			if (status != null) {
				minId = status.getId();
			}
			status = items.get(index - 1);
			if (status != null) {
				maxId = status.getId();
			}
		}
		boolean success = listener.onPlaceholderClick(minId, maxId, index);
		if (success) {
			loadingIndex = index;
			return true;
		}
		return false;
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		if (type == OnHolderClickListener.STATUS_CLICK) {
			Status status = items.get(position);
			if (status != null) {
				listener.onStatusSelected(status);
			}
		}
	}

	/**
	 * get adapter items
	 *
	 * @return item array
	 */
	public Statuses getItems() {
		return new Statuses(items);
	}

	/**
	 * Insert data at specific index of the list
	 *
	 * @param newItems list of statuses to insert
	 * @param index    position to insert
	 */
	public void addItems(@NonNull Statuses newItems, int index) {
		disableLoading();
		if (newItems.size() > MIN_COUNT) {
			if (items.isEmpty() || items.get(index) != null) {
				// Add placeholder
				items.add(index, null);
				notifyItemInserted(index);
			}
		} else if (!items.isEmpty() && items.get(index) == null) {
			// remove placeholder
			items.remove(index);
			notifyItemRemoved(index);
		}
		if (!newItems.isEmpty()) {
			items.addAll(index, newItems);
			notifyItemRangeInserted(index, newItems.size());
		}
	}

	/**
	 * Replace all items in the list
	 *
	 * @param newItems list of statuses to add
	 */
	public void replaceItems(@NonNull Statuses newItems) {
		items.replaceAll(newItems);
		if (items.size() > MIN_COUNT && items.getMaxId() != Statuses.NO_ID && items.peekLast() != null)
			items.add(null);
		loadingIndex = NO_LOADING;
		notifyDataSetChanged();
	}

	/**
	 * update a single item
	 *
	 * @param status status to update
	 */
	public void updateItem(Status status) {
		int index = items.indexOf(status);
		if (index >= 0) {
			items.set(index, status);
			notifyItemChanged(index);
		}
	}

	/**
	 * Remove specific status from list
	 *
	 * @param id ID of the status
	 */
	public void removeItem(long id) {
		for (int pos = items.size() - 1; pos >= 0; pos--) {
			Status status = items.get(pos);
			if (status != null) {
				Status embedded = status.getEmbeddedStatus();
				// remove status and any repost of it
				if (status.getId() == id || (embedded != null && embedded.getId() == id)) {
					items.remove(pos);
					notifyItemRemoved(pos);
				}
			}
		}
	}

	/**
	 * clear all data from adapter
	 */
	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}

	/**
	 * get Id of the first status
	 *
	 * @return status ID
	 */
	public long getTopItemId() {
		if (!items.isEmpty() && items.get(0) != null) {
			Status status = items.get(0);
			if (status != null) {
				return status.getId();
			}
		}
		return 0L;
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