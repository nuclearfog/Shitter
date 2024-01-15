package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.BlurHashDecoder;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Card;

/**
 * adapter item for {@link org.nuclearfog.twidda.ui.adapter.recyclerview.PreviewAdapter}
 *
 * @author nuclearfog
 */
public class CardHolder extends ViewHolder implements OnClickListener {

	/**
	 * empty placeholder image color
	 */
	private static final int EMPTY_COLOR = 0x1f000000;

	/**
	 * link text background transparency
	 */
	private static final int TEXT_TRANSPARENCY = 0xafffffff;

	/**
	 * maximum char count of the title before truncating
	 */
	private static final int TITLE_MAX_LEN = 30;

	private TextView linkText;
	private ImageView preview;

	private Picasso picasso;
	private GlobalSettings settings;
	private OnHolderClickListener listener;

	private Card card;

	/**
	 *
	 */
	public CardHolder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false));
		linkText = itemView.findViewById(R.id.link_preview_text);
		preview = itemView.findViewById(R.id.link_preview_image);
		settings = GlobalSettings.get(parent.getContext());
		picasso = PicassoBuilder.get(parent.getContext());
		this.listener = listener;

		itemView.getLayoutParams().width = Resources.getSystem().getDisplayMetrics().widthPixels * 2 / 3;
		linkText.setTypeface(settings.getTypeFace());
		linkText.setTextColor(settings.getTextColor());
		linkText.setBackgroundColor(settings.getBackgroundColor() & TEXT_TRANSPARENCY);

		linkText.setOnClickListener(this);
		preview.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int pos = getLayoutPosition();
		if (pos != RecyclerView.NO_POSITION) {
			if (v == linkText) {
				listener.onItemClick(pos, OnHolderClickListener.CARD_LINK);
			} else if (v == preview) {
				listener.onItemClick(pos, OnHolderClickListener.CARD_IMAGE);
			}
		}
	}

	/**
	 * set view content
	 *
	 * @param card        card content
	 * @param blurPreview true to blur the preview image
	 */
	public void setContent(Card card, boolean blurPreview) {
		if (!card.equals(this.card)) {
			SpannableStringBuilder urlDescription = new SpannableStringBuilder();
			Drawable placeholder = new ColorDrawable(EMPTY_COLOR);
			// set url preview image
			if (settings.imagesEnabled() && !card.getImageUrl().isEmpty()) {
				if (blurPreview) {
					if (!card.getBlurHash().isEmpty()) {
						Bitmap blur = BlurHashDecoder.decode(card.getBlurHash());
						preview.setImageBitmap(blur);
					} else {
						preview.setImageDrawable(placeholder);
					}
				} else {
					RequestCreator picassoBuilder = picasso.load(card.getImageUrl()).networkPolicy(NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_STORE);
					if (!card.getBlurHash().isEmpty()) {
						Bitmap blur = BlurHashDecoder.decode(card.getBlurHash());
						picassoBuilder.placeholder(new BitmapDrawable(preview.getResources(), blur)).into(preview);
					} else {
						picassoBuilder.placeholder(placeholder).into(preview);
					}
				}
			} else {
				preview.setImageDrawable(placeholder);
			}
			// set url title and truncate if needed
			if (!card.getTitle().trim().isEmpty()) {
				// truncate title
				if (card.getTitle().length() > TITLE_MAX_LEN) {
					urlDescription.append(card.getTitle().substring(0, TITLE_MAX_LEN - 3));
					urlDescription.append("...");
					urlDescription.setSpan(new StyleSpan(Typeface.BOLD), 0, TITLE_MAX_LEN, 0);
				} else {
					urlDescription.append(card.getTitle());
					urlDescription.setSpan(new StyleSpan(Typeface.BOLD), 0, card.getTitle().length(), 0);
				}
			}
			// set url description
			if (!card.getDescription().isEmpty()) {
				urlDescription.append('\n');
				urlDescription.append(card.getDescription());
			}
			// apply description
			linkText.setText(urlDescription);
			this.card = card;
		}
	}
}