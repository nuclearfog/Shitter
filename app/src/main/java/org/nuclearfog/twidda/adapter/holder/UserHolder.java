package org.nuclearfog.twidda.adapter.holder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.graphics.PorterDuff.Mode.SRC_IN;

/**
 * View holder class for user item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.UserAdapter
 */
public class UserHolder extends RecyclerView.ViewHolder {

    public final TextView[] textViews = new TextView[4];
    public final ImageView profileImg, verifyIcon, lockedIcon;
    public final ImageButton delete;

    public UserHolder(View v, GlobalSettings settings) {
        super(v);
        CardView background = (CardView) v;
        ImageView followingIcon = v.findViewById(R.id.following_icon);
        ImageView followerIcon = v.findViewById(R.id.follower_icon);
        textViews[0] = v.findViewById(R.id.username_detail);
        textViews[1] = v.findViewById(R.id.screenname_detail);
        textViews[2] = v.findViewById(R.id.item_user_friends);
        textViews[3] = v.findViewById(R.id.item_user_follower);
        profileImg = v.findViewById(R.id.user_profileimg);
        verifyIcon = v.findViewById(R.id.useritem_verified);
        lockedIcon = v.findViewById(R.id.useritem_locked);
        delete = v.findViewById(R.id.useritem_del_user);

        followerIcon.setImageResource(R.drawable.follower);
        followingIcon.setImageResource(R.drawable.following);
        verifyIcon.setImageResource(R.drawable.verify);
        lockedIcon.setImageResource(R.drawable.lock);
        delete.setImageResource(R.drawable.cross);

        background.setCardBackgroundColor(settings.getCardColor());
        followerIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        followingIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        verifyIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        lockedIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        delete.setColorFilter(settings.getIconColor(), SRC_IN);

        AppStyles.setButtonColor(delete, settings.getFontColor());
        for (TextView tv : textViews) {
            tv.setTextColor(settings.getFontColor());
            tv.setTypeface(settings.getFontFace());
        }
    }
}