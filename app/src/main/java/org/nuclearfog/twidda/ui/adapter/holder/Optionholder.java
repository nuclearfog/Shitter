package org.nuclearfog.twidda.ui.adapter.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Poll.Option;

/**
 * This holder if for a single poll option
 *
 * @author nuclearfog
 */
public class Optionholder extends ViewHolder implements OnClickListener {

	private SeekBar voteProgress;
	private TextView optionName, optionVotes;
	private ImageView checkIcon;

	private OnHolderClickListener listener;
	private GlobalSettings settings;

	/**
	 *
	 */
	public Optionholder(ViewGroup parent, GlobalSettings settings, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent, false));
		optionName = itemView.findViewById(R.id.item_option_name);
		checkIcon = itemView.findViewById(R.id.item_option_voted_icon);
		voteProgress = itemView.findViewById(R.id.item_option_count_bar);
		optionVotes = itemView.findViewById(R.id.item_option_count_text);
		this.settings = settings;
		this.listener = listener;

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

	/**
	 * set viewholder content
	 *
	 * @param option     poll option content
	 * @param selected   true if option is selected
	 * @param totalCount total vote count
	 */
	public void setContent(Option option, boolean selected, int totalCount) {
		if (option.isSelected() | selected) {
			checkIcon.setImageResource(R.drawable.check);
		} else {
			checkIcon.setImageResource(R.drawable.circle);
		}
		voteProgress.setMax(Math.max(totalCount, 1));
		AppStyles.setDrawableColor(checkIcon, settings.getIconColor());
		optionName.setText(option.getTitle());
		voteProgress.setProgress(option.getVotes());
		optionVotes.setText(StringUtils.NUMBER_FORMAT.format(option.getVotes()));
	}
}