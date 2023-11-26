package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

import android.content.res.Resources;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.Param;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.Result;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.User;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * View holder class for user item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.adapter.recyclerview.UserAdapter
 */
public class UserHolder extends ViewHolder implements OnClickListener, AsyncCallback<Result> {

	private static final int EMPTY_COLOR = 0x2F000000;

	private static final int IMG_SIZE = 150;

	private TextView username, screenname, followingCount, followerCount, label;
	private ImageView profileImg, verifyIcon, lockedIcon, labelIcon;
	private Drawable placeholder;

	private GlobalSettings settings;
	private TextEmojiLoader emojiLoader;
	private Picasso picasso;

	private OnHolderClickListener listener;

	private long tagId = 0L;

	/**
	 * @param enableRemoveButton true to enable remove button
	 */
	public UserHolder(ViewGroup parent, OnHolderClickListener listener, boolean isNotification, boolean enableRemoveButton) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
		settings = GlobalSettings.get(parent.getContext());
		picasso = PicassoBuilder.get(parent.getContext());
		emojiLoader = new TextEmojiLoader(parent.getContext());
		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_user_container);
		View dismiss = itemView.findViewById(R.id.item_user_notification_dismiss);
		View delete = itemView.findViewById(R.id.item_user_delete_button);
		label = itemView.findViewById(R.id.item_user_label);
		labelIcon = itemView.findViewById(R.id.item_user_label_icon);
		username = itemView.findViewById(R.id.item_user_username);
		screenname = itemView.findViewById(R.id.item_user_screenname);
		followingCount = itemView.findViewById(R.id.item_user_following_count);
		followerCount = itemView.findViewById(R.id.item_user_follower_count);
		profileImg = itemView.findViewById(R.id.item_user_profile);
		verifyIcon = itemView.findViewById(R.id.item_user_verified);
		lockedIcon = itemView.findViewById(R.id.item_user_private);
		placeholder = new ColorDrawable(EMPTY_COLOR);
		this.listener = listener;

		AppStyles.setTheme(container, Color.TRANSPARENT);
		background.setCardBackgroundColor(settings.getCardColor());
		if (enableRemoveButton) {
			delete.setVisibility(View.VISIBLE);
		} else {
			delete.setVisibility(View.GONE);
		}
		if (isNotification) {
			label.setVisibility(View.VISIBLE);
			labelIcon.setVisibility(View.VISIBLE);
			if (settings.getLogin().getConfiguration().notificationDismissEnabled()) {
				dismiss.setVisibility(View.VISIBLE);
			}
		}
		dismiss.setOnClickListener(this);
		delete.setOnClickListener(this);
		label.setOnClickListener(this);
		container.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != RecyclerView.NO_POSITION) {
			if (v.getId() == R.id.item_user_container) {
				listener.onItemClick(position, OnHolderClickListener.USER_CLICK);
			} else if (v.getId() == R.id.item_user_delete_button) {
				listener.onItemClick(position, OnHolderClickListener.USER_REMOVE);
			} else if (v.getId() == R.id.item_user_notification_dismiss) {
				listener.onItemClick(position, OnHolderClickListener.NOTIFICATION_DISMISS);
			} else if (v.getId() == R.id.item_user_label) {
				listener.onItemClick(position, OnHolderClickListener.NOTIFICATION_USER_CLICK);
			}
		}
	}


	@Override
	public void onResult(@NonNull Result result) {
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
			verifyIcon.setVisibility(View.VISIBLE);
		} else {
			verifyIcon.setVisibility(View.GONE);
		}
		if (user.isProtected()) {
			lockedIcon.setVisibility(View.VISIBLE);
		} else {
			lockedIcon.setVisibility(View.GONE);
		}
		if (user.getEmojis().length > 0 && !user.getUsername().trim().isEmpty() && settings.imagesEnabled()) {
			Spannable usernameSpan = new SpannableString(user.getUsername());
			Param param = new Param(tagId, user.getEmojis(), usernameSpan, username.getResources().getDimensionPixelSize(R.dimen.item_user_icon_size));
			emojiLoader.execute(param, this);
			username.setText(EmojiUtils.removeTags(usernameSpan));
		} else {
			username.setText(user.getUsername());
		}
		String profileImageUrl = user.getProfileImageThumbnailUrl();
		if (settings.imagesEnabled() && !profileImageUrl.isEmpty()) {
			Transformation roundCorner = new RoundedCornersTransformation(2, 0);
			picasso.load(profileImageUrl).resize(IMG_SIZE, IMG_SIZE).centerCrop().transform(roundCorner).placeholder(placeholder).error(R.drawable.no_image).into(profileImg);
		} else {
			profileImg.setImageDrawable(placeholder);
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
		labelIcon.setImageResource(iconRes);
		label.setText(text);
	}
}