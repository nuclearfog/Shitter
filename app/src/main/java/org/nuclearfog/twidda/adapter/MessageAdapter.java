package org.nuclearfog.twidda.adapter;

import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.backend.helper.StringTools;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class MessageAdapter extends Adapter<MessageAdapter.MessageHolder> {

    private WeakReference<OnItemSelected> itemClickListener;
    private List<Message> messages;
    private GlobalSettings settings;


    public MessageAdapter(OnItemSelected l, GlobalSettings settings) {
        itemClickListener = new WeakReference<>(l);
        messages = new ArrayList<>();
        this.settings = settings;
    }


    @MainThread
    public void replaceAll(@NonNull List<Message> messageList) {
        messages.clear();
        messages.addAll(messageList);
        notifyDataSetChanged();
    }


    @MainThread
    public void remove(long id) {
        int pos = -1;
        for (int index = 0; index < messages.size() && pos < 0; index++) {
            if (messages.get(index).getId() == id) {
                messages.remove(index);
                pos = index;
            }
        }
        if (pos != -1)
            notifyItemRemoved(pos);
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
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dm, parent, false);
        final MessageHolder vh = new MessageHolder(view);
        FontTool.setViewFont(settings, view);

        vh.message.setMovementMethod(LinkMovementMethod.getInstance());
        vh.answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener.get() != null) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION)
                        itemClickListener.get().onClick(messages.get(position), OnItemSelected.Action.ANSWER);
                }
            }
        });
        vh.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener.get() != null) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION)
                        itemClickListener.get().onClick(messages.get(position), OnItemSelected.Action.DELETE);
                }
            }
        });
        vh.profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener.get() != null) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION)
                        itemClickListener.get().onClick(messages.get(position), OnItemSelected.Action.PROFILE);
                }
            }
        });
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull MessageHolder vh, int index) {
        Spanned text;
        Message message = messages.get(index);
        TwitterUser sender = message.getSender();
        if (itemClickListener.get() != null)
            text = Tagger.makeTextWithLinks(message.getText(), settings.getHighlightColor(), itemClickListener.get());
        else
            text = Tagger.makeTextWithLinks(message.getText(), settings.getHighlightColor());

        vh.message.setText(text);
        vh.username.setText(sender.getUsername());
        vh.screenname.setText(sender.getScreenname());
        vh.createdAt.setText(StringTools.getTimeString(message.getTime()));
        vh.receivername.setText(message.getReceiver().getScreenname());

        if (sender.isVerified())
            vh.username.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
        else
            vh.username.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        if (sender.isLocked())
            vh.screenname.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
        else
            vh.screenname.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        if (settings.getImageLoad())
            Picasso.get().load(sender.getImageLink() + "_mini").into(vh.profile_img);
    }


    static class MessageHolder extends ViewHolder {
        final ImageView profile_img;
        final TextView username;
        final TextView screenname;
        final TextView receivername;
        final TextView createdAt;
        final TextView message;
        final Button answer;
        final Button delete;

        MessageHolder(View v) {
            super(v);
            profile_img = v.findViewById(R.id.dm_profileImg);
            username = v.findViewById(R.id.dm_username);
            screenname = v.findViewById(R.id.dm_screenname);
            receivername = v.findViewById(R.id.dm_receiver);
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