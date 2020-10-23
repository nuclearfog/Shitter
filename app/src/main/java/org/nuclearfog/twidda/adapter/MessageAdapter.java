package org.nuclearfog.twidda.adapter;

import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.utils.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.ArrayList;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static org.nuclearfog.twidda.backend.utils.TimeString.getTimeString;

/**
 * Adapter class for direct messages list
 *
 * @see org.nuclearfog.twidda.fragment.MessageFragment
 */
public class MessageAdapter extends Adapter<ViewHolder> {

    private OnItemSelected itemClickListener;
    private GlobalSettings settings;

    private List<Message> messages;


    public MessageAdapter(OnItemSelected itemClickListener, GlobalSettings settings) {
        this.itemClickListener = itemClickListener;
        this.settings = settings;
        messages = new ArrayList<>();
    }

    /**
     * replace all messages from list
     *
     * @param messageList new message list
     */
    @MainThread
    public void replaceAll(@NonNull List<Message> messageList) {
        messages.clear();
        messages.addAll(messageList);
        notifyDataSetChanged();
    }

    /**
     * Remove a single item from list if found
     *
     * @param id message ID
     */
    @MainThread
    public void remove(long id) {
        int pos = -1;
        for (int index = 0; index < messages.size() && pos < 0; index++) {
            if (messages.get(index).getId() == id) {
                messages.remove(index);
                pos = index;
            }
        }
        if (pos != -1) {
            notifyItemRemoved(pos);
        }
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dm, parent, false);
        final MessageHolder vh = new MessageHolder(view);
        FontTool.setViewFontAndColor(settings, view);

        vh.message.setMovementMethod(LinkMovementMethod.getInstance());
        vh.answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = vh.getLayoutPosition();
                if (position != NO_POSITION) {
                    itemClickListener.onClick(messages.get(position), OnItemSelected.Action.ANSWER);
                }
            }
        });
        vh.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = vh.getLayoutPosition();
                if (position != NO_POSITION) {
                    itemClickListener.onClick(messages.get(position), OnItemSelected.Action.DELETE);
                }
            }
        });
        vh.profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = vh.getLayoutPosition();
                if (position != NO_POSITION) {
                    itemClickListener.onClick(messages.get(position), OnItemSelected.Action.PROFILE);
                }
            }
        });
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int index) {
        Spanned text;
        Message message = messages.get(index);
        TwitterUser sender = message.getSender();
        text = Tagger.makeTextWithLinks(message.getText(), settings.getHighlightColor(), itemClickListener);

        MessageHolder holder = (MessageHolder) vh;
        holder.message.setText(text);
        holder.username.setText(sender.getUsername());
        holder.screenname.setText(sender.getScreenname());
        holder.createdAt.setText(getTimeString(message.getTime()));
        holder.receivername.setText(message.getReceiver().getScreenname());

        setIcon(holder.username, sender.isVerified() ? R.drawable.verify : 0);
        setIcon(holder.screenname, sender.isLocked() ? R.drawable.lock : 0);

        if (settings.getImageLoad()) {
            String pbLink = sender.getImageLink();
            if (!sender.hasDefaultProfileImage()) {
                pbLink += "_mini";
            }
            Picasso.get().load(pbLink).error(R.drawable.no_image).into(holder.profile_img);
        }
    }

    /**
     * sets an icon to a extview
     *
     * @param tv   textview
     * @param icon icon drawable
     */
    private void setIcon(TextView tv, @DrawableRes int icon) {
        tv.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
    }

    /**
     * Holder class for a message view
     */
    private final class MessageHolder extends ViewHolder {
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

    /**
     * callback for the click listener
     */
    public interface OnItemSelected extends OnTagClickListener {

        enum Action {
            ANSWER,
            DELETE,
            PROFILE
        }

        /**
         * called when a button was clicked
         *
         * @param message Message information
         * @param action  what button was clicked
         */
        void onClick(Message message, Action action);
    }
}