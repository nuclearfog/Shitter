package org.nuclearfog.twidda.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * ViewHolder for a trend item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.TrendAdapter
 */
public class TrendHolder extends ViewHolder {

	public final TextView name, rank, vol;

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

		AppStyles.setTheme(container, 0);
		background.setCardBackgroundColor(settings.getCardColor());
	}
}