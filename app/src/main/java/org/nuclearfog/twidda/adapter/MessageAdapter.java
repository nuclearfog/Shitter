package org.nuclearfog.twidda.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
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
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
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

import static android.graphics.PorterDuff.Mode.SRC_ATOP;
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
    private Drawable[] icons;

    private List<Message> messages = new ArrayList<>();


    public MessageAdapter(Context context, OnItemSelected itemClickListener) {
        this.itemClickListener = itemClickListener;
        settings = GlobalSettings.getInstance(context);

        TypedArray drawables = context.getResources().obtainTypedArray(R.array.dm_item_icons);
        icons = new Drawable[drawables.length()];
        for (int index = 0; index < drawables.length(); index++) {
            int resId = drawables.getResourceId(index, 0);
            icons[index] = AppCompatResources.getDrawable(context, resId);
        }
        drawables.recycle();
        setIconColor();
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
            setIcon(holder.textViews[0], icons[0]);
        } else {
            setIcon(holder.textViews[0], null);
        }
        if (sender.isLocked()) {
            setIcon(holder.textViews[1], icons[1]);
        } else {
            setIcon(holder.textViews[1], null);
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
     * sets an icon to a TextView
     *
     * @param tv   TextView to set an icon
     * @param icon icon drawable
     */
    private void setIcon(TextView tv, @Nullable Drawable icon) {
        if (icon != null)
            icon = icon.mutate();
        tv.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
    }

    /**
     * set color for all icons
     */
    private void setIconColor() {
        for (Drawable icon : icons) {
            icon.setColorFilter(settings.getIconColor(), SRC_ATOP);
        }
    }

    /**
     * Holder class for a message view
     */
    private final class MessageHolder extends ViewHolder {

        final TextView[] textViews = new TextView[5];
        final Button[] buttons = new Button[2];
        final ImageView profile_img;

        MessageHolder(View v, GlobalSettings settings) {
            super(v);
            CardView background = (CardView) v;
            profile_img = v.findViewById(R.id.dm_profileImg);
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
            background.setCardBackgroundColor(settings.getCardColor());
            textViews[2].setCompoundDrawables(icons[2], null, null, null);
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