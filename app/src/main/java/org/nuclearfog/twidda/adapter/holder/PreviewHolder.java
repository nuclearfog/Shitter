package org.nuclearfog.twidda.adapter.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.model.Media;

/**
 * holder class for {@link org.nuclearfog.twidda.adapter.PreviewAdapter}
 *
 * @author nucleaerfog
 */
public class PreviewHolder extends ViewHolder implements OnClickListener {

	private ImageView previewImage, playIcon;

	private Picasso picasso;
	private OnPreviewClickListener listener;


	public PreviewHolder(ViewGroup parent, Picasso picasso) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preview, parent, false));
		previewImage = itemView.findViewById(R.id.item_preview_image);
		playIcon = itemView.findViewById(R.id.item_preview_play);
		previewImage.getLayoutParams().width = parent.getMeasuredHeight();
		this.picasso = picasso;

		previewImage.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int pos = getLayoutPosition();
		if (pos != RecyclerView.NO_POSITION && listener != null) {
			if (v == previewImage) {
				listener.onPreviewClick(pos);
			}
		}
	}

	/**
	 * set view content
	 *
	 * @param media media content
	 */
	public void setContent(Media media) {
		if (!media.getPreviewUrl().isEmpty()) {
			picasso.load(media.getPreviewUrl()).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).error(R.drawable.no_image).into(previewImage);
		} else {
			previewImage.setImageResource(R.drawable.no_image);
		}
		if (media.getMediaType() == Media.VIDEO || media.getMediaType() == Media.GIF) {
			playIcon.setVisibility(View.VISIBLE);
		} else {
			playIcon.setVisibility(View.GONE);
		}
	}


	/**
	 * set holder click lsitener
	 *
	 * @param listener listener for the holder
	 */
	public void setOnPreviewClickListener(OnPreviewClickListener listener) {
		this.listener = listener;
	}

	/**
	 * holder click lsitener
	 */
	public interface OnPreviewClickListener {

		/**
		 * called on holder click
		 *
		 * @param pos list position of the holder
		 */
		void onPreviewClick(int pos);
	}
}