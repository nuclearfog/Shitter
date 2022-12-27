package org.nuclearfog.twidda.adapter.holder;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Poll;

/**
 * This holder if for a single poll option
 *
 * @author nuclearfog
 */
public class Optionholder extends ViewHolder implements View.OnClickListener {

	private SeekBar count;
	private TextView name, votes;
	private ImageView checked;

	@Nullable
	private OnOptionItemClick listener;
	private GlobalSettings settings;

	/**
	 *
	 */
	public Optionholder(ViewGroup parent, GlobalSettings settings) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent, false));
		name = itemView.findViewById(R.id.item_option_name);
		checked = itemView.findViewById(R.id.item_option_voted_icon);
		count = itemView.findViewById(R.id.item_option_count_bar);
		votes = itemView.findViewById(R.id.item_option_count_text);

		name.setTextColor(settings.getFontColor());
		votes.setTextColor(settings.getFontColor());
		AppStyles.setSeekBarColor(count, settings);

		checked.setOnClickListener(this);
		this.settings = settings;
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != NO_POSITION && listener != null) {
			if (v == checked) {
				listener.onOptionClick(position);
			}
		}
	}

	/**
	 * set viewholder click listener
	 */
	public void setOnOptionItemClickListener(OnOptionItemClick listener) {
		this.listener = listener;
	}

	/**
	 * set viewholder content
	 *
	 * @param option     poll option content
	 * @param totalCount total vote count
	 */
	public void setContent(Poll.Option option, int totalCount) {
		if (option.selected())
			checked.setImageResource(R.drawable.check);
		else
			checked.setImageResource(R.drawable.circle);
		AppStyles.setDrawableColor(checked, settings.getIconColor());
		name.setText(option.getTitle());
		count.setMax(totalCount);
		count.setProgress(option.getVotes());
		votes.setText(StringTools.NUMBER_FORMAT.format(option.getVotes()));
	}

	/**
	 * viewholder click listener
	 */
	public interface OnOptionItemClick {

		/**
		 * called on item click
		 *
		 * @param pos adapter position of the item
		 */
		void onOptionClick(int pos);
	}
}