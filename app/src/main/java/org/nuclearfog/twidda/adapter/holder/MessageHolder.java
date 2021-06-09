package org.nuclearfog.twidda.adapter.holder;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.graphics.PorterDuff.Mode.SRC_IN;

/**
 * Holder class for a message view
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.MessageAdapter
 */
public class MessageHolder extends ViewHolder {

    public final TextView[] textViews = new TextView[5];
    public final Button[] buttons = new Button[2];
    public final ImageView profile_img, verifiedIcon, lockedIcon;

    /**
     * @param parent Parent view from adapter
     */
    public MessageHolder(ViewGroup parent, GlobalSettings settings) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dm, parent, false));
        // get views
        CardView background = (CardView) itemView;
        ImageView receiver_icon = itemView.findViewById(R.id.dm_receiver_icon);
        profile_img = itemView.findViewById(R.id.dm_profile_img);
        verifiedIcon = itemView.findViewById(R.id.dm_user_verified);
        lockedIcon = itemView.findViewById(R.id.dm_user_locked);
        textViews[0] = itemView.findViewById(R.id.dm_username);
        textViews[1] = itemView.findViewById(R.id.dm_screenname);
        textViews[2] = itemView.findViewById(R.id.dm_receiver);
        textViews[3] = itemView.findViewById(R.id.dm_time);
        textViews[4] = itemView.findViewById(R.id.dm_message);
        buttons[0] = itemView.findViewById(R.id.dm_answer);
        buttons[1] = itemView.findViewById(R.id.dm_delete);
        // set icons
        receiver_icon.setImageResource(R.drawable.right);
        verifiedIcon.setImageResource(R.drawable.verify);
        lockedIcon.setImageResource(R.drawable.lock);
        // theme views
        for (TextView tv : textViews) {
            tv.setTextColor(settings.getFontColor());
            tv.setTypeface(settings.getTypeFace());
        }
        for (Button button : buttons) {
            button.setTextColor(settings.getFontColor());
            button.setTypeface(settings.getTypeFace());
            AppStyles.setButtonColor(button, settings.getFontColor());
        }
        verifiedIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        lockedIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        receiver_icon.setColorFilter(settings.getIconColor(), SRC_IN);
        background.setCardBackgroundColor(settings.getCardColor());
        // make links clickable
        textViews[4].setMovementMethod(LinkMovementMethod.getInstance());
    }
}