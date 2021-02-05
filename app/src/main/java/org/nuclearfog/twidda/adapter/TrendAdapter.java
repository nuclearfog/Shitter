package org.nuclearfog.twidda.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Trend;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * Adapter class for Trend list
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.fragment.TrendFragment
 */
public class TrendAdapter extends Adapter<ViewHolder> {

    private TrendClickListener itemClickListener;
    private GlobalSettings settings;

    private NumberFormat formatter = NumberFormat.getIntegerInstance();
    private List<Trend> trends = new ArrayList<>();


    public TrendAdapter(GlobalSettings settings, TrendClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        this.settings = settings;
    }

    /**
     * replace data from list
     *
     * @param trendList list of trends
     */
    @MainThread
    public void setData(@NonNull List<Trend> trendList) {
        trends.clear();
        trends.addAll(trendList);
        notifyDataSetChanged();
    }

    /**
     * removes all items from adapter
     */
    @MainThread
    public void clear() {
        trends.clear();
        notifyDataSetChanged();
    }

    /**
     * check if adapter is empty
     *
     * @return true if adapter is empty
     */
    public boolean isEmpty() {
        return trends.isEmpty();
    }


    @Override
    public int getItemCount() {
        return trends.size();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trend, parent, false);
        final ItemHolder vh = new ItemHolder(v, settings);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = vh.getLayoutPosition();
                if (position != NO_POSITION) {
                    itemClickListener.onTrendClick(trends.get(position));
                }
            }
        });
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int index) {
        ItemHolder holder = (ItemHolder) vh;
        Trend trend = trends.get(index);
        holder.textViews[0].setText(trend.getRankStr());
        holder.textViews[1].setText(trend.getName());
        if (trend.hasRangeInfo()) {
            Resources resources = holder.textViews[2].getContext().getResources();
            String trendVol = formatter.format(trend.getRange()) + " " + resources.getString(R.string.trend_range);
            holder.textViews[2].setText(trendVol);
            holder.textViews[2].setVisibility(VISIBLE);
        } else {
            holder.textViews[2].setVisibility(GONE);
        }
    }

    /**
     * view holder class for an item view
     */
    private final class ItemHolder extends ViewHolder {
        final TextView[] textViews = new TextView[3];

        ItemHolder(View v, GlobalSettings settings) {
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


    /**
     * Listener for trend list
     */
    public interface TrendClickListener {

        /**
         * called when trend item is clicked
         *
         * @param trend trend name
         */
        void onTrendClick(Trend trend);
    }
}