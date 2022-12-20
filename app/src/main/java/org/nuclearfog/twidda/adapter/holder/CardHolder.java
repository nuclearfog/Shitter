package org.nuclearfog.twidda.adapter.holder;

import android.graphics.Typeface;
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
import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.PreviewAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Card;

/**
 * adapter item for {@link PreviewAdapter}
 *
 * @author nuclearfog
 */
public class CardHolder extends ViewHolder implements OnClickListener {

	/**
	 * link text background transparency
	 */
	private static final int TEXT_TRANSPARENCY = 0xafffffff;

	/**
	 * how much views should be fit in the window
	 */
	private static final int COLUMN_COUNT = 2;

	/**
	 * maximum char count of the title before truncating
	 */
	private static final int TITLE_MAX_LEN = 30;

	private TextView linkText;
	private ImageView preview;

	private Picasso picasso;
	private OnItemClickListener listener;


	public CardHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false));
		this.picasso = picasso;

		linkText = itemView.findViewById(R.id.link_preview_text);
		preview = itemView.findViewById(R.id.link_preview_image);

		itemView.getLayoutParams().width = parent.getMeasuredWidth() / COLUMN_COUNT;
		itemView.getLayoutParams().height = parent.getMeasuredHeight();
		linkText.setTypeface(settings.getTypeFace());
		linkText.setTextColor(settings.getFontColor());
		linkText.setBackgroundColor(settings.getBackgroundColor() & TEXT_TRANSPARENCY);

		linkText.setOnClickListener(this);
		preview.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int pos = getLayoutPosition();
		if (pos != RecyclerView.NO_POSITION && listener != null) {
			if (v == linkText) {
				listener.onItemClick(pos, OnItemClickListener.TYPE_LINK);
			} else if (v == preview) {
				listener.onItemClick(pos, OnItemClickListener.TYPE_IMAGE);
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
			textSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, Math.min(textStr.length() - 1, TITLE_MAX_LEN), 0);
		linkText.setText(textSpan);
		if (!card.getImageUrl().isEmpty()) {
			picasso.load(card.getImageUrl()).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(preview);
		}
	}

	/**
	 * add viewholder click listener
	 */
	public void setClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	/**
	 * custom viewholder click listener
	 */
	public interface OnItemClickListener {

		/**
		 * indicates a link click
		 */
		int TYPE_LINK = 1;

		/**
		 * indicates a click on the image
		 */
		int TYPE_IMAGE = 2;

		/**
		 * @param pos  index of the item
		 * @param type type of click {@link #TYPE_IMAGE,#TYPE_LINK}
		 */
		void onItemClick(int pos, int type);
	}
}