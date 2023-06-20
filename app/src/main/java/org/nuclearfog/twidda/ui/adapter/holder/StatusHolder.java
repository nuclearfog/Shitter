package org.nuclearfog.twidda.ui.adapter.holder;

import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.EmojiParam;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.EmojiResult;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.adapter.IconAdapter;
import org.nuclearfog.twidda.ui.adapter.IconAdapter.OnMediaClickListener;
import org.nuclearfog.twidda.ui.adapter.StatusAdapter;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Holder class for the status item view
 *
 * @author nuclearfog
 * @see StatusAdapter
 */
public class StatusHolder extends ViewHolder implements OnClickListener, OnMediaClickListener {

	private static final int EMPTY_COLOR = 0x2F000000;

	private static final int IMG_SIZE = 150;

	private ImageView profile, repostUserIcon, verifiedIcon, lockedIcon, repostIcon, favoriteIcon, replyStatus;
	private TextView username, screenname, statusText, repost, favorite, reply, reposter, created, replyname, label;
	private View dismissButton;
	private RecyclerView iconList;
	private Drawable placeholder;

	private GlobalSettings settings;
	private Picasso picasso;
	private TextEmojiLoader emojiLoader;
	private IconAdapter adapter;
	private OnHolderClickListener listener;

	private AsyncCallback<EmojiResult> textResult = this::setTextEmojis;
	private AsyncCallback<EmojiResult> usernameResult = this::setUsernameEmojis;

	private long tagId = 0L;


