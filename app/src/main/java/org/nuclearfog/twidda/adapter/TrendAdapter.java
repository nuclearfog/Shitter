package org.nuclearfog.twidda.adapter;

import android.graphics.Typeface;
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
import org.nuclearfog.twidda.database.GlobalSettings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class TrendAdapter extends Adapter<TrendAdapter.ItemHolder> {

    private WeakReference<TrendClickListener> itemClickListener;
    private List<String> trends;
    private GlobalSettings settings;


    public TrendAdapter(TrendClickListener l, GlobalSettings settings) {
        itemClickListener = new WeakReference<>(l);
        trends = new ArrayList<>();
        this.settings = settings;
    }


    @MainThread
    public void setData(@NonNull List<String> trendList) {
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
        Typeface font = settings.getFontFace();
        int color = settings.getFontColor();
        String posStr = index + 1 + ".";
        vh.pos.setTextColor(color);
        vh.trends.setTextColor(color);
        vh.pos.setTypeface(font);
        vh.trends.setTypeface(font);
        vh.pos.setText(posStr);
        vh.trends.setText(trends.get(index));
    }


    class ItemHolder extends ViewHolder {
        final TextView trends, pos;

        ItemHolder(View v) {
            super(v);
            pos = v.findViewById(R.id.trendpos);
            trends = v.findViewById(R.id.trendname);
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
        void onTrendClick(String trend);
    }
}