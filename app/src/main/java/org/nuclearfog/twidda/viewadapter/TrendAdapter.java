package org.nuclearfog.twidda.viewadapter;

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
import org.nuclearfog.twidda.backend.listitems.Trend;

import java.util.ArrayList;
import java.util.List;

public class TrendAdapter extends Adapter<TrendAdapter.ItemHolder> {

    private List<Trend> trendList;
    private OnItemClicked mListener;
    private int font_color = 0xFFFFFFFF;


    public TrendAdapter(OnItemClicked mListener) {
        trendList = new ArrayList<>();
        this.mListener = mListener;
    }


    public void setColor(int font_color) {
        this.font_color = font_color;
    }


    public void setData(List<Trend> trendList) {
        this.trendList = trendList;
    }


    public List<Trend> getData() {
        return trendList;
    }


    @Override
    public int getItemCount() {
        return trendList.size();
    }


    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull final ViewGroup parent, int index) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trend,parent,false);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView rv = (RecyclerView) parent;
                int position = rv.getChildLayoutPosition(v);
                mListener.onItemClick(parent, position);
            }
        });
        return new ItemHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, int index) {
        String posStr = Integer.toString(trendList.get(index).position)+'.';
        vh.trends.setText(trendList.get(index).trend);
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


    /**
     * Custom Click Listener
     */
    public interface OnItemClicked {
        void onItemClick(ViewGroup parent, int position);
    }
}