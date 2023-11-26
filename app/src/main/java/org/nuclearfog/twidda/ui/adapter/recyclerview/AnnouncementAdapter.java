package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.model.Announcement;
import org.nuclearfog.twidda.model.lists.Announcements;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.AnnouncementHolder;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.OnHolderClickListener;

/**
 * RecyclerView adapter used to show a list of {@link org.nuclearfog.twidda.model.Announcement}
 *
 * @author nuclearfog
 */
public class AnnouncementAdapter extends Adapter<AnnouncementHolder> implements OnHolderClickListener {

	private OnAnnouncementClickListener listener;
	private Announcements items = new Announcements();

	/**
	 * @param listener item click listener
	 */
	public AnnouncementAdapter(OnAnnouncementClickListener listener) {
		this.listener = listener;
	}


	@NonNull
	@Override
	public AnnouncementHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new AnnouncementHolder(parent, this);
	}


	@Override
	public void onBindViewHolder(@NonNull AnnouncementHolder holder, int position) {
		holder.setContent(items.get(position));
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		listener.onAnnouncementClick(items.get(position));
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		return false;
	}

	/**
	 * set adapter items
	 */
	public void setItems(Announcements items) {
		this.items.clear();
		this.items.addAll(items);
		notifyDataSetChanged();
	}

	/**
	 * get adapter items
	 */
	public Announcements getItems() {
		return new Announcements(items);
	}

	/**
	 * clear all items
	 */
	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}

	/**
	 * @return true if adapter contains no items
	 */
	public boolean isEmpty() {
		return items.isEmpty();
	}

	/**
	 * adapter item click listener
	 */
	public interface OnAnnouncementClickListener {

		/**
		 * @param announcement clicked item
		 */
		void onAnnouncementClick(Announcement announcement);
	}
}