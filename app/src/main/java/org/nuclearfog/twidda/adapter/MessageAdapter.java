package org.nuclearfog.twidda.adapter;

import android.content.Context;
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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.backend.items.User;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static android.graphics.PorterDuff.Mode.SRC_IN;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static org.nuclearfog.twidda.backend.utils.StringTools.getTimeString;

/**
 * Adapter class for direct messages list
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.fragment.MessageFragment
 */
public class MessageAdapter extends Adapter<ViewHolder> {

    private OnItemSelected itemClickListener;
    private GlobalSettings settings;

    private List<Message> messages = new ArrayList<>();


    public MessageAdapter(Context context, OnItemSelected itemClickListener) {
        this.itemClickListener = itemClickListener;
        settings = GlobalSettings.getInstance(context);
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
        final MessageHolder vh = new MessageHolder(view, settings);
        vh.buttons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = vh.getLayoutPosition();
                if (position != NO_POSITION) {
                    itemClickListener.onClick(messages.get(position), OnItemSelected.Action.ANSWER);
                }
            }
        });
        vh.buttons[1].setOnClickListener(new View.OnClickListener() {
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
        User sender = message.getSender();
        text = Tagger.makeTextWithLinks(message.getText(), settings.getHighlightColor(), itemClickListener);

        MessageHolder holder = (MessageHolder) vh;
        holder.textViews[0].setText(sender.getUsername());
        holder.textViews[1].setText(sender.getScreenname());
        holder.textViews[2].setText(message.getReceiver().getScreenname());
        holder.textViews[3].setText(getTimeString(message.getTime()));
        holder.textViews[4].setText(text);
        if (sender.isVerified()) {
            holder.verifiedIcon.setVisibility(VISIBLE);
        } else {
            holder.verifiedIcon.setVisibility(GONE);
        }
        if (sender.isLocked()) {
            holder.lockedIcon.setVisibility(VISIBLE);
        } else {
            holder.lockedIcon.setVisibility(GONE);
        }
        if (settings.getImageLoad() && sender.hasProfileImage()) {
            String pbLink = sender.getImageLink();
            if (!sender.hasDefaultProfileImage())
                pbLink += settings.getImageSuffix();
            Picasso.get().load(pbLink).transform(new RoundedCornersTransformation(2, 0))
                    .error(R.drawable.no_image).into(holder.profile_img);
        } else {
            holder.profile_img.setImageResource(0);
        }
    }

    /**
     * Holder class for a message view
     */
    private final class MessageHolder extends ViewHolder {

        final TextView[] textViews = new TextView[5];
        final Button[] buttons = new Button[2];
        final ImageView profile_img, verifiedIcon, lockedIcon;

        MessageHolder(View v, GlobalSettings settings) {
            super(v);
            CardView background = (CardView) v;
            ImageView receiver_icon = v.findViewById(R.id.dm_receiver_icon);
            profile_img = v.findViewById(R.id.dm_profile_img);
            verifiedIcon = v.findViewById(R.id.dm_user_verified);
            lockedIcon = v.findViewById(R.id.dm_user_locked);
            textViews[0] = v.findViewById(R.id.dm_username);
            textViews[1] = v.findViewById(R.id.dm_screenname);
            textViews[2] = v.findViewById(R.id.dm_receiver);
            textViews[3] = v.findViewById(R.id.dm_time);
            textViews[4] = v.findViewById(R.id.dm_message);
            buttons[0] = v.findViewById(R.id.dm_answer);
            buttons[1] = v.findViewById(R.id.dm_delete);

            for (TextView tv : textViews) {
                tv.setTextColor(settings.getFontColor());
                tv.setTypeface(settings.getFontFace());
            }
            for (Button button : buttons) {
                button.setTextColor(settings.getFontColor());
                button.setTypeface(settings.getFontFace());
            }
            receiver_icon.setImageResource(R.drawable.right);
            verifiedIcon.setImageResource(R.drawable.verify);
            lockedIcon.setImageResource(R.drawable.lock);
            verifiedIcon.setColorFilter(settings.getIconColor(), SRC_IN);
            lockedIcon.setColorFilter(settings.getIconColor(), SRC_IN);
            receiver_icon.setColorFilter(settings.getIconColor(), SRC_IN);
            background.setCardBackgroundColor(settings.getCardColor());
            textViews[4].setMovementMethod(LinkMovementMethod.getInstance());
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