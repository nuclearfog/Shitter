package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

import android.graphics.PorterDuff;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Reaction;

import java.util.Random;

/**
 * Viewholder used by {@link org.nuclearfog.twidda.ui.adapter.recyclerview.ReactionAdapter}
 *
 * @author nuclearfog
 */
public class ReactionHolder extends ViewHolder implements OnClickListener, AsyncCallback<TextEmojiLoader.Result> {

	/**
	 * transparency color mask used for background
	 */
	private static final int TRANSPARENCY_MASK = 0xC0FFFFFF;

	private static final Random RND = new Random();

	private View root;
	private ImageView icon;
	private TextView description;

	private OnHolderClickListener listener;
	private TextEmojiLoader emojiLoader;
	private GlobalSettings settings;
	private Picasso picasso;
	private int iconSize;

	private long tagId = RND.nextLong();

	/**
	 *
	 */
	public ReactionHolder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reaction, parent, false));
		icon = itemView.findViewById(R.id.item_reaction_icon);
		description = itemView.findViewById(R.id.item_reaction_text);
		root = itemView.findViewById(R.id.item_reaction_root);
		picasso = PicassoBuilder.get(parent.getContext());
		settings = GlobalSettings.get(parent.getContext());
		emojiLoader = new TextEmojiLoader(parent.getContext());
		iconSize = parent.getResources().getDimensionPixelSize(R.dimen.item_reaction_size_icon);
		this.listener = listener;

		description.setTextColor(settings.getTextColor());

		root.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.item_reaction_root) {
			int position = getLayoutPosition();
			if (position != RecyclerView.NO_POSITION) {
				listener.onItemClick(position, OnHolderClickListener.ANNOUNCEMENT_REACTION);
			}
		}
	}


	@Override
	public void onResult(@NonNull TextEmojiLoader.Result result) {
		if (result.id == tagId && result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(description.getContext(), result.spannable, result.images);
			description.setText(spannable);
		}
	}

	/**
	 *
	 */
	public void setContent(Reaction reaction, Emoji[] emojis) {
		// set reaction text and counts
		SpannableStringBuilder spannableBuilder = new SpannableStringBuilder("");
		if (!reaction.getImageUrl().isEmpty() && settings.imagesEnabled()) {
			icon.setVisibility(View.VISIBLE);
			picasso.load(reaction.getImageUrl()).into(icon);
		} else {
			icon.setVisibility(View.GONE);
			icon.setImageResource(0);
			spannableBuilder.append(reaction.getName()).append(" ");
		}
		spannableBuilder.append(Integer.toString(reaction.getCount()));
		description.setText(spannableBuilder);
		// load emojis
		if (emojis.length > 0 && settings.imagesEnabled()) {
			TextEmojiLoader.Param param = new TextEmojiLoader.Param(tagId, emojis, spannableBuilder, iconSize);
			emojiLoader.execute(param, this);
		}
		if (reaction.isSelected()) {
			root.getBackground().setColorFilter(settings.getHighlightColor() & TRANSPARENCY_MASK, PorterDuff.Mode.SRC_IN);
		} else {
			root.getBackground().clearColorFilter();
		}
	}
}