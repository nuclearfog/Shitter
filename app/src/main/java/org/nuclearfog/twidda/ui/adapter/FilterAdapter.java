package org.nuclearfog.twidda.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.model.Filter;
import org.nuclearfog.twidda.model.lists.Filters;
import org.nuclearfog.twidda.ui.adapter.holder.FilterHolder;
import org.nuclearfog.twidda.ui.adapter.holder.OnHolderClickListener;

/**
 * filterlist adapter
 *
 * @see org.nuclearfog.twidda.ui.fragments.FilterFragment
 * @author nuclearfog
 */
public class FilterAdapter extends Adapter<FilterHolder> implements OnHolderClickListener {


	private OnFilterClickListener listener;
	private Filters items = new Filters();

	/**
	 *
	 */
	public FilterAdapter(OnFilterClickListener listener) {
		this.listener = listener;
	}


	@NonNull
	@Override
	public FilterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new FilterHolder(parent, this);
	}


	@Override
	public void onBindViewHolder(@NonNull FilterHolder holder, int position) {
		holder.setContent(items.get(position));
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		if (type == OnHolderClickListener.FILTER_CLICK) {
			listener.onFilterClick(items.get(position));
		} else if (type == OnHolderClickListener.FILTER_REMOVE) {
			listener.onFilterRemove(items.get(position));
		}
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		return false;
	}

	/**
	 * replace all filter items
	 *
	 * @param items new items to insert
	 */
	public void replaceItems(Filters items) {
		this.items.clear();
		this.items.addAll(items);
		notifyDataSetChanged();
	}

	/**
	 * remove item matching an ID
	 *
	 * @param id ID of the item to remove
	 */
	public void removeItem(long id) {
		for (int i = 0 ; i < items.size() ; i++) {
			if (items.get(i).getId() == id) {
				items.remove(i);
				notifyItemRemoved(i);
				break;
			}
		}
	}

	/**
	 * listener for filterlist items
	 */
	public interface OnFilterClickListener {

		/**
		 * ccalled on filter item click
		 *
		 * @param filter selected filter item
		 */
		void onFilterClick(Filter filter);

		/**
		 * ccalled on filter item remove click
		 *
		 * @param filter selected filter item
		 */
		void onFilterRemove(Filter filter);
	}
}