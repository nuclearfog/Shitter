package org.nuclearfog.twidda.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.StatusAdapter;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * Holder class for the status item view
 *
 * @author nuclearfog
 * @see StatusAdapter
 */
public class StatusHolder extends ViewHolder {

	public final ImageView profile, rpUser, verifiedIcon, lockedIcon, rtIcon, favIcon, media, location, replyIcon;
	public final TextView username, screenname, text, repost, favorite, reposter, created, replyname;

	/**
	 * @param parent   Parent view from adapter
	 * @param settings app settings to set theme
	 */
	public StatusHolder(ViewGroup parent, GlobalSettings settings) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status, parent, false));
		CardView cardLayout = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_status_container);
		profile = itemView.findViewById(R.id.item_status_profile_image);
		verifiedIcon = itemView.findViewById(R.id.item_status_verified_icon);
		lockedIcon = itemView.findViewById(R.id.item_status_locked_icon);
		rpUser = itemView.findViewById(R.id.item_status_reposter_icon);
		rtIcon = itemView.findViewById(R.id.item_status_repost_icon);
		favIcon = itemView.findViewById(R.id.item_status_favorite_icon);
		media = itemView.findViewById(R.id.item_status_media);
		location = itemView.findViewById(R.id.item_status_location);
		replyIcon = itemView.findViewById(R.id.item_status_reply);
		username = itemView.findViewById(R.id.item_status_author_username);
		screenname = itemView.findViewById(R.id.item_status_author_screenname);
		text = itemView.findViewById(R.id.item_status_text);
		repost = itemView.findViewById(R.id.item_status_repost_count);
		favorite = itemView.findViewById(R.id.item_status_favorite_count);
		reposter = itemView.findViewById(R.id.item_status_reposter_name);
		created = itemView.findViewById(R.id.item_status_created_at);
		replyname = itemView.findViewById(R.id.item_status_reply_name);

		if (settings.likeEnabled()) {
			favIcon.setImageResource(R.drawable.like);
		} else {
			favIcon.setImageResource(R.drawable.favorite);
		}
		AppStyles.setTheme(container, 0);
		cardLayout.setCardBackgroundColor(settings.getCardColor());
	}
}