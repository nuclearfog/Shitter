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

/**
 * View holder class for user item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.UserAdapter
 */
public class UserHolder extends ViewHolder {

	public final TextView username, screenname, followingCount, followerCount;
	public final ImageView profileImg, verifyIcon, lockedIcon;
	public final ImageButton delete;

	/**
	 * @param parent Parent view from adapter
	 */
	public UserHolder(ViewGroup parent, GlobalSettings settings) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_user_container);
		username = itemView.findViewById(R.id.item_user_username);
		screenname = itemView.findViewById(R.id.item_user_screenname);
		followingCount = itemView.findViewById(R.id.item_user_following_count);
		followerCount = itemView.findViewById(R.id.item_user_follower_count);
		profileImg = itemView.findViewById(R.id.item_user_profile);
		verifyIcon = itemView.findViewById(R.id.item_user_verified);
		lockedIcon = itemView.findViewById(R.id.item_user_private);
		delete = itemView.findViewById(R.id.item_user_delete_buton);

		AppStyles.setTheme(container, 0);
		background.setCardBackgroundColor(settings.getCardColor());
	}
}