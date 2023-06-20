package org.nuclearfog.twidda.ui.adapter.holder;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Trend;

/**
 * ViewHolder for a trend item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.adapter.TrendAdapter
 */
public class TrendHolder extends ViewHolder implements OnClickListener {

	private TextView name, rank, vol;

	private OnHolderClickListener listener;


	public TrendHolder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trend, parent, false));
		this.listener = listener;

		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_trend_container);
		rank = itemView.findViewById(R.id.item_trend_rank);
		name = itemView.findViewById(R.id.item_trend_name);
		vol = itemView.findViewById(R.id.item_trend_vol);

		GlobalSettings settings = GlobalSettings.get(parent.getContext());
		AppStyles.setTheme(container, Color.TRANSPARENT);
		background.setCardBackgroundColor(settings.getCardColor());
		itemView.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v == itemView) {
			int position = getLayoutPosition();
			if (position != RecyclerView.NO_POSITION) {
				listener.onItemClick(position, OnHolderClickListener.NO_TYPE);
			}
		}
	}

	/**
	 * set view content
	 *
	 * @param trend content information
	 * @param index index of the item
	 */
	public void setContent(Trend trend, int index) {
		rank.setText(index + 1 + ".");
		name.setText(trend.getName());
		if (trend.getPopularity() > 0) {
			Resources resources = vol.getResources();
			String trendVol = StringUtils.NUMBER_FORMAT.format(trend.getPopularity()) + resources.getString(R.string.trend_range);
			vol.setText(trendVol);
			vol.setVisibility(View.VISIBLE);
		} else {
			vol.setVisibility(View.GONE);
		}
	}
}