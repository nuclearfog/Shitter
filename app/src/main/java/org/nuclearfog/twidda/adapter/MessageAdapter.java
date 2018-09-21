package org.nuclearfog.twidda.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    private List<Message> messages;
    private OnItemSelected mListener;
    private boolean loadImage = true;
    private int color = 0xFFFFFFFF;


    public MessageAdapter(OnItemSelected listener) {
        messages = new ArrayList<>();
        this.mListener = listener;
    }


    public List<Message> getData() {
        return messages;
    }


    public void setData(List<Message> messages) {
        this.messages = messages;
    }


    public void setImageLoad(boolean loadImage) {
        this.loadImage = loadImage;
    }


    public void setColor(int color) {
        this.color = color;
    }


    @Override
    public long getItemId(int pos) {
        return messages.get(pos).messageId;
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }


    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dm, parent, false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView rv = (RecyclerView) parent;
                int position = rv.getChildLayoutPosition(v);
                mListener.onSelected(position);
            }
        });
        return new MessageHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull MessageHolder vh, int index) {
        Message message = messages.get(index);
        vh.message.setText(message.message);
        vh.username.setText(message.sender.username);
        vh.screenname.setText(message.sender.screenname);
        vh.createdAt.setText(stringTime(message.time));

        vh.message.setTextColor(color);
        vh.username.setTextColor(color);
        vh.screenname.setTextColor(color);
        vh.createdAt.setTextColor(color);

        if (loadImage)
            Picasso.get().load(message.sender.profileImg + "_mini").into(vh.profile_img);
    }


    private String stringTime(long mills) {
        Calendar now = Calendar.getInstance();
        long diff = now.getTimeInMillis() - mills;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        if (weeks > 4) {
            Date tweetDate = new Date(mills);
            return SimpleDateFormat.getDateInstance().format(tweetDate);
        }
        if (weeks > 0)
            return weeks + " w";
        if (days > 0)
            return days + " d";
        if (hours > 0)
            return hours + " h";
        if (minutes > 0)
            return minutes + " m";
        else
            return seconds + " s";
    }


    public interface OnItemSelected {
        void onSelected(int pos);
    }


    class MessageHolder extends ViewHolder {
        final ImageView profile_img;
        final TextView username;
        final TextView screenname;
        final TextView createdAt;
        final TextView message;

        MessageHolder(View v) {
            super(v);
            profile_img = v.findViewById(R.id.dm_profileImg);
            username = v.findViewById(R.id.dm_username);
            screenname = v.findViewById(R.id.dm_screenname);
            createdAt = v.findViewById(R.id.dm_time);
            message = v.findViewById(R.id.dm_message);
        }
    }
}