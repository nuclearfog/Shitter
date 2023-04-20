package org.nuclearfog.twidda.ui.adapter.holder;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.database.impl.DatabaseMedia;
import org.nuclearfog.twidda.model.Media;

import jp.wasabeef.picasso.transformations.BlurTransformation;

/**
 * holder class for {@link org.nuclearfog.twidda.ui.adapter.PreviewAdapter}
 *
 * @author nucleaerfog
 */
public class PreviewHolder extends ViewHolder implements OnClickListener {

	/**
	 * empty placeholder image color
	 */
	private static final int EMPTY_COLOR = 0x1f000000;

	private ImageView previewImage, playIcon;

	private Picasso picasso;
	private GlobalSettings settings;
	private OnHolderClickListener listener;


	public PreviewHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preview, parent, false));
		this.picasso = picasso;
		this.settings = settings;
		this.listener = listener;

		previewImage = itemView.findViewById(R.id.item_preview_image);
		playIcon = itemView.findViewById(R.id.item_preview_play);
		previewImage.getLayoutParams().width = parent.getMeasuredHeight();
		previewImage.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int pos = getLayoutPosition();
		if (pos != RecyclerView.NO_POSITION) {
			if (v == previewImage) {
				listener.onItemClick(pos, OnHolderClickListener.PREVIEW_CLICK);
			}
		}
	}

	/**
	 * set view content
	 *
	 * @param media media content
	 */
	public void setContent(Media media, boolean blurImage) {
		RequestCreator picassoBuilder = picasso.load(media.getPreviewUrl());
		Drawable placeholder = new ColorDrawable(EMPTY_COLOR);
		// check if online media and image loading is enabled
		if (!(media instanceof DatabaseMedia) && settings.imagesEnabled() && !media.getPreviewUrl().isEmpty()) {
			// set image blur if enabled
			if (blurImage) {
				BlurTransformation blurTransformation = new BlurTransformation(previewImage.getContext(), 30);
				picassoBuilder.transform(blurTransformation);
			}
			picassoBuilder.networkPolicy(NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_STORE).placeholder(placeholder).into(previewImage);
		} else {
			previewImage.setImageDrawable(placeholder);
		}
		// set 'play video' button
		if (media.getMediaType() == Media.VIDEO || media.getMediaType() == Media.GIF) {
			playIcon.setVisibility(View.VISIBLE);
		} else {
			playIcon.setVisibility(View.GONE);
		}
	}
}