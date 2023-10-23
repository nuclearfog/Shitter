package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
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
import org.nuclearfog.textviewtool.LinkAndScrollMovement;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.EditedStatus;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.adapter.recyclerview.IconAdapter;

import java.util.Random;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * View holder for {@link org.nuclearfog.twidda.ui.adapter.recyclerview.EditHistoryAdapter}
 *
 * @author nuclearfog
 */
public class EditHistoryHolder extends ViewHolder {

	private static final int EMPTY_COLOR = 0x2F000000;

	private static final int IMG_SIZE = 150;

	private static final Random RND = new Random();

	private ImageView profile, icon_lock;
	private RecyclerView icon_list;
	private TextView username, screen_name, text, spoiler, sensitive, timestamp;

	private GlobalSettings settings;
	private Picasso picasso;
	private TextEmojiLoader emojiLoader;
	private Drawable placeholder;
	private IconAdapter adapter;

	private long tagId = RND.nextLong();
	private AsyncExecutor.AsyncCallback<TextEmojiLoader.Result> textResult = this::setTextEmojis;
	private AsyncExecutor.AsyncCallback<TextEmojiLoader.Result> usernameResult = this::setUsernameEmojis;

	/**
	 *
	 */
	public EditHistoryHolder(ViewGroup parent) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_status, parent, false));
		settings = GlobalSettings.get(parent.getContext());
		picasso = PicassoBuilder.get(parent.getContext());
		placeholder = new ColorDrawable(EMPTY_COLOR);
		emojiLoader = new TextEmojiLoader(parent.getContext());
		adapter = new IconAdapter(null, false);

		CardView card = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_edit_status_container);
		profile = itemView.findViewById(R.id.item_edit_status_profile);
		username = itemView.findViewById(R.id.item_edit_status_username);
		screen_name = itemView.findViewById(R.id.item_edit_status_screenname);
		text = itemView.findViewById(R.id.item_edit_status_text);
		spoiler = itemView.findViewById(R.id.item_edit_status_spoiler);
		sensitive = itemView.findViewById(R.id.item_edit_status_sensitive);
		icon_list = itemView.findViewById(R.id.item_edit_status_attachments);
		icon_lock = itemView.findViewById(R.id.item_edit_status_locked_icon);
		timestamp = itemView.findViewById(R.id.item_edit_status_created_at);

		spoiler.setCompoundDrawablesWithIntrinsicBounds(R.drawable.exclamation, 0, 0, 0);
		sensitive.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sensitive, 0, 0, 0);
		icon_list.setLayoutManager(new LinearLayoutManager(parent.getContext(), LinearLayoutManager.HORIZONTAL, false));
		icon_list.setAdapter(adapter);
		text.setMovementMethod(LinkAndScrollMovement.getInstance());
		AppStyles.setTheme(container, Color.TRANSPARENT);
		card.setCardBackgroundColor(settings.getCardColor());
	}

	/**
	 * set view content
	 *
	 * @param editedStatus edited status content
	 */
	public void setContent(EditedStatus editedStatus) {
		User author = editedStatus.getAuthor();
		// set profile image
		if (settings.imagesEnabled() && !author.getProfileImageThumbnailUrl().isEmpty()) {
			Transformation roundCorner = new RoundedCornersTransformation(2, 0);
			picasso.load(author.getProfileImageThumbnailUrl()).transform(roundCorner).resize(IMG_SIZE, IMG_SIZE)
					.placeholder(placeholder).centerCrop().error(R.drawable.no_image).into(profile);
		} else {
			profile.setImageDrawable(placeholder);
		}
		// set status text and emojis
		if (!editedStatus.getText().trim().isEmpty()) {
			Spannable textSpan = Tagger.makeTextWithLinks(editedStatus.getText(), settings.getHighlightColor());
			if (editedStatus.getEmojis().length > 0 && settings.imagesEnabled()) {
				TextEmojiLoader.Param param = new TextEmojiLoader.Param(tagId, editedStatus.getEmojis(), textSpan, text.getResources().getDimensionPixelSize(R.dimen.item_status_icon_size));
				emojiLoader.execute(param, textResult);
				textSpan = EmojiUtils.removeTags(textSpan);
			}
			text.setText(textSpan);
			text.setVisibility(View.VISIBLE);
		} else {
			text.setVisibility(View.GONE);
		}
		// set username and emojis
		if (author.getEmojis().length > 0 && !author.getUsername().trim().isEmpty() && settings.imagesEnabled()) {
			SpannableString usernameSpan = new SpannableString(author.getUsername());
			TextEmojiLoader.Param param = new TextEmojiLoader.Param(tagId, author.getEmojis(), usernameSpan, username.getResources().getDimensionPixelSize(R.dimen.item_status_icon_size));
			emojiLoader.execute(param, usernameResult);
			username.setText(EmojiUtils.removeTags(usernameSpan));
		} else {
			username.setText(author.getUsername());
		}
		if (author.isProtected()) {
			icon_lock.setVisibility(View.VISIBLE);
		} else {
			icon_lock.setVisibility(View.GONE);
		}
		if (editedStatus.isSpoiler()) {
			spoiler.setVisibility(View.VISIBLE);
		} else {
			spoiler.setVisibility(View.GONE);
		}
		if (editedStatus.isSensitive()) {
			sensitive.setVisibility(View.VISIBLE);
		} else {
			sensitive.setVisibility(View.GONE);
		}
		// setup attachment indicators
		if (settings.statusIndicatorsEnabled()) {
			icon_list.setVisibility(View.VISIBLE);
			adapter.setItems(editedStatus);
			if (adapter.isEmpty()) {
				icon_list.setVisibility(View.GONE);
			} else {
				icon_list.setVisibility(View.VISIBLE);
			}
		} else {
			icon_list.setVisibility(View.GONE);
		}
		screen_name.setText(author.getScreenname());
		timestamp.setText(StringUtils.formatCreationTime(timestamp.getResources(), editedStatus.getTimestamp()));
	}

	/**
	 * update username
	 *
	 * @param result username with emojis
	 */
	private void setUsernameEmojis(@NonNull TextEmojiLoader.Result result) {
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
	private void setTextEmojis(@NonNull TextEmojiLoader.Result result) {
		if (result.id == tagId && result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(text.getContext(), result.spannable, result.images);
			text.setText(spannable);
		}
	}
}