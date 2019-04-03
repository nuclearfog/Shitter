package org.nuclearfog.twidda.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Trend;

import java.util.List;

public class TrendAdapter extends Adapter<TrendAdapter.ItemHolder> {

    private Trend trends[];
    private OnItemClickListener mListener;
    private int font_color = 0xFFFFFFFF;


    public TrendAdapter(OnItemClickListener mListener) {
        trends = new Trend[0];
        this.mListener = mListener;
    }


    public void setColor(int font_color) {
        this.font_color = font_color;
    }


    public Trend getData(int pos) {
        return trends[pos];
    }


    public void setData(@NonNull List<Trend> trendList) {
        trends = trendList.toArray(trends);
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
                mListener.onItemClick(rv, position);
            }
        });
        return new ItemHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, int index) {
        Trend trend = trends[index];
        String posStr = Integer.toString(trend.getPosition()) + '.';
        vh.trends.setText(trend.getName());
        vh.pos.setText(posStr);
        vh.trends.setTextColor(font_color);
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