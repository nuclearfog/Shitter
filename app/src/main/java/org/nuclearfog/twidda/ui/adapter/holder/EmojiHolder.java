package org.nuclearfog.twidda.ui.adapter.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Emoji;

/**
 * Emoji holder class for {@link org.nuclearfog.twidda.ui.adapter.EmojiAdapter}
 *
 * @author nuclearfog
 */
public class EmojiHolder extends ViewHolder implements OnClickListener {

	public static final int ROW_COUNT = 6;

	private ImageView[] emojiViews = new ImageView[ROW_COUNT];
	private TextView groupLabel;

	private OnHolderClickListener listener;
	private Picasso picasso;

	/**
	 *
	 */
	public EmojiHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emoji, parent, false));
		groupLabel = itemView.findViewById(R.id.item_emoji_group);
		emojiViews[0] = itemView.findViewById(R.id.item_emoji_1);
		emojiViews[1] = itemView.findViewById(R.id.item_emoji_2);
		emojiViews[2] = itemView.findViewById(R.id.item_emoji_3);
		emojiViews[3] = itemView.findViewById(R.id.item_emoji_4);
		emojiViews[4] = itemView.findViewById(R.id.item_emoji_5);
		emojiViews[5] = itemView.findViewById(R.id.item_emoji_6);

		for (ImageView emojiView : emojiViews)
			emojiView.setOnClickListener(this);
		groupLabel.setTextColor(settings.getTextColor());
		groupLabel.setTypeface(settings.getTypeFace());

		this.listener = listener;
		this.picasso = picasso;
	}


	@Override
	public void onClick(View v) {
		int pos = getLayoutPosition();
		if (pos != RecyclerView.NO_POSITION) {
			for (int i = 0; i < emojiViews.length; i++) {
				if (v == emojiViews[i]) {
					listener.onItemClick(pos, OnHolderClickListener.EMOJI_CLICK, i);
					v.startAnimation(AnimationUtils.loadAnimation(v.getContext(),R.anim.emoji));
				}
			}
		}
	}

	/**
	 * set emoji group
	 *
	 * @param emojis a group of emojis
	 */
	public void setData(Emoji[] emojis) {
		for (int i = 0 ; i < emojiViews.length ; i++) {
			Emoji emoji = null;
			if (i < emojis.length)
				emoji = emojis[i];
			if (emoji != null && !emoji.getUrl().trim().isEmpty()) {
				picasso.load(emoji.getUrl()).error(R.drawable.no_image).into(emojiViews[i]);
				emojiViews[i].setVisibility(View.VISIBLE);
			} else {
				emojiViews[i].setVisibility(View.INVISIBLE);
			}
		}
		groupLabel.setVisibility(View.GONE);
	}

	/**
	 * set emoji group label
	 *
	 * @param label name of the group
	 */
	public void setLabel(String label) {
		for (ImageView emojiView : emojiViews) {
			emojiView.setVisibility(View.GONE);
		}
		groupLabel.setText(label);
		groupLabel.setVisibility(View.VISIBLE);
	}
}