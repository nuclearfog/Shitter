package org.nuclearfog.twidda.adapter.holder;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

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

	private TextView username, screenname, receiver, time, text;
	private ImageView profile, verifiedIcon, lockedIcon;
	private ImageButton mediaButton;
	private Button answer, delete;

	private GlobalSettings settings;
	private Picasso picasso;

	private OnMessageClickListener listener;

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
		receiver = itemView.findViewById(R.id.item_message_receiver);
		time = itemView.findViewById(R.id.item_message_time);
		text = itemView.findViewById(R.id.item_message_text);
		answer = itemView.findViewById(R.id.item_message_answer);
		delete = itemView.findViewById(R.id.item_message_delete);

		AppStyles.setTheme(container, 0);
		background.setCardBackgroundColor(settings.getCardColor());
		text.setMovementMethod(LinkMovementMethod.getInstance());

		this.settings = settings;
		this.picasso = picasso;

		itemView.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != NO_POSITION && listener != null) {
			if (v == itemView) {
				listener.onMessageClick(position, OnMessageClickListener.TYPE_VIEW);
			} else if (v == answer) {
				listener.onMessageClick(position, OnMessageClickListener.TYPE_ANSWER);
			} else if (v == delete) {
				listener.onMessageClick(position, OnMessageClickListener.TYPE_DELETE);
			} else if (v == profile) {
				listener.onMessageClick(position, OnMessageClickListener.TYPE_PROFILE);
			} else if (v == mediaButton) {
				listener.onMessageClick(position, OnMessageClickListener.TYPE_MEDIA);
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
		receiver.setText(message.getReceiver().getScreenname());
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
		if (message.getMedia() != null) {
			mediaButton.setVisibility(View.VISIBLE);
		} else {
			mediaButton.setVisibility(View.GONE);
		}
		if (settings.imagesEnabled() && !sender.getImageUrl().isEmpty()) {
			String profileImageUrl;
			if (!sender.hasDefaultProfileImage()) {
				profileImageUrl = StringTools.buildImageLink(sender.getImageUrl(), settings.getImageSuffix());
			} else {
				profileImageUrl = sender.getImageUrl();
			}
			picasso.load(profileImageUrl).transform(new RoundedCornersTransformation(2, 0)).error(R.drawable.no_image).into(profile);
		} else {
			profile.setImageResource(0);
		}
	}

	/**
	 * set item click listener
	 */
	public void setOnMessageClickListener(OnMessageClickListener listener) {
		this.listener = listener;
	}

	/**
	 * item click listener
	 */
	public interface OnMessageClickListener {

		int TYPE_VIEW = 9;

		int TYPE_ANSWER = 10;

		int TYPE_PROFILE = 11;

		int TYPE_MEDIA = 12;

		int TYPE_DELETE = 1;

		/**
		 * @param position position of the item
		 * @param type     click type {@link #TYPE_ANSWER,#TYPE_PROFILE,#TYPE_MEDIA,#TYPE_DELETE}
		 */
		void onMessageClick(int position, int type);

		/**
		 * @param text   clicked text
		 * @param isLink true if text is a link
		 */
		void onTextClick(String text, boolean isLink);
	}
}