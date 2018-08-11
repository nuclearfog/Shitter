package org.nuclearfog.twidda.viewadapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class LogAdapter extends Adapter<LogAdapter.ItemHolder> {

    private List<String> messages;

    public LogAdapter(List<String> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @NonNull
    @Override
    public LogAdapter.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, int index) {
        vh.message.setText(messages.get(index));
        vh.message.setTextColor(0xffff0000);
        vh.message.setTextSize(12.0f);
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        public final TextView message;
        ItemHolder(View v) {
            super(v);
            message = v.findViewById(android.R.id.text1);
        }
    }
}