package org.nuclearfog.twidda.adapter;

import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.backend.items.TwitterUser;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    private WeakReference<OnItemSelected> itemClickListener;
    private Message[] messages;
    private int highlight;
    private int fontColor;
    private boolean loadImage;


    public MessageAdapter(OnItemSelected l) {
        itemClickListener = new WeakReference<>(l);
        messages = new Message[0];
        fontColor = 0xFFFFFFFF;
        loadImage = true;
    }


    public Message getData(int pos) {
        return messages[pos];
    }


    public void setData(@NonNull List<Message> messageList) {
        messages = messageList.toArray(new Message[0]);
    }


    public void toggleImage(boolean loadImage) {
        this.loadImage = loadImage;
    }


    public void setColor(int fontColor, int highlight) {
        this.fontColor = fontColor;
        this.highlight = highlight;
    }


    @Override
    public long getItemId(int pos) {
        return messages[pos].getId();
    }


    @Override
    public int getItemCount() {
        return messages.length;
    }


    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        final View view = inf.inflate(R.layout.item_dm, parent, false);
        view.findViewById(R.id.dm_answer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView rv = (RecyclerView) parent;
                int position = rv.getChildLayoutPosition(view);
                if (itemClickListener.get() != null)
                    itemClickListener.get().onClick(messages[position], OnItemSelected.Action.ANSWER);
            }
        });
        view.findViewById(R.id.dm_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView rv = (RecyclerView) parent;
                int position = rv.getChildLayoutPosition(view);
                if (itemClickListener.get() != null)
                    itemClickListener.get().onClick(messages[position], OnItemSelected.Action.DELETE);
            }
        });
        view.findViewById(R.id.dm_profileImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView rv = (RecyclerView) parent;
                int position = rv.getChildLayoutPosition(view);
                if (itemClickListener.get() != null)
                    itemClickListener.get().onClick(messages[position], OnItemSelected.Action.PROFILE);
            }
        });
        return new MessageHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MessageHolder vh, int index) {
        Spanned text;
        Message message = messages[index];
        TwitterUser user = message.getSender();
        if (itemClickListener.get() != null)
            text = Tagger.makeText(message.getText(), highlight, itemClickListener.get());
        else
            text = Tagger.makeText(message.getText(), highlight);

        vh.message.setText(text);
        vh.message.setMovementMethod(LinkMovementMethod.getInstance());
        vh.message.setLinkTextColor(highlight);
        vh.username.setText(user.getUsername());
        vh.screenname.setText(user.getScreenname());
        vh.createdAt.setText(stringTime(message.getTime()));

        vh.message.setTextColor(fontColor);
        vh.username.setTextColor(fontColor);
        vh.screenname.setTextColor(fontColor);
        vh.createdAt.setTextColor(fontColor);

        if (user.isVerified())
            vh.username.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
        else
            vh.username.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        if (user.isLocked())
            vh.screenname.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
        else
            vh.screenname.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        if (loadImage) {
            String link = message.getSender().getImageLink() + "_mini";
            Picasso.get().load(link).into(vh.profile_img);
        }
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


    public interface OnItemSelected extends OnTagClickListener {

        enum Action {
            ANSWER,
            DELETE,
            PROFILE
        }

        void onClick(Message message, Action action);
    }
}