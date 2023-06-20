package org.nuclearfog.twidda.ui.adapter.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * viewholder for {@link org.nuclearfog.twidda.ui.adapter.IconAdapter}
 *
 * @author nuclearfog
 */
public class IconHolder extends ViewHolder implements OnClickListener {

	/**
	 * icon type used for image item
	 */
	public static final int TYPE_IMAGE = 1;

	/**
	 * icon type used for video item
	 */
	public static final int TYPE_VIDEO = 2;

	/**
	 * icon type used for GIF item
	 */
	public static final int TYPE_GIF = 3;

	/**
	 * icon type used for poll
	 */
	public static final int TYPE_POLL = 4;

	/**
	 * item type used for location item
	 */
	public static final int TYPE_LOCATION = 5;

	/**
	 * item type used for audio item
	 */
	public static final int TYPE_AUDIO = 6;

	private ImageButton button;

	private GlobalSettings settings;
	private OnHolderClickListener listener;

	/**
	 *
	 */
	public IconHolder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icon, parent, false));
		settings = GlobalSettings.get(parent.getContext());
		this.listener = listener;

		button = itemView.findViewById(R.id.item_icon_media);

		int size = parent.getLayoutParams().height;
		if (size == 0)
			size = parent.getMeasuredHeight();
		itemView.getLayoutParams().height = size;
		itemView.getLayoutParams().width = size;

		button.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != RecyclerView.NO_POSITION) {
			if (v.getId() == R.id.item_icon_media) {
				listener.onItemClick(position, OnHolderClickListener.NO_TYPE);
			}
		}
	}

	/**
	 * define icon type
	 *
	 * @param iconType icon type {@link #TYPE_GIF,#TYPE_IMAGE,#TYPE_LOCATION,#TYPE_VIDEO}
	 */
	public void setIconType(int iconType) {
		switch (iconType) {
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

			case TYPE_POLL:
				button.setImageResource(R.drawable.poll);
				break;

			case TYPE_AUDIO:
				button.setImageResource(R.drawable.audio);
				break;

			default:
				button.setImageResource(0);
				break;
		}
		button.setColorFilter(settings.getIconColor());
	}
}