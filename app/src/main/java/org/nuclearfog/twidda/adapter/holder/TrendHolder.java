package org.nuclearfog.twidda.adapter.holder;


import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Trend;

/**
 * ViewHolder for a trend item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.TrendAdapter
 */
public class TrendHolder extends ViewHolder implements OnClickListener {

	private TextView name, rank, vol;

	private OnHolderClickListener listener;

	/**
	 * @param parent Parent view from adapter
	 */
	public TrendHolder(ViewGroup parent, GlobalSettings settings) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trend, parent, false));
		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_trend_container);
		rank = itemView.findViewById(R.id.item_trend_rank);
		name = itemView.findViewById(R.id.item_trend_name);
		vol = itemView.findViewById(R.id.item_trend_vol);

		AppStyles.setTheme(container, Color.TRANSPARENT);
		background.setCardBackgroundColor(settings.getCardColor());
		itemView.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v == itemView) {
			int position = getLayoutPosition();
			if (position != NO_POSITION && listener != null) {
				listener.onItemClick(position, OnHolderClickListener.NO_TYPE);
			}
		}
	}

	/**
	 * set item click listener
	 */
	public void setOnTrendClickListener(OnHolderClickListener listener) {
		this.listener = listener;
	}

	/**
	 * set view content
	 *
	 * @param trend content information
	 */
	public void setContent(Trend trend) {
		rank.setText(trend.getRank() + ".");
		name.setText(trend.getName());
		if (trend.getPopularity() > 0) {
			Resources resources = vol.getResources();
			String trendVol = StringTools.NUMBER_FORMAT.format(trend.getPopularity()) + resources.getString(R.string.trend_range);
			vol.setText(trendVol);
			vol.setVisibility(View.VISIBLE);
		} else {
			vol.setVisibility(View.GONE);
		}
	}
}