package org.nuclearfog.twidda.adapter.holder;


import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.res.Resources;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.StatusAdapter;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;

import java.text.NumberFormat;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Holder class for the status item view
 *
 * @author nuclearfog
 * @see StatusAdapter
 */
public class StatusHolder extends ViewHolder implements OnClickListener {

	private static final NumberFormat NUM_FORMAT = NumberFormat.getIntegerInstance();

	private ImageView profile, rpUser, verifiedIcon, lockedIcon, rtIcon, favIcon, media, location, replyIcon;
	private TextView username, screenname, text, repost, favorite, reposter, created, replyname, label;

	private GlobalSettings settings;
	private Picasso picasso;

	private OnHolderClickListener listener;

	/**
	 * @param parent   Parent view from adapter
	 * @param settings app settings to set theme
	 */
	public StatusHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status, parent, false));
		CardView cardLayout = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_status_container);
		label = itemView.findViewById(R.id.item_status_label);
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
		this.settings = settings;
		this.picasso = picasso;

		if (settings.likeEnabled()) {
			favIcon.setImageResource(R.drawable.like);
		} else {
			favIcon.setImageResource(R.drawable.favorite);
		}
		AppStyles.setTheme(container, 0);
		cardLayout.setCardBackgroundColor(settings.getCardColor());

		label.setOnClickListener(this);
		itemView.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != NO_POSITION && listener != null) {
			if (v == itemView) {
				listener.onItemClick(position, OnHolderClickListener.STATUS_CLICK);
			} else if (v == label) {
				listener.onItemClick(position, OnHolderClickListener.STATUS_LABEL);
			}
		}
	}

	/**
	 * set view content
	 *
	 * @param status content to show
	 */
	public void setContent(Status status) {
		User user = status.getAuthor();
		if (status.getEmbeddedStatus() != null) {
			reposter.setText(user.getScreenname());
			reposter.setVisibility(View.VISIBLE);
			rpUser.setVisibility(View.VISIBLE);
			status = status.getEmbeddedStatus();
			user = status.getAuthor();
		} else {
			reposter.setVisibility(View.GONE);
			rpUser.setVisibility(View.GONE);
		}
		username.setText(user.getUsername());
		screenname.setText(user.getScreenname());
		repost.setText(NUM_FORMAT.format(status.getRepostCount()));
		favorite.setText(NUM_FORMAT.format(status.getFavoriteCount()));
		created.setText(StringTools.formatCreationTime(itemView.getResources(), status.getTimestamp()));
		if (!status.getText().isEmpty()) {
			Spanned textSpan = Tagger.makeTextWithLinks(status.getText(), settings.getHighlightColor());
			text.setText(textSpan);
			text.setVisibility(View.VISIBLE);
		} else {
			text.setVisibility(View.GONE);
		}
		if (status.isReposted()) {
			rtIcon.setColorFilter(settings.getRepostIconColor());
		} else {
			rtIcon.setColorFilter(settings.getIconColor());
		}
		if (status.isFavorited()) {
			favIcon.setColorFilter(settings.getFavoriteIconColor());
		} else {
			favIcon.setColorFilter(settings.getIconColor());
		}
		if (user.isVerified()) {
			verifiedIcon.setVisibility(View.VISIBLE);
		} else {
			verifiedIcon.setVisibility(View.GONE);
		}
		if (user.isProtected()) {
			lockedIcon.setVisibility(View.VISIBLE);
		} else {
			lockedIcon.setVisibility(View.GONE);
		}
		if (settings.imagesEnabled() && !user.getImageUrl().isEmpty()) {
			String profileImageUrl;
			if (!user.hasDefaultProfileImage()) {
				profileImageUrl = StringTools.buildImageLink(user.getImageUrl(), settings.getImageSuffix());
			} else {
				profileImageUrl = user.getImageUrl();
			}
			picasso.load(profileImageUrl).transform(new RoundedCornersTransformation(2, 0)).error(R.drawable.no_image).into(profile);
		} else {
			profile.setImageResource(0);
		}
		if (status.getRepliedStatusId() > 0) {
			replyIcon.setVisibility(View.VISIBLE);
			replyname.setVisibility(View.VISIBLE);
			if (!status.getReplyName().isEmpty())
				replyname.setText(status.getReplyName());
			else
				replyname.setText(R.string.status_replyname_empty);
		} else {
			replyIcon.setVisibility(View.GONE);
			replyname.setVisibility(View.GONE);
		}
		if (settings.statusIndicatorsEnabled()) {
			if (status.getLocationName() != null && !status.getLocationName().isEmpty()) {
				location.setVisibility(View.VISIBLE);
			} else {
				location.setVisibility(View.GONE);
			}
			if (status.getMediaType() != Status.MEDIA_NONE) {
				if (status.getMediaType() == Status.MEDIA_PHOTO) {
					media.setImageResource(R.drawable.image);
				} else if (status.getMediaType() == Status.MEDIA_VIDEO) {
					media.setImageResource(R.drawable.video);
				} else if (status.getMediaType() == Status.MEDIA_GIF) {
					media.setImageResource(R.drawable.gif);
				}
				media.setColorFilter(settings.getIconColor());
				media.setVisibility(View.VISIBLE);
			} else {
				media.setVisibility(View.GONE);
			}
		} else {
			location.setVisibility(View.GONE);
			media.setVisibility(View.GONE);
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
		}
		label.setVisibility(View.VISIBLE);
		label.setText(text);
		label.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
		AppStyles.setDrawableColor(label, settings.getIconColor());
	}

	/**
	 * set item click listener
	 */
	public void setOnStatusClickListener(OnHolderClickListener listener) {
		this.listener = listener;
	}
}