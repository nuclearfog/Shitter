package org.nuclearfog.twidda.ui.adapter.holder;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.EmojiParam;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.EmojiResult;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * view holder class for an user list item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.adapter.UserlistAdapter
 */
public class UserlistHolder extends ViewHolder implements OnClickListener {

	private static final int IMG_SIZE = 150;

	private static final int EMPTY_COLOR = 0x2F000000;

	private AsyncExecutor.AsyncCallback<EmojiResult> usernameResult = this::setUsernameEmojis;

	private ImageView profileImage, userVerified, userLocked, privateIcon, followIcon;
	private TextView title, description, username, screenname, date, member, subscriber, followList;
	private Drawable placeholder;

	private Picasso picasso;
	private GlobalSettings settings;
	private OnHolderClickListener listener;
	private TextEmojiLoader emojiLoader;

	private boolean enableExtras, enableImages;
	private long tagId;

	/**
	 * @param parent Parent view from adapter
	 */
	public UserlistHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso, TextEmojiLoader emojiLoader, OnHolderClickListener listener) {
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
		placeholder = new ColorDrawable(EMPTY_COLOR);

		enableExtras = settings.getLogin().getConfiguration().showListExtras();
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
		this.emojiLoader = emojiLoader;
		this.settings = settings;
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
		tagId = userlist.getId();
		User owner = userlist.getListOwner();
		title.setText(userlist.getTitle());
		if (enableExtras) {
			description.setText(userlist.getDescription());
			date.setText(StringUtils.formatCreationTime(itemView.getResources(), userlist.getTimestamp()));
			member.setText(StringUtils.NUMBER_FORMAT.format(userlist.getMemberCount()));
			subscriber.setText(StringUtils.NUMBER_FORMAT.format(userlist.getSubscriberCount()));
		}
		if (owner != null) {
			screenname.setText(owner.getScreenname());
			String profileImageUrl = owner.getProfileImageThumbnailUrl();
			if (owner.getEmojis().length > 0 && !owner.getUsername().trim().isEmpty() && settings.imagesEnabled()) {
				SpannableString usernameSpan = new SpannableString(owner.getUsername());
				EmojiParam param = new EmojiParam(tagId, owner.getEmojis(), usernameSpan, username.getResources().getDimensionPixelSize(R.dimen.item_user_icon_size));
				emojiLoader.execute(param, usernameResult);
				username.setText(EmojiUtils.removeTags(usernameSpan));
			} else {
				username.setText(owner.getUsername());
			}
			if (enableImages && !profileImageUrl.isEmpty()) {
				Transformation roundCorner = new RoundedCornersTransformation(3, 0);
				picasso.load(profileImageUrl).transform(roundCorner).resize(IMG_SIZE, IMG_SIZE).centerCrop().placeholder(placeholder).error(R.drawable.no_image).into(profileImage);
			} else {
				profileImage.setImageDrawable(placeholder);
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

	/**
	 * update username
	 *
	 * @param result username text with emojis
	 */
	private void setUsernameEmojis(@NonNull EmojiResult result) {
		if (result.id == tagId && result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(username.getContext(), result.spannable, result.images);
			username.setText(spannable);
		}
	}
}