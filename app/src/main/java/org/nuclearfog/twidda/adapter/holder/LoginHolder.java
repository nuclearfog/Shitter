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

    public static final int IDX_USERNAME = 0;
    public static final int IDX_SCR_NAME = 1;
    public static final int IDX_CREATED = 2;

    public final ImageView profile;
    public final ImageButton remove;
    public final TextView[] text = new TextView[3];

    /**
     *
     */
    public LoginHolder(ViewGroup parent, GlobalSettings settings) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false));
        // get views
        CardView background = (CardView) itemView;
        text[IDX_USERNAME] = itemView.findViewById(R.id.item_login_username);
        text[IDX_SCR_NAME] = itemView.findViewById(R.id.item_login_screenname);
        text[IDX_CREATED] = itemView.findViewById(R.id.item_login_createdAt);
        remove = itemView.findViewById(R.id.item_login_remove);
        profile = itemView.findViewById(R.id.item_login_image);
        // theme views
        for (TextView tv : text) {
            tv.setTextColor(settings.getFontColor());
            tv.setTypeface(settings.getTypeFace());
        }
        remove.setImageResource(R.drawable.cross);
        remove.setColorFilter(settings.getIconColor(), SRC_IN);
        background.setCardBackgroundColor(settings.getCardColor());
        AppStyles.setButtonColor(remove, settings.getFontColor());
    }
}