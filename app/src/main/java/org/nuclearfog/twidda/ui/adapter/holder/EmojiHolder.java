package org.nuclearfog.twidda.ui.adapter.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Emoji;


public class EmojiHolder extends ViewHolder implements OnClickListener {

	public static final int ROW_COUNT = 6;

	private ImageView[] emojiViews = new ImageView[ROW_COUNT];
	private TextView groupLabel;

	private OnHolderClickListener listener;
	private Picasso picasso;


	public EmojiHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emoji, parent, false));

		groupLabel = itemView.findViewById(R.id.item_emoji_group);
		emojiViews[0] = itemView.findViewById(R.id.item_emoji_1);
		emojiViews[1] = itemView.findViewById(R.id.item_emoji_2);
		emojiViews[2] = itemView.findViewById(R.id.item_emoji_3);
		emojiViews[3] = itemView.findViewById(R.id.item_emoji_4);
		emojiViews[4] = itemView.findViewById(R.id.item_emoji_5);
		emojiViews[5] = itemView.findViewById(R.id.item_emoji_6);

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
				}
			}
		}
	}


	public void setData(Emoji[] emojis) {
		for (int i = 0 ; i < emojis.length && i < emojiViews.length ; i++) {
			Emoji emoji = emojis[i];
			if (emoji != null) {
				ImageView emojiView = emojiViews[i];
				if (!emoji.getUrl().trim().isEmpty()) {
					picasso.load(emoji.getUrl()).into(emojiViews[i]);
					emojiView.setVisibility(View.VISIBLE);
				}
				emojiView.setOnClickListener(this);
			}
		}
		groupLabel.setVisibility(View.GONE);
	}


	public void setLabel(String label) {
		for (ImageView emojiView : emojiViews) {
			emojiView.setVisibility(View.GONE);
		}
		groupLabel.setText(label);
		groupLabel.setVisibility(View.VISIBLE);
	}
}