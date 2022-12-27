package org.nuclearfog.twidda.adapter.holder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.User;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * View holder class for user item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.UserAdapter
 */
public class UserHolder extends ViewHolder implements OnClickListener {

	private TextView username, screenname, followingCount, followerCount, label;
	private ImageView profileImg, verifyIcon, lockedIcon;
	private ImageButton delete;

	private GlobalSettings settings;
	private Picasso picasso;

	private OnHolderClickListener listener;

	/**
	 * @param parent Parent view from adapter
	 */
	public UserHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_user_container);
		label = itemView.findViewById(R.id.item_user_label);
		username = itemView.findViewById(R.id.item_user_username);
		screenname = itemView.findViewById(R.id.item_user_screenname);
		followingCount = itemView.findViewById(R.id.item_user_following_count);
		followerCount = itemView.findViewById(R.id.item_user_follower_count);
		profileImg = itemView.findViewById(R.id.item_user_profile);
		verifyIcon = itemView.findViewById(R.id.item_user_verified);
		lockedIcon = itemView.findViewById(R.id.item_user_private);
		delete = itemView.findViewById(R.id.item_user_delete_buton);

		AppStyles.setTheme(container, Color.TRANSPARENT);
		background.setCardBackgroundColor(settings.getCardColor());
		this.settings = settings;
		this.picasso = picasso;

		itemView.setOnClickListener(this);
		delete.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (listener != null && position != NO_POSITION) {
			if (v == itemView) {
				listener.onItemClick(position, OnHolderClickListener.USER_CLICK);
			} else if (v == delete) {
				listener.onItemClick(position, OnHolderClickListener.USER_REMOVE);
			}
		}
	}

	/**
	 * set item click listener
	 */
	public void setOnUserClickListener(OnHolderClickListener listener) {
		this.listener = listener;
	}

	/**
	 * set user information
	 *
	 * @param user user information
	 */
	public void setContent(User user) {
		username.setText(user.getUsername());
		screenname.setText(user.getScreenname());
		followingCount.setText(StringTools.NUMBER_FORMAT.format(user.getFollowing()));
		followerCount.setText(StringTools.NUMBER_FORMAT.format(user.getFollower()));
		if (user.isVerified()) {
			verifyIcon.setVisibility(VISIBLE);
		} else {
			verifyIcon.setVisibility(GONE);
		}
		if (user.isProtected()) {
			lockedIcon.setVisibility(VISIBLE);
		} else {
			lockedIcon.setVisibility(GONE);
		}
		String profileImageUrl = user.getProfileImageThumbnailUrl();
		if (settings.imagesEnabled() && !profileImageUrl.isEmpty()) {
			Transformation roundCorner = new RoundedCornersTransformation(2, 0);
			picasso.load(profileImageUrl).transform(roundCorner).error(R.drawable.no_image).into(profileImg);
		} else {
			profileImg.setImageResource(0);
		}
	}

	/**
	 * set notification label
	 */
	public void setLabel(Notification notification) {
		int iconRes;
		String text, name;
		Resources resources = itemView.getResources();
		if (notification.getUser() != null)
			name = notification.getUser().getScreenname();
		else
			name = "";

		switch (notification.getType()) {
			default:
				text = "";
				iconRes = 0;
				break;

			case Notification.TYPE_FOLLOW:
				text = resources.getString(R.string.info_user_follow, name);
				iconRes = R.drawable.follower;
				break;

			case Notification.TYPE_REQUEST:
				text = resources.getString(R.string.info_user_follow_request, name);
				iconRes = R.drawable.follower_request;
				break;
		}
		label.setVisibility(VISIBLE);
		label.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
		label.setText(text);
	}

	/**
	 * enable/disable delete button
	 *
	 * @param enable true to enable delete button
	 */
	public void setDeleteButton(boolean enable) {
		if (enable) {
			delete.setVisibility(VISIBLE);
		} else {
			delete.setVisibility(GONE);
		}
	}
}