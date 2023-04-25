package org.nuclearfog.twidda.ui.adapter.holder;

import static androidx.recyclerview.widget.RecyclerView.HORIZONTAL;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
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
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.EmojiParam;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.EmojiResult;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Message;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.adapter.IconAdapter;
import org.nuclearfog.twidda.ui.adapter.IconAdapter.OnMediaClickListener;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Holder class for a message view
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.adapter.MessageAdapter
 */
public class MessageHolder extends ViewHolder implements OnClickListener, OnTagClickListener, OnMediaClickListener, AsyncCallback<EmojiResult> {

	private static final int IMG_SIZE = 150;

	private TextView username, screenname, time, text;
	private ImageView profile, verifiedIcon, lockedIcon;
	private RecyclerView iconList;
	private Button answer, delete;

	private OnItemClickListener listener;
	private GlobalSettings settings;
	private Picasso picasso;
	private IconAdapter adapter;
	private TextEmojiLoader emojiLoader;

	private long tagId;


	public MessageHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso, TextEmojiLoader emojiLoader, OnItemClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
		this.settings = settings;
		this.picasso = picasso;
		this.listener = listener;
		this.emojiLoader = emojiLoader;

		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_message_container);
		iconList = itemView.findViewById(R.id.item_message_attachment_list);
		profile = itemView.findViewById(R.id.item_message_profile);
		verifiedIcon = itemView.findViewById(R.id.item_message_verified);
		lockedIcon = itemView.findViewById(R.id.item_message_private);
		username = itemView.findViewById(R.id.item_message_username);
		screenname = itemView.findViewById(R.id.item_message_screenname);
		time = itemView.findViewById(R.id.item_message_time);
		text = itemView.findViewById(R.id.item_message_text);
		answer = itemView.findViewById(R.id.item_message_answer);
		delete = itemView.findViewById(R.id.item_message_delete);

		AppStyles.setTheme(container, Color.TRANSPARENT);
		background.setCardBackgroundColor(settings.getCardColor());
		text.setMovementMethod(LinkMovementMethod.getInstance());

		adapter = new IconAdapter(settings, false);
		adapter.addOnMediaClickListener(this);
		iconList.setLayoutManager(new LinearLayoutManager(parent.getContext(), HORIZONTAL, false));
		iconList.setAdapter(adapter);

		itemView.setOnClickListener(this);
		profile.setOnClickListener(this);
		answer.setOnClickListener(this);
		delete.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != NO_POSITION) {
			if (v == itemView) {
				listener.onItemClick(position, OnItemClickListener.MESSAGE_VIEW);
			} else if (v == answer) {
				listener.onItemClick(position, OnItemClickListener.MESSAGE_ANSWER);
			} else if (v == delete) {
				listener.onItemClick(position, OnItemClickListener.MESSAGE_DELETE);
			} else if (v == profile) {
				listener.onItemClick(position, OnItemClickListener.MESSAGE_PROFILE);
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


	@Override
	public void onMediaClick(int index) {
		int position = getLayoutPosition();
		if (position != NO_POSITION && listener != null) {
			listener.onItemClick(position, OnHolderClickListener.MESSAGE_MEDIA, index);
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
	 * set item content
	 */
	public void setContent(Message message) {
		tagId = message.getId();
		User sender = message.getSender();
		screenname.setText(sender.getScreenname());
		time.setText(StringUtils.formatCreationTime(itemView.getResources(), message.getTimestamp()));
		iconList.setVisibility(View.VISIBLE);
		adapter.addItems(message);
		if (sender.getEmojis().length > 0 && !sender.getUsername().trim().isEmpty()) {
			Spannable usernameSpan = new SpannableString(sender.getUsername());
			EmojiParam param = new EmojiParam(tagId, sender.getEmojis(), usernameSpan, username.getResources().getDimensionPixelSize(R.dimen.item_user_icon_size));
			emojiLoader.execute(param, this);
			username.setText(EmojiUtils.removeTags(usernameSpan));
		} else {
			username.setText(sender.getUsername());
		}
		if (!message.getText().trim().isEmpty()) {
			Spanned textSpan = Tagger.makeTextWithLinks(message.getText(), settings.getHighlightColor(), this);
			text.setText(textSpan);
			text.setVisibility(View.VISIBLE);
		} else {
			text.setVisibility(View.GONE);
		}
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
		if (adapter.isEmpty()) {
			iconList.setVisibility(View.GONE);
		}
		String profileImageUrl = sender.getProfileImageThumbnailUrl();
		if (settings.imagesEnabled() && !profileImageUrl.isEmpty()) {
			Transformation roundCorner = new RoundedCornersTransformation(2, 0);
			picasso.load(profileImageUrl).resize(IMG_SIZE, IMG_SIZE).centerCrop().transform(roundCorner).error(R.drawable.no_image).into(profile);
		} else {
			profile.setImageResource(0);
		}
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