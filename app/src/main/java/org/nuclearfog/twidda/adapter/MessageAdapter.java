package org.nuclearfog.twidda.adapter;

import android.graphics.Color;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.TimeFormat;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.backend.items.TwitterUser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends Adapter<MessageAdapter.MessageHolder> {

    private WeakReference<OnItemSelected> itemClickListener;
    private List<Message> messages;
    private int highlight;
    private int fontColor;
    private boolean loadImage;


    public MessageAdapter(OnItemSelected l) {
        itemClickListener = new WeakReference<>(l);
        messages = new ArrayList<>();
        fontColor = Color.WHITE;
        loadImage = true;
    }


    public Message getData(int index) {
        return messages.get(index);
    }


    public void setData(@NonNull List<Message> messageList) {
        messages.clear();
        messages.addAll(messageList);
        notifyDataSetChanged();
    }


    public void toggleImage(boolean loadImage) {
        this.loadImage = loadImage;
    }


    public void setColor(int fontColor, int highlight) {
        this.fontColor = fontColor;
        this.highlight = highlight;
    }


    @Override
    public long getItemId(int index) {
        return messages.get(index).getId();
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }


    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        View view = inf.inflate(R.layout.item_dm, parent, false);
        return new MessageHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MessageHolder vh, final int index) {
        Spanned text;
        Message message = messages.get(index);
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
        vh.createdAt.setText(TimeFormat.getString(message.getTime()));
        vh.message.setTextColor(fontColor);
        vh.username.setTextColor(fontColor);
        vh.screenname.setTextColor(fontColor);
        vh.createdAt.setTextColor(fontColor);
        vh.answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener.get() != null)
                    itemClickListener.get().onClick(messages.get(index), OnItemSelected.Action.ANSWER);
            }
        });
        vh.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener.get() != null)
                    itemClickListener.get().onClick(messages.get(index), OnItemSelected.Action.DELETE);
            }
        });
        vh.profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener.get() != null)
                    itemClickListener.get().onClick(messages.get(index), OnItemSelected.Action.PROFILE);
            }
        });
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


    class MessageHolder extends ViewHolder {
        final ImageView profile_img;
        final TextView username;
        final TextView screenname;
        final TextView createdAt;
        final TextView message;
        final Button answer;
        final Button delete;

        MessageHolder(View v) {
            super(v);
            profile_img = v.findViewById(R.id.dm_profileImg);
            username = v.findViewById(R.id.dm_username);
            screenname = v.findViewById(R.id.dm_screenname);
            createdAt = v.findViewById(R.id.dm_time);
            message = v.findViewById(R.id.dm_message);
            answer = v.findViewById(R.id.dm_answer);
            delete = v.findViewById(R.id.dm_delete);
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