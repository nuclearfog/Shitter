package org.nuclearfog.twidda.adapter.holder;

import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * ViewHolder for a trend item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.TrendAdapter
 */
public class TrendHolder extends RecyclerView.ViewHolder {

    public final TextView[] textViews = new TextView[3];

    public TrendHolder(View v, GlobalSettings settings) {
        super(v);
        CardView background = (CardView) v;
        textViews[0] = v.findViewById(R.id.trendpos);
        textViews[1] = v.findViewById(R.id.trendname);
        textViews[2] = v.findViewById(R.id.trendvol);

        background.setCardBackgroundColor(settings.getCardColor());
        for (TextView tv : textViews) {
            tv.setTextColor(settings.getFontColor());
            tv.setTypeface(settings.getFontFace());
        }
    }
}