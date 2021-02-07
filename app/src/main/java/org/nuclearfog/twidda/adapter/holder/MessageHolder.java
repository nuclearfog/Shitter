package org.nuclearfog.twidda.adapter.holder;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

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
public class MessageHolder extends RecyclerView.ViewHolder {

    public final TextView[] textViews = new TextView[5];
    public final Button[] buttons = new Button[2];
    public final ImageView profile_img, verifiedIcon, lockedIcon;

    public MessageHolder(View v, GlobalSettings settings) {
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
            AppStyles.setButtonColor(button, settings.getFontColor());
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