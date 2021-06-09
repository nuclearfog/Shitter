package org.nuclearfog.twidda.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * ViewHolder for a trend item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.TrendAdapter
 */
public class TrendHolder extends ViewHolder {

    public final TextView[] textViews = new TextView[3];

    /**
     * @param parent Parent view from adapter
     */
    public TrendHolder(ViewGroup parent, GlobalSettings settings) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trend, parent, false));
        // get views
        CardView background = (CardView) itemView;
        textViews[0] = itemView.findViewById(R.id.trendpos);
        textViews[1] = itemView.findViewById(R.id.trendname);
        textViews[2] = itemView.findViewById(R.id.trendvol);
        // theme views
        background.setCardBackgroundColor(settings.getCardColor());
        for (TextView tv : textViews) {
            tv.setTextColor(settings.getFontColor());
            tv.setTypeface(settings.getTypeFace());
        }
    }
}