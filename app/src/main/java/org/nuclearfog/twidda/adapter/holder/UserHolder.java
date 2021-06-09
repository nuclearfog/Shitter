package org.nuclearfog.twidda.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

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
public class UserHolder extends ViewHolder {

    public final TextView[] textViews = new TextView[4];
    public final ImageView profileImg, verifyIcon, lockedIcon;
    public final ImageButton delete;

    /**
     * @param parent Parent view from adapter
     */
    public UserHolder(ViewGroup parent, GlobalSettings settings) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
        // get views
        CardView background = (CardView) itemView;
        ImageView followingIcon = itemView.findViewById(R.id.following_icon);
        ImageView followerIcon = itemView.findViewById(R.id.follower_icon);
        textViews[0] = itemView.findViewById(R.id.username_detail);
        textViews[1] = itemView.findViewById(R.id.screenname_detail);
        textViews[2] = itemView.findViewById(R.id.item_user_friends);
        textViews[3] = itemView.findViewById(R.id.item_user_follower);
        profileImg = itemView.findViewById(R.id.user_profileimg);
        verifyIcon = itemView.findViewById(R.id.useritem_verified);
        lockedIcon = itemView.findViewById(R.id.useritem_locked);
        delete = itemView.findViewById(R.id.useritem_del_user);
        // set view icons
        followerIcon.setImageResource(R.drawable.follower);
        followingIcon.setImageResource(R.drawable.following);
        verifyIcon.setImageResource(R.drawable.verify);
        lockedIcon.setImageResource(R.drawable.lock);
        delete.setImageResource(R.drawable.cross);
        // theme views
        background.setCardBackgroundColor(settings.getCardColor());
        followerIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        followingIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        verifyIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        lockedIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        delete.setColorFilter(settings.getIconColor(), SRC_IN);
        AppStyles.setButtonColor(delete, settings.getFontColor());
        for (TextView tv : textViews) {
            tv.setTextColor(settings.getFontColor());
            tv.setTypeface(settings.getTypeFace());
        }
    }
}