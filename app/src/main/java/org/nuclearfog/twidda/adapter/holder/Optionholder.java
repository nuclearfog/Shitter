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
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Poll;

import java.text.NumberFormat;


public class Optionholder extends ViewHolder implements View.OnClickListener {

	private static final NumberFormat NUM_FORMAT = NumberFormat.getIntegerInstance();

	private SeekBar count;
	private TextView name, votes;
	private ImageView checked;

	@Nullable
	private OnOptionItemClick listener;
	private GlobalSettings settings;


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


	public void setOnOptionItemClickListener(OnOptionItemClick listener) {
		this.listener = listener;
	}


	public void setContent(Poll.Option option, int totalCount) {
		if (option.selected())
			checked.setImageResource(R.drawable.check);
		else
			checked.setImageResource(R.drawable.circle);
		AppStyles.setDrawableColor(checked, settings.getIconColor());
		name.setText(option.getTitle());
		count.setMax(totalCount);
		count.setProgress(option.getVotes());
		votes.setText(NUM_FORMAT.format(option.getVotes()));
	}


	public interface OnOptionItemClick {

		void onOptionClick(int pos);
	}
}