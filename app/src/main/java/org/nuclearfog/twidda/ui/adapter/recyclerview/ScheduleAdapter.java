package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.model.ScheduledStatus;
import org.nuclearfog.twidda.model.lists.ScheduledStatuses;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.PlaceHolder;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.ScheduleHolder;

/**
 * @author nuclearfog
 */
public class ScheduleAdapter extends Adapter<ViewHolder> implements OnHolderClickListener {

	private static final int NO_LOADING = -1;

	private static final int MIN_COUNT = 2;

	private static final int ITEM_GAP = 1;
	private static final int ITEM_SCHEDULE = 2;


	private OnScheduleClickListener listener;

	private ScheduledStatuses items = new ScheduledStatuses();
	private int loadingIndex = NO_LOADING;

	/**
	 * @param listener item click listener
	 */
	public ScheduleAdapter(OnScheduleClickListener listener) {
		this.listener = listener;
	}


	@Override
	public int getItemViewType(int index) {
		if (items.get(index) == null)
			return ITEM_GAP;
		return ITEM_SCHEDULE;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == ITEM_SCHEDULE) {
			return new ScheduleHolder(parent, this);
		} else {
			return new PlaceHolder(parent, this, false);
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		if (holder instanceof ScheduleHolder) {
			ScheduledStatus item = items.get(position);
			if (item != null) {
				((ScheduleHolder) holder).setContent(item);
			}
		} else if (holder instanceof PlaceHolder) {
			PlaceHolder placeHolder = (PlaceHolder) holder;
			placeHolder.setLoading(loadingIndex == position);
		}
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		if (type == SCHEDULE_CLICK) {
			ScheduledStatus item = items.get(position);
			if (item != null) {
				listener.onScheduleClick(item, OnScheduleClickListener.SELECT);
			}
		} else if (type == SCHEDULE_REMOVE) {
			listener.onScheduleClick(items.get(position), OnScheduleClickListener.REMOVE);
		}
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		long sinceId = 0;
		long maxId = 0;
		if (index == 0) {
			if (items.size() > 1) {
				ScheduledStatus item = items.get(index + 1);
				if (item != null) {
					sinceId = item.getId();
				}
			}
		} else if (index == items.size() - 1) {
			ScheduledStatus item = items.get(index - 1);
			if (item != null) {
				maxId = item.getId() - 1;
			}
		} else {
			ScheduledStatus item = items.get(index + 1);
			if (item != null) {
				sinceId = item.getId();
			}
			item = items.get(index - 1);
			if (item != null) {
				maxId = item.getId() - 1;
			}
		}
		boolean success = listener.onPlaceholderClick(sinceId, maxId, index);
		if (success) {
			loadingIndex = index;
			return true;
		}
		return false;
	}

	/**
	 *
	 */
	public void setItems(ScheduledStatuses newItems) {
		items.clear();
		items.addAll(newItems);
		if (newItems.size() > MIN_COUNT) {
			items.add(null);
		}
		notifyDataSetChanged();
	}


	/**
	 *
	 */
	public void addItems(ScheduledStatuses newItems, int index) {
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
	 *
	 */
	public void removeItem(long id) {
		int pos = -1;
		for (int i = items.size() - 1; i >= 0; i--) {
			ScheduledStatus item = items.get(i);
			if (item != null && item.getId() == id) {
				pos = i;
				break;
			}
		}
		if (pos >= 0) {
			items.remove(pos);
			notifyItemRemoved(pos);
		}
	}

	/**
	 *
	 */
	public void updateItem(@NonNull ScheduledStatus item) {
		for (int pos = items.size() - 1; pos >= 0; pos--) {
			ScheduledStatus current = items.get(pos);
			if (current != null && current.getId() == item.getId()) {
				items.set(pos, item);
				notifyItemChanged(pos);
				break;
			}
		}
	}

	/**
	 *
	 */
	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}

	/**
	 *
	 */
	public ScheduledStatuses getItems() {
		return new ScheduledStatuses(items);
	}

	/**
	 *
	 */
	public long getTopItemId() {
		ScheduledStatus item = items.peekFirst();
		if (item != null)
			return item.getId();
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
	 *
	 */
	public interface OnScheduleClickListener {

		int SELECT = 1;
		int REMOVE = 2;

		void onScheduleClick(ScheduledStatus status, int type);

		boolean onPlaceholderClick(long min_id, long max_id, int position);
	}
}