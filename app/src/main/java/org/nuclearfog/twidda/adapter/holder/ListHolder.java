package org.nuclearfog.twidda.adapter.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.graphics.PorterDuff.Mode.SRC_IN;

/**
 * view holder class for an user list item
 *
 * @author nuclearfog
 * @see android.widget.ListAdapter
 */
public class ListHolder extends RecyclerView.ViewHolder {

    public final ImageView[] icons = new ImageView[7];
    public final TextView[] textViews = new TextView[8];
    public final ImageView profile_img;


    public ListHolder(View v, GlobalSettings settings) {
        super(v);
        CardView background = (CardView) v;
        profile_img = v.findViewById(R.id.list_owner_profile);
        icons[0] = v.findViewById(R.id.list_user_verified);
        icons[1] = v.findViewById(R.id.list_user_locked);
        icons[2] = v.findViewById(R.id.list_member_icon);
        icons[3] = v.findViewById(R.id.list_subscriber_icon);
        icons[4] = v.findViewById(R.id.list_date_icon);
        icons[5] = v.findViewById(R.id.list_private);
        icons[6] = v.findViewById(R.id.list_follow_icon);
        textViews[0] = v.findViewById(R.id.list_title);
        textViews[1] = v.findViewById(R.id.list_description);
        textViews[2] = v.findViewById(R.id.list_ownername);
        textViews[3] = v.findViewById(R.id.list_screenname);
        textViews[4] = v.findViewById(R.id.list_createdat);
        textViews[5] = v.findViewById(R.id.list_member);
        textViews[6] = v.findViewById(R.id.list_subscriber);
        textViews[7] = v.findViewById(R.id.list_action);

        icons[0].setImageResource(R.drawable.verify);
        icons[1].setImageResource(R.drawable.lock);
        icons[2].setImageResource(R.drawable.user);
        icons[3].setImageResource(R.drawable.subscriber);
        icons[4].setImageResource(R.drawable.calendar);
        icons[5].setImageResource(R.drawable.lock);
        icons[6].setImageResource(R.drawable.followback);

        for (TextView tv : textViews) {
            tv.setTextColor(settings.getFontColor());
            tv.setTypeface(settings.getFontFace());
        }
        for (ImageView icon : icons) {
            icon.setColorFilter(settings.getIconColor(), SRC_IN);
        }
        background.setCardBackgroundColor(settings.getCardColor());
    }
}