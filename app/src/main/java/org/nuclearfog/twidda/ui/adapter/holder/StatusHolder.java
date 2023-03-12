package org.nuclearfog.twidda.ui.adapter.holder;

import static androidx.recyclerview.widget.RecyclerView.HORIZONTAL;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.res.Resources;
import android.graphics.Color;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.adapter.IconAdapter;
import org.nuclearfog.twidda.ui.adapter.StatusAdapter;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Holder class for the status item view
 *
 * @author nuclearfog
 * @see StatusAdapter
 */
public class StatusHolder extends ViewHolder implements OnClickListener {

	private ImageView profile, repostUserIcon, verifiedIcon, lockedIcon, repostIcon, favoriteIcon, replyStatus;
	private TextView username, screenname, text, repost, favorite, reply, reposter, created, replyname, label;
	private View dismissButton;
	private RecyclerView iconList;

	private GlobalSettings settings;
	private Picasso picasso;
	private IconAdapter adapter;
	private OnHolderClickListener listener;


	public StatusHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status, parent, false));
		this.settings = settings;
		this.picasso = picasso;
		this.listener = listener;

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
		text = itemView.findViewById(R.id.item_status_text);
		repost = itemView.findViewById(R.id.item_status_repost_count);
		favorite = itemView.findViewById(R.id.item_status_favorite_count);
		reply = itemView.findViewById(R.id.item_status_reply_count);
		reposter = itemView.findViewById(R.id.item_status_reposter_name);
		created = itemView.findViewById(R.id.item_status_created_at);
		replyname = itemView.findViewById(R.id.item_status_reply_name);

		iconList.setLayoutManager(new LinearLayoutManager(parent.getContext(), HORIZONTAL, false));
		adapter = new IconAdapter(settings, false);
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
		if (position != NO_POSITION) {
			if (v == itemView) {
				listener.onItemClick(position, OnHolderClickListener.STATUS_CLICK);
			} else if (v == label) {
				listener.onItemClick(position, OnHolderClickListener.STATUS_LABEL);
			} else if (v == dismissButton) {
				listener.onItemClick(position, OnHolderClickListener.NOTIFICATION_DISMISS);
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
			repostUserIcon.setVisibility(View.VISIBLE);
			status = status.getEmbeddedStatus();
			user = status.getAuthor();
		} else {
			reposter.setVisibility(View.GONE);
			repostUserIcon.setVisibility(View.GONE);
		}
		username.setText(user.getUsername());
		screenname.setText(user.getScreenname());
		repost.setText(StringTools.NUMBER_FORMAT.format(status.getRepostCount()));
		favorite.setText(StringTools.NUMBER_FORMAT.format(status.getFavoriteCount()));
		reply.setText(StringTools.NUMBER_FORMAT.format(status.getReplyCount()));
		created.setText(StringTools.formatCreationTime(itemView.getResources(), status.getTimestamp()));
		if (!status.getText().trim().isEmpty()) {
			Spanned textSpan = Tagger.makeTextWithLinks(status.getText(), settings.getHighlightColor());
			text.setText(textSpan);
			text.setVisibility(View.VISIBLE);
		} else {
			text.setVisibility(View.GONE);
		}
		if (status.isReposted()) {
			repostIcon.setColorFilter(settings.getRepostIconColor());
		} else {
			repostIcon.setColorFilter(settings.getIconColor());
		}
		if (status.isFavorited()) {
			favoriteIcon.setColorFilter(settings.getFavoriteIconColor());
		} else {
			favoriteIcon.setColorFilter(settings.getIconColor());
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
		String profileImageUrl = user.getProfileImageThumbnailUrl();
		if (settings.imagesEnabled() && !profileImageUrl.isEmpty()) {
			Transformation roundCorner = new RoundedCornersTransformation(2, 0);
			picasso.load(profileImageUrl).transform(roundCorner).error(R.drawable.no_image).into(profile);
		} else {
			profile.setImageResource(0);
		}
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
		if (settings.statusIndicatorsEnabled()) {
			// set visibility first so iconholder can measure the listview height
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
}