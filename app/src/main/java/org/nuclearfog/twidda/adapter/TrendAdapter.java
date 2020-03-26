package org.nuclearfog.twidda.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.backend.items.TwitterTrend;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class TrendAdapter extends Adapter<TrendAdapter.ItemHolder> {

    private WeakReference<TrendClickListener> itemClickListener;
    private List<TwitterTrend> trends;
    private GlobalSettings settings;
    private NumberFormat formatter;


    public TrendAdapter(TrendClickListener l, GlobalSettings settings) {
        itemClickListener = new WeakReference<>(l);
        trends = new ArrayList<>();
        formatter = NumberFormat.getIntegerInstance();
        this.settings = settings;
    }


    @MainThread
    public void setData(@NonNull List<TwitterTrend> trendList) {
        trends.clear();
        trends.addAll(trendList);
        notifyDataSetChanged();
    }


    @MainThread
    public void clear() {
        trends.clear();
        notifyDataSetChanged();
    }


    public boolean isEmpty() {
        return trends.isEmpty();
    }


    @Override
    public int getItemCount() {
        return trends.size();
    }


    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trend, parent, false);
        final ItemHolder vh = new ItemHolder(v);
        FontTool.setViewFontAndColor(settings, v);

        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener.get() != null) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION)
                        itemClickListener.get().onTrendClick(trends.get(position));
                }
            }
        });
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, int index) {
        TwitterTrend trend = trends.get(index);
        vh.pos.setText(trend.getRankStr());
        vh.name.setText(trend.getName());
        if (trend.hasRangeInfo()) {
            Resources resources = vh.vol.getContext().getResources();
            String trendVol = formatter.format(trend.getRange()) + " " + resources.getString(R.string.trend_range);
            vh.vol.setText(trendVol);
            vh.vol.setVisibility(VISIBLE);
        } else {
            vh.vol.setVisibility(GONE);
        }
    }


    static class ItemHolder extends ViewHolder {
        final TextView name, pos, vol;

        ItemHolder(View v) {
            super(v);
            pos = v.findViewById(R.id.trendpos);
            name = v.findViewById(R.id.trendname);
            vol = v.findViewById(R.id.trendvol);
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
        void onTrendClick(TwitterTrend trend);
    }
}