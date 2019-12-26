package org.nuclearfog.twidda.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TrendAdapter extends Adapter<TrendAdapter.ItemHolder> {

    private WeakReference<OnItemClickListener> itemClickListener;
    private List<String> trends;
    private GlobalSettings settings;

    public TrendAdapter(OnItemClickListener l, GlobalSettings settings) {
        itemClickListener = new WeakReference<>(l);
        trends = new ArrayList<>();
        this.settings = settings;
    }

    public String getData(int index) {
        return trends.get(index);
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
    public ItemHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trend, parent, false);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener.get() != null) {
                    RecyclerView rv = (RecyclerView) parent;
                    int index = rv.getChildLayoutPosition(v);
                    itemClickListener.get().onItemClick(index);
                }
            }
        });
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, final int index) {
        String posStr = index + 1 + ".";
        vh.pos.setText(posStr);
        vh.trends.setText(trends.get(index));
        vh.pos.setTextColor(settings.getFontColor());
        vh.trends.setTextColor(settings.getFontColor());
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