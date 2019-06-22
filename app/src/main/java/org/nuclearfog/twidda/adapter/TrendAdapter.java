package org.nuclearfog.twidda.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Trend;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TrendAdapter extends Adapter<TrendAdapter.ItemHolder> {


    private WeakReference<OnItemClickListener> itemClickListener;
    private List<Trend> trends;
    private int font_color;


    public TrendAdapter(OnItemClickListener l) {
        itemClickListener = new WeakReference<>(l);
        trends = new ArrayList<>();
        font_color = Color.WHITE;
    }


    public void setColor(int font_color) {
        this.font_color = font_color;
    }


    public Trend getData(int index) {
        return trends.get(index);
    }


    public void setData(@NonNull List<Trend> trendList) {
        trends.clear();
        trends.addAll(trendList);
        notifyDataSetChanged();
    }


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
    public ItemHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trend, parent, false);
        return new ItemHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, final int index) {
        Trend trend = trends.get(index);
        String posStr = trend.getPosition();
        vh.trends.setText(trend.getName());
        vh.trends.setTextColor(font_color);
        vh.pos.setText(posStr);
        vh.pos.setTextColor(font_color);
        vh.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener.get() != null)
                    itemClickListener.get().onItemClick(index);
            }
        });
    }


    class ItemHolder extends ViewHolder {
        final TextView trends, pos;

        ItemHolder(View v) {
            super(v);
            pos = v.findViewById(R.id.trendpos);
            trends = v.findViewById(R.id.trendname);
        }
    }
}