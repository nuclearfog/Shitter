package org.nuclearfog.twidda.adapter.holder;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.graphics.Color;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Message;
import org.nuclearfog.twidda.model.User;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Holder class for a message view
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.MessageAdapter
 */
public class MessageHolder extends ViewHolder implements OnClickListener, OnTagClickListener {

	private TextView username, screenname, time, text;
	private ImageView profile, verifiedIcon, lockedIcon;
	private ImageButton mediaButton;
	private Button answer, delete;

	private GlobalSettings settings;
	private Picasso picasso;

	private OnItemClickListener listener;

	/**
	 * @param parent Parent view from adapter
	 */
	public MessageHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_message_container);
		profile = itemView.findViewById(R.id.item_message_profile);
		verifiedIcon = itemView.findViewById(R.id.item_message_verified);
		lockedIcon = itemView.findViewById(R.id.item_message_private);
		mediaButton = itemView.findViewById(R.id.item_message_media);
		username = itemView.findViewById(R.id.item_message_username);
		screenname = itemView.findViewById(R.id.item_message_screenname);
		time = itemView.findViewById(R.id.item_message_time);
		text = itemView.findViewById(R.id.item_message_text);
		answer = itemView.findViewById(R.id.item_message_answer);
		delete = itemView.findViewById(R.id.item_message_delete);

		AppStyles.setTheme(container, Color.TRANSPARENT);
		background.setCardBackgroundColor(settings.getCardColor());
		text.setMovementMethod(LinkMovementMethod.getInstance());

		this.settings = settings;
		this.picasso = picasso;

		itemView.setOnClickListener(this);
		profile.setOnClickListener(this);
		answer.setOnClickListener(this);
		delete.setOnClickListener(this);
		mediaButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != NO_POSITION && listener != null) {
			if (v == itemView) {
				listener.onItemClick(position, OnItemClickListener.MESSAGE_VIEW);
			} else if (v == answer) {
				listener.onItemClick(position, OnItemClickListener.MESSAGE_ANSWER);
			} else if (v == delete) {
				listener.onItemClick(position, OnItemClickListener.MESSAGE_DELETE);
			} else if (v == profile) {
				listener.onItemClick(position, OnItemClickListener.MESSAGE_PROFILE);
			} else if (v == mediaButton) {
				listener.onItemClick(position, OnItemClickListener.MESSAGE_MEDIA);
			}
		}
	}


	@Override
	public void onTagClick(String tag) {
		if (listener != null) {
			listener.onTextClick(tag, true);
		}
	}


	@Override
	public void onLinkClick(String link) {
		if (listener != null) {
			listener.onTextClick(link, false);
		}
	}

	/**
	 * set item content
	 */
	public void setContent(Message message) {
		User sender = message.getSender();
		Spanned textSpan = Tagger.makeTextWithLinks(message.getText(), settings.getHighlightColor(), this);

		username.setText(sender.getUsername());
		screenname.setText(sender.getScreenname());
		time.setText(StringTools.formatCreationTime(itemView.getResources(), message.getTimestamp()));
		text.setText(textSpan);
		if (sender.isVerified()) {
			verifiedIcon.setVisibility(View.VISIBLE);
		} else {
			verifiedIcon.setVisibility(View.GONE);
		}
		if (sender.isProtected()) {
			lockedIcon.setVisibility(View.VISIBLE);
		} else {
			lockedIcon.setVisibility(View.GONE);
		}
		if (message.getMedia().length > 0) {
			mediaButton.setVisibility(View.VISIBLE);
		} else {
			mediaButton.setVisibility(View.GONE);
		}
		String profileImageUrl = sender.getProfileImageThumbnailUrl();
		if (settings.imagesEnabled() && !profileImageUrl.isEmpty()) {
			Transformation roundCorner = new RoundedCornersTransformation(2, 0);
			picasso.load(profileImageUrl).transform(roundCorner).error(R.drawable.no_image).into(profile);
		} else {
			profile.setImageResource(0);
		}
	}

	/**
	 * set item click listener
	 */
	public void setOnMessageClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	/**
	 * item click listener
	 * todo find a replace for this listener class
	 */
	public interface OnItemClickListener extends OnHolderClickListener {

		/**
		 * @param text   clicked text
		 * @param isLink true if text is a link
		 */
		void onTextClick(String text, boolean isLink);
	}
}