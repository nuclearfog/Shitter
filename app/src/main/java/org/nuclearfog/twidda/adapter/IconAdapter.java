package org.nuclearfog.twidda.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.adapter.holder.IconHolder;
import org.nuclearfog.twidda.adapter.holder.OnHolderClickListener;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Message;
import org.nuclearfog.twidda.model.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter sued to show status icons
 *
 * @author nuclearfog
 */
public class IconAdapter extends Adapter<IconHolder> implements OnHolderClickListener {

	@Nullable
	private OnMediaClickListener listener;
	private GlobalSettings settings;
	private List<Integer> items;

	/**
	 *
	 */
	public IconAdapter(GlobalSettings settings) {
		this.settings = settings;
		items = new ArrayList<>();
	}


	@NonNull
	@Override
	public IconHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		IconHolder holder = new IconHolder(parent, settings);
		if (listener != null)
			holder.addOnHolderClickListener(this);
		return holder;
	}


	@Override
	public void onBindViewHolder(@NonNull IconHolder holder, int position) {
		holder.setIconType(items.get(position));
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		if (listener != null) {
			Integer item = items.get(position);
			if (item == IconHolder.TYPE_IMAGE || item == IconHolder.TYPE_GIF || item == IconHolder.TYPE_VIDEO) {
				listener.onMediaClick(position);
			}
		}
	}


	@Override
	public boolean onPlaceholderClick(int position) {
		return false;
	}


	/**
	 * add icons using status information
	 */
	public void addItems(Status status) {
		items.clear();
		if (status.getMedia().length > 0) {
			addMediaIcons(status.getMedia());
		}
		if (status.getLocation() != null) {
			items.add(IconHolder.TYPE_LOCATION);
		}
		if (status.getPoll() != null) {
			items.add(IconHolder.TYPE_POLL);
		}
		notifyDataSetChanged();
	}

	/**
	 * add icons using message information
	 */
	public void addItems(Message message) {
		items.clear();
		if (message.getMedia().length > 0) {
			addMediaIcons(message.getMedia());
		}
		notifyDataSetChanged();
	}

	/**
	 * add a single image icon
	 */
	public void addImageItem() {
		items.add(IconHolder.TYPE_IMAGE);
		notifyItemInserted(items.size() - 1);
	}

	/**
	 * add a single gif item
	 */
	public void addGifItem() {
		items.add(IconHolder.TYPE_GIF);
		notifyItemInserted(items.size() - 1);
	}

	/**
	 * add a single video item
	 */
	public void addVideoItem() {
		items.add(IconHolder.TYPE_VIDEO);
		notifyItemInserted(items.size() - 1);
	}

	/**
	 * add media iconsdepending on type
	 */
	private void addMediaIcons(Media[] medias) {
		for (Media media : medias) {
			switch (media.getMediaType()) {
				case Media.PHOTO:
					items.add(IconHolder.TYPE_IMAGE);
					break;

				case Media.GIF:
					items.add(IconHolder.TYPE_GIF);
					break;

				case Media.VIDEO:
					items.add(IconHolder.TYPE_VIDEO);
					break;
			}
		}
	}

	/**
	 * add click listener for media items
	 */
	public void addOnMediaClickListener(OnMediaClickListener listener) {
		this.listener = listener;
	}

	/**
	 * item click lsitener for media icons
	 */
	public interface OnMediaClickListener {

		/**
		 * called on media item click
		 */
		void onMediaClick(int index);
	}
}