	public StatusHolder(ViewGroup parent, TextEmojiLoader emojiLoader, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status, parent, false));
		settings = GlobalSettings.get(parent.getContext());
		picasso = PicassoBuilder.get(parent.getContext());
		this.listener = listener;
		this.emojiLoader = emojiLoader;

		CardView cardLayout = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_status_container);
		label = itemView.findViewById(R.id.item_status_label);
		dismissButton = itemView.findViewById(R.id.item_status_notification_dismiss);
		profile = itemView.findViewById(R.id.item_status_profile_image);
		verifiedIcon = itemView.findViewById(R.id.item_status_verified_icon);
		lockedIcon = itemView.findViewById(R.id.item_status_locked_icon);
		repostUserIcon = itemView.findViewById(R.id.item_status_reposter_icon);
		repostIcon = itemView.findViewById(R.id.item_status_repost_icon);
		favoriteIcon = itemView.findViewById(R.id.item_status_favorite_icon);
		iconList = itemView.findViewById(R.id.item_status_attachment_list);
		replyStatus = itemView.findViewById(R.id.item_status_reply);
		username = itemView.findViewById(R.id.item_status_author_username);
		screenname = itemView.findViewById(R.id.item_status_author_screenname);
		statusText = itemView.findViewById(R.id.item_status_text);
		repost = itemView.findViewById(R.id.item_status_repost_count);
		favorite = itemView.findViewById(R.id.item_status_favorite_count);
		reply = itemView.findViewById(R.id.item_status_reply_count);
		reposter = itemView.findViewById(R.id.item_status_reposter_name);
		created = itemView.findViewById(R.id.item_status_created_at);
		replyname = itemView.findViewById(R.id.item_status_reply_name);

		placeholder = new ColorDrawable(EMPTY_COLOR);
		iconList.setLayoutManager(new LinearLayoutManager(parent.getContext(), RecyclerView.HORIZONTAL, false));
		adapter = new IconAdapter(this, false);
		iconList.setAdapter(adapter);

		if (settings.likeEnabled()) {
			favoriteIcon.setImageResource(R.drawable.like);
		} else {
			favoriteIcon.setImageResource(R.drawable.favorite);
		}
		AppStyles.setTheme(container, Color.TRANSPARENT);
		cardLayout.setCardBackgroundColor(settings.getCardColor());

		label.setOnClickListener(this);
		itemView.setOnClickListener(this);
		dismissButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != RecyclerView.NO_POSITION) {
			if (v == itemView) {
				listener.onItemClick(position, OnHolderClickListener.STATUS_CLICK);
			} else if (v == label) {
				listener.onItemClick(position, OnHolderClickListener.STATUS_LABEL);
			} else if (v == dismissButton) {
				listener.onItemClick(position, OnHolderClickListener.NOTIFICATION_DISMISS);
			}
		}
	}


	@Override
	public void onMediaClick(int index) {
	}


	/**
	 * set view content
	 *
	 * @param status content to show
	 */
	public void setContent(Status status) {
		tagId = status.getId();
		if (status.getEmbeddedStatus() != null) {
			reposter.setText(status.getAuthor().getScreenname());
			reposter.setVisibility(View.VISIBLE);
			repostUserIcon.setVisibility(View.VISIBLE);
			status = status.getEmbeddedStatus();
		} else {
			reposter.setVisibility(View.GONE);
			repostUserIcon.setVisibility(View.GONE);
		}
		User author = status.getAuthor();
		String profileImageUrl = author.getProfileImageThumbnailUrl();

		screenname.setText(author.getScreenname());
		repost.setText(StringUtils.NUMBER_FORMAT.format(status.getRepostCount()));
		favorite.setText(StringUtils.NUMBER_FORMAT.format(status.getFavoriteCount()));
		reply.setText(StringUtils.NUMBER_FORMAT.format(status.getReplyCount()));
		created.setText(StringUtils.formatCreationTime(itemView.getResources(), status.getTimestamp()));
		// set username and emojis
		if (author.getEmojis().length > 0 && !author.getUsername().trim().isEmpty() && settings.imagesEnabled()) {
			SpannableString usernameSpan = new SpannableString(author.getUsername());
			EmojiParam param = new EmojiParam(tagId, author.getEmojis(), usernameSpan, statusText.getResources().getDimensionPixelSize(R.dimen.item_status_icon_size));
			emojiLoader.execute(param, usernameResult);
			username.setText(EmojiUtils.removeTags(usernameSpan));
		} else {
			username.setText(author.getUsername());
		}
		// set status text and emojis
		if (!status.getText().trim().isEmpty()) {
			Spannable textSpan = Tagger.makeTextWithLinks(status.getText(), settings.getHighlightColor());
			if (status.getEmojis().length > 0 && settings.imagesEnabled()) {
				EmojiParam param = new EmojiParam(tagId, status.getEmojis(), textSpan, statusText.getResources().getDimensionPixelSize(R.dimen.item_status_icon_size));
				emojiLoader.execute(param, textResult);
				textSpan = EmojiUtils.removeTags(textSpan);
			}
			statusText.setText(textSpan);
			statusText.setVisibility(View.VISIBLE);
		} else {
			statusText.setVisibility(View.GONE);
		}
		// set status blur if spoiler content
		if (settings.hideSensitiveEnabled() && status.isSpoiler()) {
			statusText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			float radius = statusText.getTextSize() / 3;
			BlurMaskFilter filter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
			statusText.getPaint().setMaskFilter(filter);
		} else {
			statusText.getPaint().setMaskFilter(null);
		}
		// set repost icon
		if (status.isReposted()) {
			repostIcon.setColorFilter(settings.getRepostIconColor());
		} else {
			repostIcon.setColorFilter(settings.getIconColor());
		}
		// set favorite/like icon
		if (status.isFavorited()) {
			favoriteIcon.setColorFilter(settings.getFavoriteIconColor());
		} else {
			favoriteIcon.setColorFilter(settings.getIconColor());
		}
		// set user verified icon
		if (author.isVerified()) {
			verifiedIcon.setVisibility(View.VISIBLE);
		} else {
			verifiedIcon.setVisibility(View.GONE);
		}
		// set user protected icon
		if (author.isProtected()) {
			lockedIcon.setVisibility(View.VISIBLE);
		} else {
			lockedIcon.setVisibility(View.GONE);
		}
		// set profile image
		if (settings.imagesEnabled() && !profileImageUrl.isEmpty()) {
			Transformation roundCorner = new RoundedCornersTransformation(2, 0);
			picasso.load(profileImageUrl).transform(roundCorner).resize(IMG_SIZE, IMG_SIZE).placeholder(placeholder).centerCrop().error(R.drawable.no_image).into(profile);
		} else {
			profile.setImageDrawable(placeholder);
		}
		// set 'replied' text and icon
		if (status.getRepliedStatusId() > 0) {
			replyStatus.setVisibility(View.VISIBLE);
			replyname.setVisibility(View.VISIBLE);
			if (!status.getReplyName().isEmpty())
				replyname.setText(status.getReplyName());
			else
				replyname.setText(R.string.status_replyname_empty);
		} else {
			replyStatus.setVisibility(View.GONE);
			replyname.setVisibility(View.GONE);
		}
		// setup attachment indicators
		if (settings.statusIndicatorsEnabled()) {
			iconList.setVisibility(View.VISIBLE);
			adapter.addItems(status);
			if (adapter.isEmpty()) {
				iconList.setVisibility(View.GONE);
			} else {
				iconList.setVisibility(View.VISIBLE);
			}
		} else {
			iconList.setVisibility(View.GONE);
		}
	}

	/**
	 * set notification label
	 */
	public void setLabel(Notification notification) {
		int iconRes;
		String text, name;
		if (notification.getUser() != null)
			name = notification.getUser().getScreenname();
		else
			name = "";
		Resources resources = itemView.getResources();

		switch (notification.getType()) {
			default:
				text = "";
				iconRes = 0;
				break;

			case Notification.TYPE_MENTION:
				if (name.startsWith("@"))
					text = resources.getString(R.string.info_user_mention, name.substring(1));
				else
					text = resources.getString(R.string.info_user_mention, name);
				iconRes = R.drawable.mention;
				break;

			case Notification.TYPE_REPOST:
				text = resources.getString(R.string.info_user_repost, name);
				iconRes = R.drawable.repost;
				break;

			case Notification.TYPE_FAVORITE:
				text = resources.getString(R.string.info_user_favorited, name);
				iconRes = R.drawable.favorite;
				break;

			case Notification.TYPE_POLL:
				text = resources.getString(R.string.notification_status_poll);
				iconRes = R.drawable.poll;
				break;
		}
		label.setVisibility(View.VISIBLE);
		label.setText(text);
		label.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
		if (settings.getLogin().getConfiguration().notificationDismissEnabled()) {
			dismissButton.setVisibility(View.VISIBLE);
		}
		AppStyles.setDrawableColor(label, settings.getIconColor());
	}

	/**
	 * update username
	 *
	 * @param result username with emojis
	 */
	private void setUsernameEmojis(@NonNull EmojiResult result) {
		if (result.id == tagId && result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(username.getContext(), result.spannable, result.images);
			username.setText(spannable);
		}
	}

	/**
	 * update status text
	 *
	 * @param result status text with emojis
	 */
	private void setTextEmojis(@NonNull EmojiResult result) {
		if (result.id == tagId && result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(statusText.getContext(), result.spannable, result.images);
			statusText.setText(spannable);
		}
	}
}