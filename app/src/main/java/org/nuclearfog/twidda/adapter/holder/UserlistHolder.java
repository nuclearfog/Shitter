package org.nuclearfog.twidda.adapter.holder;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * view holder class for an user list item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.UserlistAdapter
 */
public class UserlistHolder extends ViewHolder implements OnClickListener {

	private ImageView profileImage, userVerified, userLocked, privateIcon, followIcon;
	private TextView title, description, username, screenname, date, member, subscriber, followList;

	private Picasso picasso;
	private OnHolderClickListener listener;

	private boolean enableExtras, enableImages;

	/**
	 * @param parent Parent view from adapter
	 */
	public UserlistHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false));
		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_list_container);
		View dateIcon = itemView.findViewById(R.id.item_list_date_icon);
		View memberIcon = itemView.findViewById(R.id.item_list_member_icon);
		View subscriberIcon = itemView.findViewById(R.id.item_list_subscriber_icon);
		profileImage = itemView.findViewById(R.id.item_list_profile);
		userVerified = itemView.findViewById(R.id.item_list_user_verified);
		userLocked = itemView.findViewById(R.id.item_list_user_locked);
		privateIcon = itemView.findViewById(R.id.item_list_private);
		followIcon = itemView.findViewById(R.id.item_list_follow_icon);
		title = itemView.findViewById(R.id.item_list_title);
		description = itemView.findViewById(R.id.item_list_description);
		username = itemView.findViewById(R.id.item_list_username);
		screenname = itemView.findViewById(R.id.item_list_screenname);
		date = itemView.findViewById(R.id.item_list_created_date);
		member = itemView.findViewById(R.id.item_list_member);
		subscriber = itemView.findViewById(R.id.item_list_subscriber);
		followList = itemView.findViewById(R.id.item_list_following_indicator);

		int apiType = settings.getLogin().getApiType();
		enableExtras = apiType == Account.API_TWITTER_1 || apiType == Account.API_TWITTER_2;
		enableImages = settings.imagesEnabled();

		if (!enableExtras) {
			// disable extra views
			date.setVisibility(View.GONE);
			dateIcon.setVisibility(View.GONE);
			member.setVisibility(View.GONE);
			subscriber.setVisibility(View.GONE);
			description.setVisibility(View.GONE);
			memberIcon.setVisibility(View.GONE);
			subscriberIcon.setVisibility(View.GONE);
			// add title icon
			privateIcon.setImageResource(R.drawable.list);
		}
		AppStyles.setTheme(container, Color.TRANSPARENT);
		background.setCardBackgroundColor(settings.getCardColor());
		itemView.setOnClickListener(this);
		profileImage.setOnClickListener(this);

		this.picasso = picasso;
		this.listener = listener;
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != NO_POSITION) {
			if (v == itemView) {
				listener.onItemClick(position, OnHolderClickListener.LIST_CLICK);
			} else if (v == profileImage) {
				listener.onItemClick(position, OnHolderClickListener.LIST_PROFILE);
			}
		}
	}

	/**
	 * set view content
	 */
	public void setContent(UserList userlist) {
		User owner = userlist.getListOwner();
		title.setText(userlist.getTitle());
		if (enableExtras) {
			description.setText(userlist.getDescription());
			date.setText(StringTools.formatCreationTime(itemView.getResources(), userlist.getTimestamp()));
			member.setText(StringTools.NUMBER_FORMAT.format(userlist.getMemberCount()));
			subscriber.setText(StringTools.NUMBER_FORMAT.format(userlist.getSubscriberCount()));
		}
		if (owner != null) {
			username.setText(owner.getUsername());
			screenname.setText(owner.getScreenname());
			String profileImageUrl = owner.getProfileImageThumbnailUrl();
			if (enableImages && !profileImageUrl.isEmpty()) {
				Transformation roundCorner = new RoundedCornersTransformation(3, 0);
				picasso.load(profileImageUrl).transform(roundCorner).error(R.drawable.no_image).into(profileImage);
			} else {
				profileImage.setImageResource(0);
			}
			if (!owner.isCurrentUser() && userlist.isFollowing()) {
				followIcon.setVisibility(View.VISIBLE);
				followList.setVisibility(View.VISIBLE);
			} else {
				followIcon.setVisibility(View.GONE);
				followList.setVisibility(View.GONE);
			}
			if (owner.isVerified()) {
				userVerified.setVisibility(View.VISIBLE);
			} else {
				userVerified.setVisibility(View.GONE);
			}
			if (owner.isProtected()) {
				userLocked.setVisibility(View.VISIBLE);
			} else {
				userLocked.setVisibility(View.GONE);
			}
		} else {
			userLocked.setVisibility(View.GONE);
			userVerified.setVisibility(View.GONE);
			followIcon.setVisibility(View.GONE);
			followList.setVisibility(View.GONE);
			profileImage.setVisibility(View.GONE);
			username.setVisibility(View.GONE);
			screenname.setVisibility(View.GONE);
		}
		if (userlist.isPrivate() || !enableExtras) {
			privateIcon.setVisibility(View.VISIBLE);
		} else {
			privateIcon.setVisibility(View.GONE);
		}
	}
}