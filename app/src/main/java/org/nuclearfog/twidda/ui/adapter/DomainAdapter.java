package org.nuclearfog.twidda.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.lists.Domains;
import org.nuclearfog.twidda.ui.adapter.holder.DomainHolder;
import org.nuclearfog.twidda.ui.adapter.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.holder.PlaceHolder;

/**
 * RecyclerView adapter for domain list
 *
 * @author nuclearfog
 */
public class DomainAdapter extends Adapter<ViewHolder> implements OnHolderClickListener {

	private static final int TYPE_ITEM = 0;
	private static final int TYPE_FOOTER = 1;

	private static final int NO_LOADING = -1;
	public static final int NO_INDEX = -1;

	private OnDomainClickListener listener;
	private GlobalSettings settings;

	private Domains items = new Domains();
	private int loadingIndex = NO_LOADING;

	/**
	 *
	 */
	public DomainAdapter(Context context, OnDomainClickListener listener) {
		settings = GlobalSettings.getInstance(context);
		this.listener = listener;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == TYPE_ITEM) {
			return new DomainHolder(parent, settings, this);
		} else {
			return new PlaceHolder(parent, settings, false, this);
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		if (holder instanceof DomainHolder) {
			DomainHolder domainHolder = (DomainHolder) holder;
			domainHolder.setDomain(items.get(position));
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
	public int getItemViewType(int position) {
		if (items.get(position) != null)
			return TYPE_ITEM;
		return TYPE_FOOTER;
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		if (type == DOMAIN_REMOVE) {
			listener.onDomainRemove(items.get(position));
		}
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		return listener.onPlaceholderClick(index, items.getNextCursor());
	}

	/**
	 * replace old items with new items
	 *
	 * @param domains new items
	 */
	public void replaceItems(Domains domains) {
		disableLoading();
		items.replaceAll(domains);
		if (items.getNextCursor() != 0L && items.peekLast() != null) {
			items.add(null);
		}
		notifyDataSetChanged();
	}

	/**
	 * insert new items at specific position
	 *
	 * @param domains new items
	 * @param index   index where to insert new items
	 */
	public void addItems(Domains domains, int index) {
		disableLoading();
		if (index < 0) {
			items.replaceAll(domains);
			if (items.getNextCursor() != 0L) {
				items.add(null);
			}
			notifyDataSetChanged();
		} else {
			items.addAll(index, domains);
			if (items.getNextCursor() != 0L && items.peekLast() != null) {
				items.add(null);
				notifyItemRangeInserted(index, domains.size() + 1);
			} else if (items.getNextCursor() == 0L && items.peekLast() == null) {
				items.pollLast();
				notifyItemRangeInserted(index, domains.size() - 1);
			} else if (!domains.isEmpty()) {
				notifyItemRangeInserted(index, domains.size());
			}
		}
	}

	/**
	 * get all items used in this adapter
	 *
	 * @return domain list
	 */
	public Domains getItems() {
		return new Domains(items);
	}

	/**
	 * remove specific item from list
	 *
	 * @param item domain name item
	 */
	public void removeItem(String item) {
		int index = items.indexOf(item);
		if (index >= 0) {
			items.remove(index);
			notifyItemRemoved(index);
		}
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
	 * listener for list item
	 */
	public interface OnDomainClickListener {

		/**
		 * called on remove button click
		 *
		 * @param domain item of selected item
		 */
		void onDomainRemove(String domain);

		/**
		 * called on footer click
		 *
		 * @param index  index of the footer
		 * @param cursor cursor for the next items
		 * @return true if click was performed
		 */
		boolean onPlaceholderClick(int index, long cursor);
	}
}