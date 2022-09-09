package org.nuclearfog.twidda.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * view holder class for an user list item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.UserlistAdapter
 */
public class UserlistHolder extends ViewHolder {

	public final ImageView profile, verified, locked, privateList, follow;
	public final TextView title, description, username, screenname, date, member, subscriber, followList;

	/**
	 * @param parent Parent view from adapter
	 */
	public UserlistHolder(ViewGroup parent, GlobalSettings settings) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false));
		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_list_container);
		profile = itemView.findViewById(R.id.item_list_profile);
		verified = itemView.findViewById(R.id.item_list_user_verified);
		locked = itemView.findViewById(R.id.item_list_user_locked);
		privateList = itemView.findViewById(R.id.item_list_private);
		follow = itemView.findViewById(R.id.item_list_follow_icon);
		title = itemView.findViewById(R.id.item_list_title);
		description = itemView.findViewById(R.id.item_list_description);
		username = itemView.findViewById(R.id.item_list_username);
		screenname = itemView.findViewById(R.id.item_list_screenname);
		date = itemView.findViewById(R.id.item_list_created_date);
		member = itemView.findViewById(R.id.item_list_member);
		subscriber = itemView.findViewById(R.id.item_list_subscriber);
		followList = itemView.findViewById(R.id.item_list_following_indicator);

		AppStyles.setTheme(container, 0);
		background.setCardBackgroundColor(settings.getCardColor());
	}
}