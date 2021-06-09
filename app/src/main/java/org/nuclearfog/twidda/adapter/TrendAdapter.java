package org.nuclearfog.twidda.adapter;

import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.holder.TrendHolder;
import org.nuclearfog.twidda.backend.model.Trend;
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

    /**
     * Max trend count Twitter API returns
     */
    private static final int INIT_COUNT = 50;

    /**
     * Locale specific number format
     */
    private static final NumberFormat NUM_FORMAT = NumberFormat.getIntegerInstance();


    private TrendClickListener itemClickListener;
    private GlobalSettings settings;
    private List<Trend> trends = new ArrayList<>(INIT_COUNT);

    /**
     * @param itemClickListener Listener for item click
     */
    public TrendAdapter(GlobalSettings settings, TrendClickListener itemClickListener) {
        this.settings = settings;
        this.itemClickListener = itemClickListener;
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
        final TrendHolder vh = new TrendHolder(parent, settings);
        vh.itemView.setOnClickListener(new OnClickListener() {
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
        TrendHolder holder = (TrendHolder) vh;
        Trend trend = trends.get(index);
        holder.textViews[0].setText(trend.getRankStr());
        holder.textViews[1].setText(trend.getName());
        if (trend.hasRangeInfo()) {
            Resources resources = holder.textViews[2].getContext().getResources();
            String trendVol = NUM_FORMAT.format(trend.getRange()) + " " + resources.getString(R.string.trend_range);
            holder.textViews[2].setText(trendVol);
            holder.textViews[2].setVisibility(VISIBLE);
        } else {
            holder.textViews[2].setVisibility(GONE);
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