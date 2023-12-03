package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.model.lists.StatusEditHistory;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.EditHistoryHolder;

/**
 * RecyclerView adapter for {@link org.nuclearfog.twidda.ui.fragments.EditHistoryFragment}
 *
 * @author nuclearfog
 */
public class EditHistoryAdapter extends Adapter<EditHistoryHolder> {

	private StatusEditHistory items = new StatusEditHistory();


	@NonNull
	@Override
	public EditHistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new EditHistoryHolder(parent);
	}


	@Override
	public void onBindViewHolder(@NonNull EditHistoryHolder holder, int position) {
		holder.setContent(items.get(position));
	}


	@Override
	public int getItemCount() {
		return items.size();
	}

	/**
	 * replace all items of adapter
	 */
	public void setItems(StatusEditHistory items) {
		this.items.clear();
		this.items.addAll(items);
		notifyDataSetChanged();
	}

	/**
	 * get all items from adapter
	 */
	public StatusEditHistory getItems() {
		return new StatusEditHistory(items);
	}

	/**
	 * remove all items from adapter
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
}
