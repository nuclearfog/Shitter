package org.nuclearfog.twidda.viewadapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.TrendDatabase;

public class TrendRecycler extends RecyclerView.Adapter<TrendRecycler.ItemHolder>
        implements View.OnClickListener {

    private ViewGroup parent;
    private TrendDatabase trend;
    private OnItemClicked mListener;
    private int font_color = 0xFFFFFFFF;

    public TrendRecycler(TrendDatabase trend, OnItemClicked mListener) {
        this.mListener = mListener;
        this.trend = trend;
    }


    public TrendDatabase getData() { return trend; }


    public void setColor(int font_color) {
        this.font_color = font_color;
    }


    @Override
    public int getItemCount(){
        return trend.getSize();
    }


    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int index) {
        this.parent = parent;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.trend, parent,false);
        v.setOnClickListener(this);
        return new ItemHolder(v);
    }


    @Override
    public void onBindViewHolder(ItemHolder vh, int index) {
        vh.pos.setText(trend.getTrendpos(index));
        vh.pos.setTextColor(font_color);
        vh.trends.setText(trend.getTrendname(index));
        vh.trends.setTextColor(font_color);
    }


    @Override
    public void onClick(View view) {
        ViewGroup p = TrendRecycler.this.parent;
        RecyclerView rv = (RecyclerView) p;
        int position = rv.getChildLayoutPosition(view);
        mListener.onItemClick(view, p, position);
    }


    class ItemHolder extends ViewHolder {
        public TextView trends, pos;
        public ItemHolder(View v) {
            super(v);
            pos = v.findViewById(R.id.trendpos);
            trends = v.findViewById(R.id.trendname);
        }
    }


    /**
     * Custom Click Listener
     */
    public interface OnItemClicked {
        void onItemClick(View v, ViewGroup parent, int position);
    }
}