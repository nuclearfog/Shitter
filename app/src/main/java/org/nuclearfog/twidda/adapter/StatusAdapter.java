package org.nuclearfog.twidda.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_ID;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.adapter.holder.PlaceHolder;
import org.nuclearfog.twidda.adapter.holder.PlaceHolder.OnHolderClickListener;
import org.nuclearfog.twidda.adapter.holder.StatusHolder;
import org.nuclearfog.twidda.adapter.holder.StatusHolder.OnStatusClickListener;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;

import java.util.LinkedList;
import java.util.List;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter to show statuses
 *
 * @author nuclearfog
 * @see StatusFragment
 */
public class StatusAdapter extends Adapter<ViewHolder> implements OnStatusClickListener, OnHolderClickListener {

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


	private StatusSelectListener itemClickListener;
	private GlobalSettings settings;
	private Picasso picasso;

	private final List<Status> items = new LinkedList<>();
	private int loadingIndex = NO_LOADING;

	/**
	 * @param itemClickListener listener for item click
	 */
	public StatusAdapter(Context context, StatusSelectListener itemClickListener) {
		this.itemClickListener = itemClickListener;
		settings = GlobalSettings.getInstance(context);
		picasso = PicassoBuilder.get(context);
	}


	@Override
	public long getItemId(int index) {
		Status status = items.get(index);
		if (status != null)
			return status.getId();
		return NO_ID;
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
			StatusHolder vh = new StatusHolder(parent, settings, picasso);
			vh.setOnStatusClickListener(this);
			return vh;
		} else {
			PlaceHolder placeHolder = new PlaceHolder(parent, settings, false);
			placeHolder.setOnHolderClickListener(this);
			return placeHolder;
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
		if (holder instanceof StatusHolder) {
			Status status = items.get(index);
			if (status != null) {
				((StatusHolder) holder).setContent(status);
			}
		} else if (holder instanceof PlaceHolder) {
			PlaceHolder placeHolder = (PlaceHolder) holder;
			placeHolder.setLoading(loadingIndex == index);
		}
	}


	@Override
	public boolean onHolderClick(int position) {
		long sinceId = 0;
		long maxId = 0;
		if (position == 0) {
			Status status = items.get(position + 1);
			if (status != null) {
				sinceId = status.getId();
			}
		} else if (position == items.size() - 1) {
			Status status = items.get(position - 1);
			if (status != null) {
				maxId = status.getId() - 1;
			}
		} else {
			Status status = items.get(position + 1);
			if (status != null) {
				sinceId = status.getId();
			}
			status = items.get(position - 1);
			if (status != null) {
				maxId = status.getId() - 1;
			}
		}
		boolean success = itemClickListener.onPlaceholderClick(sinceId, maxId, position);
		if (success) {
			loadingIndex = position;
			return true;
		}
		return false;
	}


	@Override
	public void onStatusClick(int position, int type) {
		if (type == OnStatusClickListener.TYPE_STATUS) {
			Status status = items.get(position);
			if (status != null) {
				itemClickListener.onStatusSelected(status);
			}
		}
	}

	/**
	 * Insert data at specific index of the list
	 *
	 * @param newItems list of statuses to insert
	 * @param index    position to insert
	 */
	public void addItems(@NonNull List<Status> newItems, int index) {
		disableLoading();
		if (newItems.size() > MIN_COUNT) {
			if (items.isEmpty() || items.get(index) != null) {
				// Add placeholder
				items.add(index, null);
				notifyItemInserted(index);
			}
		} else {
			if (!items.isEmpty() && items.get(index) == null) {
				// remove placeholder
				items.remove(index);
				notifyItemRemoved(index);
			}
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
	public void replaceItems(@NonNull List<Status> newItems) {
		items.clear();
		items.addAll(newItems);
		if (newItems.size() > MIN_COUNT) {
			items.add(null);
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
	 * check if list is empty
	 *
	 * @return true if list is empty
	 */
	public boolean isEmpty() {
		return items.isEmpty();
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