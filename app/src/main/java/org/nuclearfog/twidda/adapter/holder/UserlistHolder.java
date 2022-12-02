package org.nuclearfog.twidda.adapter.holder;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

import java.text.NumberFormat;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * view holder class for an user list item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.UserlistAdapter
 */
public class UserlistHolder extends ViewHolder implements OnClickListener {

	private static final NumberFormat NUM_FORMAT = NumberFormat.getIntegerInstance();

	private ImageView profile, verified, locked, privateList, follow;
	private TextView title, description, username, screenname, date, member, subscriber, followList;

	private GlobalSettings settings;
	private Picasso picasso;

	private OnListClickListener listener;

	/**
	 * @param parent Parent view from adapter
	 */
	public UserlistHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso, OnListClickListener listener) {
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
		this.settings = settings;
		this.picasso = picasso;
		this.listener = listener;

		AppStyles.setTheme(container, 0);
		background.setCardBackgroundColor(settings.getCardColor());

		itemView.setOnClickListener(this);
		profile.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != NO_POSITION) {
			if (v == itemView) {
				listener.onUserlistClick(position, OnListClickListener.LIST_CLICK);
			} else if (v == profile) {
				listener.onUserlistClick(position, OnListClickListener.PROFILE_CLICK);
			}
		}
	}

	/**
	 * set view content
	 */
	public void setContent(UserList userlist) {
		User owner = userlist.getListOwner();
		title.setText(userlist.getTitle());
		description.setText(userlist.getDescription());
		username.setText(owner.getUsername());
		screenname.setText(owner.getScreenname());
		date.setText(StringTools.formatCreationTime(itemView.getResources(), userlist.getTimestamp()));
		member.setText(NUM_FORMAT.format(userlist.getMemberCount()));
		subscriber.setText(NUM_FORMAT.format(userlist.getSubscriberCount()));
		if (settings.imagesEnabled() && !owner.getImageUrl().isEmpty()) {
			String profileImageUrl;
			if (!owner.hasDefaultProfileImage()) {
				profileImageUrl = StringTools.buildImageLink(owner.getImageUrl(), settings.getImageSuffix());
			} else {
				profileImageUrl = owner.getImageUrl();
			}
			picasso.load(profileImageUrl).transform(new RoundedCornersTransformation(3, 0)).error(R.drawable.no_image).into(profile);
		} else {
			profile.setImageResource(0);
		}
		if (!userlist.getListOwner().isCurrentUser() && userlist.isFollowing()) {
			follow.setVisibility(View.VISIBLE);
			followList.setVisibility(View.VISIBLE);
		} else {
			follow.setVisibility(View.GONE);
			followList.setVisibility(View.GONE);
		}
		if (owner.isVerified()) {
			verified.setVisibility(View.VISIBLE);
		} else {
			verified.setVisibility(View.GONE);
		}
		if (owner.isProtected()) {
			locked.setVisibility(View.VISIBLE);
		} else {
			locked.setVisibility(View.GONE);
		}
		if (userlist.isPrivate()) {
			privateList.setVisibility(View.VISIBLE);
		} else {
			privateList.setVisibility(View.GONE);
		}
	}

	/**
	 * item click listener
	 */
	public interface OnListClickListener {

		int LIST_CLICK = 1;

		int PROFILE_CLICK = 2;

		/**
		 * @param position position of the item
		 * @param type     type of click
		 */
		void onUserlistClick(int position, int type);
	}
}