package org.nuclearfog.twidda.ui.adapter.holder;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import com.wolt.blurhashkt.BlurHashDecoder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Media;

import jp.wasabeef.picasso.transformations.BlurTransformation;

/**
 * holder class for {@link org.nuclearfog.twidda.ui.adapter.PreviewAdapter}
 *
 * @author nucleaerfog
 */
public class MediaHolder extends ViewHolder implements OnClickListener {

	/**
	 * empty placeholder image color
	 */
	private static final int EMPTY_COLOR = 0x1f000000;

	private ImageView previewImage, playIcon;

	private Picasso picasso;
	private GlobalSettings settings;
	private OnHolderClickListener listener;

	private Media media;

	/**
	 *
	 */
	public MediaHolder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preview, parent, false));
		settings = GlobalSettings.get(parent.getContext());
		picasso = PicassoBuilder.get(parent.getContext());
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
		// skip if same media is already set
		if (!media.equals(this.media)) {
			Drawable placeholder = new ColorDrawable(EMPTY_COLOR);
			if (settings.imagesEnabled() && media.getMediaType() != Media.AUDIO && media.getMediaType() != Media.UNDEFINED && !media.getPreviewUrl().trim().isEmpty()) {
				if (blurImage) {
					// use integrated blur generator
					if (media.getBlurHash().isEmpty()) {
						BlurTransformation blurTransformation = new BlurTransformation(previewImage.getContext(), 30);
						RequestCreator picassoBuilder = picasso.load(media.getPreviewUrl());
						picassoBuilder.transform(blurTransformation).networkPolicy(NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_STORE);
						picassoBuilder.placeholder(placeholder).into(previewImage);
					}
					// use hash to generate blur
					else {
						Bitmap blur = BlurHashDecoder.INSTANCE.decode(media.getBlurHash(), 16, 16, 1f, true);
						previewImage.setImageBitmap(blur);
					}
				} else {
					RequestCreator picassoBuilder = picasso.load(media.getPreviewUrl());
					// create blur placeholder
					if (!media.getBlurHash().isEmpty()) {
						Bitmap blur = BlurHashDecoder.INSTANCE.decode(media.getBlurHash(), 16, 16, 1f, true);
						picassoBuilder.placeholder(new BitmapDrawable(previewImage.getResources(), blur));
					}
					// load preview image
					picassoBuilder.networkPolicy(NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_STORE);
					picassoBuilder.into(previewImage);
				}
			} else {
				previewImage.setImageDrawable(placeholder);
			}
			switch (media.getMediaType()) {
				case Media.AUDIO:
				case Media.VIDEO:
					playIcon.setVisibility(View.VISIBLE);
					playIcon.setImageResource(R.drawable.play);

					break;

				case Media.GIF:
					playIcon.setVisibility(View.VISIBLE);
					playIcon.setImageResource(R.drawable.gif);
					break;

				default:
					playIcon.setVisibility(View.GONE);
					break;
			}
			AppStyles.setDrawableColor(playIcon, settings.getIconColor());
			this.media = media;
		}
	}
}