package org.nuclearfog.twidda.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Trend;

import java.lang.ref.WeakReference;
import java.util.List;

public class TrendAdapter extends Adapter<TrendAdapter.ItemHolder> {


    private WeakReference<OnItemClickListener> itemClickListener;
    private Trend[] trends;
    private int font_color;


    public TrendAdapter(OnItemClickListener l) {
        itemClickListener = new WeakReference<>(l);
        trends = new Trend[0];
        font_color = Color.WHITE;
    }


    public void setColor(int font_color) {
        this.font_color = font_color;
    }


    public Trend getData(int pos) {
        return trends[pos];
    }


    public void setData(@NonNull List<Trend> trendList) {
        trends = trendList.toArray(new Trend[0]);
    }


    public void clear() {
        trends = new Trend[0];
        notifyDataSetChanged();
    }


    public boolean isEmpty() {
        return trends.length == 0;
    }


    @Override
    public int getItemCount() {
        return trends.length;
    }


    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trend, parent, false);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView rv = (RecyclerView) parent;
                int position = rv.getChildLayoutPosition(v);
                if (itemClickListener.get() != null)
                    itemClickListener.get().onItemClick(position);
            }
        });
        return new ItemHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, int index) {
        Trend trend = trends[index];
        String posStr = trend.getPosition();
        vh.trends.setText(trend.getName());
        vh.trends.setTextColor(font_color);
        vh.pos.setText(posStr);
        vh.pos.setTextColor(font_color);
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