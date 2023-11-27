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
		if (type == ANNOUNCEMENT_DISMISS) {
			listener.onAnnouncementDismiss(items.get(position));
		} else if (type == ANNOUNCEMENT_CLICK) {
			listener.onAnnouncementClick(items.get(position));
		}
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
	 * remove single item matching ID
	 *
	 * @param id ID of the item to remove
	 */
	public void removeItem(long id) {
		for (int i = items.size() - 1; i >= 0; i--) {
			if (items.get(i).getId() == id) {
				items.remove(i);
				notifyItemRemoved(i);
			}
		}
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
		 * called when announcement was selected
		 *
		 * @param announcement clicked item
		 */
		void onAnnouncementClick(Announcement announcement);

		/**
		 * called to dismiss announcement
		 *
		 * @param announcement clicked item
		 */
		void onAnnouncementDismiss(Announcement announcement);
	}
}