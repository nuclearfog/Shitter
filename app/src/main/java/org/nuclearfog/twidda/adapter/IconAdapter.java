package org.nuclearfog.twidda.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.adapter.holder.IconHolder;
import org.nuclearfog.twidda.adapter.holder.OnHolderClickListener;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Message;
import org.nuclearfog.twidda.model.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RecyclerView Adapter sued to show status icons
 *
 * @author nuclearfog
 */
public class IconAdapter extends Adapter<IconHolder> implements OnHolderClickListener {

	private GlobalSettings settings;

	@Nullable
	private OnMediaClickListener listener;
	private List<Object> items = new ArrayList<>();

	/**
	 *
	 */
	public IconAdapter(GlobalSettings settings) {
		this.settings = settings;
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
		Object item = items.get(position);
		if (item instanceof Media) {
			Media media = (Media) item;
			switch (media.getMediaType()) {
				case Media.PHOTO:
					holder.setIconType(IconHolder.TYPE_IMAGE);
					break;

				case Media.VIDEO:
					holder.setIconType(IconHolder.TYPE_VIDEO);
					break;

				case Media.GIF:
					holder.setIconType(IconHolder.TYPE_GIF);
					break;

				default:
				case Media.NONE:
					holder.setIconType(IconHolder.TYPE_EMPTY);
					break;
			}
		} else if (item instanceof Location) {
			holder.setIconType(IconHolder.TYPE_LOCATION);
		} else {
			holder.setIconType(IconHolder.TYPE_EMPTY);
		}
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		Object item = items.get(position);
		if (listener != null && item instanceof Media) {
			listener.onMediaClick(position);
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
			items.addAll(Arrays.asList(status.getMedia()));
		}
		if (status.getLocation() != null) {
			items.add(status.getLocation());
		}
		notifyDataSetChanged();
	}

	/**
	 * add icons using message information
	 */
	public void addItems(Message message) {
		items.clear();
		if (message.getMedia().length > 0) {
			items.addAll(Arrays.asList(message.getMedia()));
		}
		notifyDataSetChanged();
	}

	/**
	 * clear previous icons
	 */
	public void clear() {
		items.clear();
		notifyDataSetChanged();
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