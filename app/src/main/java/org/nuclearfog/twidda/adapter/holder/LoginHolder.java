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
 * item holder for a user login item
 *
 * @author nuclearfog
 */
public class LoginHolder extends ViewHolder {

    public final ImageView profile;
    public final TextView username, screenname, date;
    public final ImageButton remove;

    /**
     *
     */
    public LoginHolder(ViewGroup parent, GlobalSettings settings) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_login, parent, false));
        // get views
        CardView background = (CardView) itemView;
        username = itemView.findViewById(R.id.item_login_username);
        screenname = itemView.findViewById(R.id.item_login_screenname);
        date = itemView.findViewById(R.id.item_login_createdAt);
        remove = itemView.findViewById(R.id.item_login_remove);
        profile = itemView.findViewById(R.id.item_login_image);
        // theme views
        screenname.setTextColor(settings.getFontColor());
        screenname.setTypeface(settings.getTypeFace());
        screenname.setTextColor(settings.getFontColor());
        screenname.setTypeface(settings.getTypeFace());
        date.setTextColor(settings.getFontColor());
        date.setTypeface(settings.getTypeFace());
        remove.setImageResource(R.drawable.cross);
        remove.setColorFilter(settings.getIconColor(), SRC_IN);
        background.setCardBackgroundColor(settings.getCardColor());
        AppStyles.setButtonColor(remove, settings.getFontColor());
    }
}