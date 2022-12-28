package org.nuclearfog.twidda.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.adapter.holder.IconHolder;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class IconAdapter extends Adapter<IconHolder> {

	private GlobalSettings settings;

	private List<Object> items = new ArrayList<>();


	public IconAdapter(GlobalSettings settings) {
		this.settings = settings;
	}


	@NonNull
	@Override
	public IconHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new IconHolder(parent, settings);
	}


	@Override
	public void onBindViewHolder(@NonNull IconHolder holder, int position) {
		Object item = items.get(position);
		if (item instanceof Media) {
			Media media = (Media) item;
			switch (media.getMediaType()) {
				case Media.PHOTO:
					holder.setContent(IconHolder.TYPE_IMAGE);
					break;

				case Media.VIDEO:
					holder.setContent(IconHolder.TYPE_VIDEO);
					break;

				case Media.GIF:
					holder.setContent(IconHolder.TYPE_GIF);
					break;

				default:
				case Media.NONE:
					holder.setContent(IconHolder.TYPE_EMPTY);
					break;
			}
		} else if (item instanceof Location) {
			holder.setContent(IconHolder.TYPE_LOCATION);
		} else {
			holder.setContent(IconHolder.TYPE_EMPTY);
		}
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


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


	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}
}