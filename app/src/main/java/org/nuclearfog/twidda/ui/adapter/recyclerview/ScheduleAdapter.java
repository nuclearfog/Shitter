package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.model.ScheduledStatus;
import org.nuclearfog.twidda.model.lists.ScheduledStatuses;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.PlaceHolder;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.ScheduleHolder;

/**
 * @author nuclearfog
 */
public class ScheduleAdapter extends Adapter<ViewHolder> implements OnHolderClickListener {

	private static final int ITEM_GAP = 1;
	private static final int ITEM_SCHEDULE = 2;


	private OnScheduleClickListener listener;

	private ScheduledStatuses items = new ScheduledStatuses();
	private int loadingIndex = -1;


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

	}


	@Override
	public boolean onPlaceholderClick(int index) {
		return false;
	}


	public interface OnScheduleClickListener {

		int SELECT = 1;
		int REMOVE = 2;

		void onScheduleClick(ScheduledStatus status, int type);
	}
}