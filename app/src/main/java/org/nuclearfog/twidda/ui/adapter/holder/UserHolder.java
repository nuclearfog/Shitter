package org.nuclearfog.twidda.ui.adapter.holder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.res.Resources;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.EmojiParam;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.EmojiResult;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.User;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * View holder class for user item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.adapter.UserAdapter
 */
public class UserHolder extends ViewHolder implements OnClickListener, AsyncCallback<EmojiResult> {

	private TextView username, screenname, followingCount, followerCount, label;
	private ImageView profileImg, verifyIcon, lockedIcon;
	private ImageButton delete;
	private View notificationDismiss;

	private GlobalSettings settings;
	private TextEmojiLoader emojiLoader;
	private Picasso picasso;

	private OnHolderClickListener listener;

	private long tagId = 0L;


	public UserHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso, TextEmojiLoader emojiLoader, OnHolderClickListener listener, boolean enableDelete) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
		this.settings = settings;
		this.picasso = picasso;
		this.listener = listener;
		this.emojiLoader = emojiLoader;

		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_user_container);
		label = itemView.findViewById(R.id.item_user_label);
		notificationDismiss = itemView.findViewById(R.id.item_user_notification_dismiss);
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
		if (enableDelete) {
			delete.setVisibility(VISIBLE);
		} else {
			delete.setVisibility(GONE);
		}
		itemView.setOnClickListener(this);
		notificationDismiss.setOnClickListener(this);
		delete.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != NO_POSITION) {
			if (v == itemView) {
				listener.onItemClick(position, OnHolderClickListener.USER_CLICK);
			} else if (v == delete) {
				listener.onItemClick(position, OnHolderClickListener.USER_REMOVE);
			} else if (v == notificationDismiss) {
				listener.onItemClick(position, OnHolderClickListener.NOTIFICATION_DISMISS);
			}
		}
	}


	@Override
	public void onResult(@NonNull EmojiResult result) {
		if (result.id == tagId && result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(username.getContext(), result.spannable, result.images);
			username.setText(spannable);
		}
	}

	/**
	 * set user information
	 *
	 * @param user user information
	 */
	public void setContent(User user) {
		tagId = user.getId();
		screenname.setText(user.getScreenname());
		followingCount.setText(StringUtils.NUMBER_FORMAT.format(user.getFollowing()));
		followerCount.setText(StringUtils.NUMBER_FORMAT.format(user.getFollower()));
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
		if (user.getEmojis().length > 0) {
			Spannable usernameSpan = new SpannableString(user.getUsername());
			username.setText(EmojiUtils.removeTags(usernameSpan));
		} else {
			username.setText(user.getUsername());
		}
		String profileImageUrl = user.getProfileImageThumbnailUrl();
		if (settings.imagesEnabled() && !profileImageUrl.isEmpty()) {
			Transformation roundCorner = new RoundedCornersTransformation(2, 0);
			picasso.load(profileImageUrl).transform(roundCorner).error(R.drawable.no_image).into(profileImg);
		} else {
			profileImg.setImageResource(0);
		}
		if (settings.imagesEnabled() && user.getEmojis().length > 0 && !user.getUsername().isEmpty()) {
			SpannableString userSpan = new SpannableString(user.getUsername());
			EmojiParam param = new EmojiParam(tagId, user.getEmojis(), userSpan, username.getResources().getDimensionPixelSize(R.dimen.item_user_icon_size));
			emojiLoader.execute(param, this);
		}
	}

	/**
	 * set notification label
	 */
	public void setLabel(Notification notification) {
		int iconRes;
		String text, name;
		Resources resources = itemView.getResources();
		if (notification.getUser() != null) {
			name = notification.getUser().getScreenname();
		} else {
			name = "";
		}
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
		if (settings.getLogin().getConfiguration().notificationDismissEnabled()) {
			notificationDismiss.setVisibility(VISIBLE);
		}
		label.setVisibility(VISIBLE);
		label.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
		label.setText(text);
	}
}