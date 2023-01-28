package org.nuclearfog.twidda.adapter.holder;

import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.SpannableString;
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

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.PreviewAdapter;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Card;

/**
 * adapter item for {@link PreviewAdapter}
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


	public CardHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false));
		this.picasso = picasso;
		this.settings = settings;
		this.listener = listener;

		linkText = itemView.findViewById(R.id.link_preview_text);
		preview = itemView.findViewById(R.id.link_preview_image);

		itemView.getLayoutParams().width = parent.getMeasuredHeight() * 16 / 9;

		linkText.setTypeface(settings.getTypeFace());
		linkText.setTextColor(settings.getFontColor());
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
	 * @param card card content
	 */
	public void setContent(Card card) {
		String textStr;
		String title = card.getTitle();
		if (title.length() > TITLE_MAX_LEN) {
			textStr = title.substring(0, TITLE_MAX_LEN - 3) + "...";
		} else {
			textStr = title;
		}
		if (!card.getDescription().isEmpty())
			textStr += '\n' + card.getDescription();
		SpannableString textSpan = new SpannableString(textStr);
		if (!title.isEmpty())
			textSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, Math.min(title.length(), TITLE_MAX_LEN), 0);
		linkText.setText(textSpan);
		if (settings.imagesEnabled() && !card.getImageUrl().isEmpty()) {
			picasso.load(card.getImageUrl()).networkPolicy(NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_STORE).into(preview);
		} else {
			preview.setImageDrawable(new ColorDrawable(EMPTY_COLOR));
		}
	}
}