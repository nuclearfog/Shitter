package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.backend.utils.Tagger;
import org.nuclearfog.twidda.backend.utils.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Field;

import java.util.Random;

/**
 * ViewHolder for {@link org.nuclearfog.twidda.ui.adapter.recyclerview.FieldAdapter}
 *
 * @author nuclearfog
 */
public class FieldHolder extends ViewHolder {

	private static final Random RND = new Random();

	private View verified;
	private TextView key, value, time;

	private TextEmojiLoader emojiLoader;
	private GlobalSettings settings;
	private OnTagClickListener listener;

	private AsyncCallback<TextEmojiLoader.Result> resultValue = this::onValueResult;
	private AsyncCallback<TextEmojiLoader.Result> resultKey = this::onKeyResult;

	private long tagId = RND.nextLong();

	/**
	 *
	 */
	public FieldHolder(ViewGroup parent, OnTagClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_field, parent, false));
		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_field_container);
		verified = itemView.findViewById(R.id.item_field_verified);
		key = itemView.findViewById(R.id.item_field_key);
		value = itemView.findViewById(R.id.item_field_value);
		time = itemView.findViewById(R.id.item_field_timestamp);
		settings = GlobalSettings.get(parent.getContext());
		emojiLoader = new TextEmojiLoader(parent.getContext());
		this.listener = listener;

		value.setMovementMethod(LinkMovementMethod.getInstance());
		background.setCardBackgroundColor(settings.getCardColor());
		AppStyles.setTheme(container, Color.TRANSPARENT);
	}

	/**
	 * set emojis of the key text
	 */
	private void onKeyResult(@NonNull TextEmojiLoader.Result result) {
		if (result.id == tagId) {
			Spannable spannable = EmojiUtils.addEmojis(key.getContext(), result.spannable, result.images);
			key.setText(spannable);
		}
	}

	/**
	 * set emojis of the value text
	 */
	private void onValueResult(@NonNull TextEmojiLoader.Result result) {
		if (result.id == tagId) {
			Spannable spannable = EmojiUtils.addEmojis(value.getContext(), result.spannable, result.images);
			value.setText(spannable);
		}
	}

	/**
	 * set view content
	 */
	public void setContent(Field field, Emoji[] emojis) {
		Spannable keySpan = new SpannableString(field.getKey());
		Spannable valueSpan = Tagger.makeTextWithLinks(field.getValue(), settings.getHighlightColor(), listener);
		if (field.getTimestamp() != 0L) {
			verified.setVisibility(View.VISIBLE);
			time.setText(R.string.field_verified);
			time.append(StringUtils.formatCreationTime(time.getResources(), field.getTimestamp()));
		} else {
			verified.setVisibility(View.GONE);
			time.setVisibility(View.GONE);
		}
		if (emojis.length > 0 && settings.imagesEnabled()) {
			// set emojis of the value field
			TextEmojiLoader.Param param = new TextEmojiLoader.Param(tagId, emojis, valueSpan, value.getResources().getDimensionPixelSize(R.dimen.item_field_emoji_size));
			emojiLoader.execute(param, resultValue);
			value.setText(EmojiUtils.removeTags(valueSpan));
			// set emojis of the key field
			param = new TextEmojiLoader.Param(tagId, emojis, keySpan, value.getResources().getDimensionPixelSize(R.dimen.item_field_emoji_size));
			emojiLoader.execute(param, resultKey);
			key.setText(EmojiUtils.removeTags(keySpan));
		} else {
			value.setText(valueSpan);
			key.setText(keySpan);
		}
	}
}