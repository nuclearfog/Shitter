package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.model.EditedStatus;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.IconHolder;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.OnHolderClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter sued to show status icons
 *
 * @author nuclearfog
 */
public class IconAdapter extends Adapter<IconHolder> implements OnHolderClickListener {

	@Nullable
	private OnIconClickListener listener;

	private List<Integer> items = new ArrayList<>();
	private boolean invert;

	/**
	 * @param invert true to invert item order
	 */
	public IconAdapter(@Nullable OnIconClickListener listener, boolean invert) {
		this.listener = listener;
		this.invert = invert;
	}


	@NonNull
	@Override
	public IconHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new IconHolder(parent, this);
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
			if (invert) {
				position = items.size() - position - 1;
			}
			switch (item) {
				case IconHolder.TYPE_IMAGE:
				case IconHolder.TYPE_GIF:
				case IconHolder.TYPE_VIDEO:
				case IconHolder.TYPE_AUDIO:
					listener.onIconClick(OnIconClickListener.MEDIA, position);
					break;

				case IconHolder.TYPE_POLL:
					listener.onIconClick(OnIconClickListener.POLL, position);
					break;
			}
		}
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		return false;
	}

	/**
	 * @return true if adapter does not contain any elements
	 */
	public boolean isEmpty() {
		return items.isEmpty();
	}

	/**
	 * add icons using status information
	 */
	public void setItems(Status status) {
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
	 * add icons using edited status information
	 */
	public void setItems(EditedStatus status) {
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
	 * set media icons
	 */
	public void setItems(Media[] medias) {
		items.clear();
		if (medias.length > 0) {
			addMediaIcons(medias);
		}
		notifyDataSetChanged();
	}

	/**
	 * append image icon at the end
	 */
	public void addImageItem() {
		appendItem(IconHolder.TYPE_IMAGE);
	}

	/**
	 * append video icon at the end
	 */
	public void addVideoItem() {
		appendItem(IconHolder.TYPE_VIDEO);
	}

	/**
	 * append GIF icon at the end
	 */
	public void addGifItem() {
		appendItem(IconHolder.TYPE_GIF);
	}

	/**
	 * append audio icon to the end
	 */
	public void addAudioItem() {
		appendItem(IconHolder.TYPE_AUDIO);
	}

	/**
	 * append poll icon to the end
	 */
	public void addPollItem() {
		appendItem(IconHolder.TYPE_POLL);
	}

	/**
	 *
	 */
	private void appendItem(int itemType) {
		if (invert) {
			items.add(0, itemType);
			notifyItemInserted(0);
		} else {
			items.add(itemType);
			notifyItemInserted(items.size() - 1);
		}
	}

	/**
	 * add media icons depending on type
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

				case Media.AUDIO:
					items.add(IconHolder.TYPE_AUDIO);
					break;
			}
		}
	}

	/**
	 * item click listener
	 */
	public interface OnIconClickListener {

		int MEDIA = 43;

		int POLL = 44;

		/**
		 * called on item click
		 *
		 * @param index index of the item
		 */
		void onIconClick(int type, int index);
	}
}