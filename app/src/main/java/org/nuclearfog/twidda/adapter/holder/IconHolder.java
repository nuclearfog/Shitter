package org.nuclearfog.twidda.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;


public class IconHolder extends RecyclerView.ViewHolder {

	public static final int TYPE_EMPTY = 0;

	public static final int TYPE_IMAGE = 1;

	public static final int TYPE_VIDEO = 2;

	public static final int TYPE_GIF = 3;

	public static final int TYPE_LOCATION = 4;

	private ImageButton button;

	private GlobalSettings settings;


	public IconHolder(ViewGroup parent, GlobalSettings settings) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mediabutton, parent, false));
		button = (ImageButton) itemView;
		this.settings = settings;
	}


	public void setContent(int iconType) {
		switch(iconType) {
			case TYPE_IMAGE:
				button.setImageResource(R.drawable.image);
				break;

			case TYPE_VIDEO:
				button.setImageResource(R.drawable.video);
				break;

			case TYPE_GIF:
				button.setImageResource(R.drawable.gif);
				break;

			case TYPE_LOCATION:
				button.setImageResource(R.drawable.location);
				break;

			default:
				button.setImageResource(0);
				break;
		}
		button.setColorFilter(settings.getIconColor());
	}
}