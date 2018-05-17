package org.nuclearfog.twidda.viewadapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nuclearfog.twidda.R;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ItemHolder> {

    private List<String> messages;

    public LogAdapter(List<String> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemCount(){return messages.size();}

    @Override
    public LogAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int index) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_error, parent,false);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(LogAdapter.ItemHolder vh, int index) {
        vh.message.setText(messages.get(index));
        vh.message.setTextColor(0xffff0000);
    }


    class ItemHolder extends RecyclerView.ViewHolder {
        public final TextView message;
        ItemHolder(View v) {
            super(v);
            message = v.findViewById(R.id.errortext);
        }
    }

}
