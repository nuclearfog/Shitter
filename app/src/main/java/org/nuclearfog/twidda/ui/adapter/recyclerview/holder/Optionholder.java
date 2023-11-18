package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.Param;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.Result;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.PollOption;

/**
 * This holder if for a single poll option
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.adapter.recyclerview.OptionsAdapter
 */
public class Optionholder extends ViewHolder implements OnClickListener, AsyncCallback<Result> {

	private SeekBar voteProgress;
	private TextView optionName, optionVotes;
	private ImageView checkIcon;

	private OnHolderClickListener listener;
	private TextEmojiLoader emojiLoader;
	private GlobalSettings settings;

	private long tagId;

	/**
	 *
	 */
	public Optionholder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent, false));
		settings = GlobalSettings.get(parent.getContext());
		emojiLoader = new TextEmojiLoader(parent.getContext());
		this.listener = listener;

		optionName = itemView.findViewById(R.id.item_option_name);
		checkIcon = itemView.findViewById(R.id.item_option_voted_icon);
		voteProgress = itemView.findViewById(R.id.item_option_count_bar);
		optionVotes = itemView.findViewById(R.id.item_option_count_text);

		optionName.setTextColor(settings.getTextColor());
		optionName.setTypeface(settings.getTypeFace());
		optionVotes.setTextColor(settings.getTextColor());
		optionVotes.setTypeface(settings.getTypeFace());
		AppStyles.setSeekBarColor(voteProgress, settings);
		checkIcon.setColorFilter(settings.getIconColor());
		voteProgress.setPadding(0, 0, 0, 0);

		checkIcon.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != RecyclerView.NO_POSITION) {
			if (v == checkIcon) {
				listener.onItemClick(position, OnHolderClickListener.POLL_OPTION);
			}
		}
	}


	@Override
	public void onResult(@NonNull Result result) {
		if (result.images != null && result.id == tagId) {
			Spannable spannable = EmojiUtils.addEmojis(optionName.getContext(), result.spannable, result.images);
			optionName.setText(spannable);
		}
	}

	/**
	 * set viewholder content
	 *
	 * @param option     poll option content
	 * @param selected   true if option is selected
	 * @param totalCount total vote count
	 */
	public void setContent(PollOption option, Emoji[] emojis, boolean selected, int totalCount) {
		voteProgress.setMax(Math.max(totalCount, 1));
		AppStyles.setDrawableColor(checkIcon, settings.getIconColor());
		voteProgress.setProgress(option.getVotes());
		optionVotes.setText(StringUtils.NUMBER_FORMAT.format(option.getVotes()));
		if (emojis.length > 0 && settings.imagesEnabled()) {
			tagId = option.getTitle().hashCode();
			SpannableString optionSpan = new SpannableString(option.getTitle());
			Param param = new Param(tagId, emojis, optionSpan, optionName.getResources().getDimensionPixelSize(R.dimen.item_option_emoji_size));
			optionName.setText(EmojiUtils.removeTags(optionSpan));
			emojiLoader.execute(param, this);
		} else {
			optionName.setText(option.getTitle());
		}
		if (option.isSelected() | selected) {
			checkIcon.setImageResource(R.drawable.check);
		} else {
			checkIcon.setImageResource(R.drawable.circle);
		}
	}
}