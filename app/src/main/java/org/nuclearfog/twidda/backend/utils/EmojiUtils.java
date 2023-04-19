package org.nuclearfog.twidda.backend.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides method to replace tags (e.g. :tagname:) from a TextView with bitmap drawables
 *
 * @author nuclearfog
 */
public class EmojiUtils {

	/**
	 * emoji pattern used by Mastodon
	 */
	private static final Pattern EMOJI_PATTERN = Pattern.compile(":\\w+:");


	private EmojiUtils() {}

	/**
	 * replace tags with emojis
	 *
	 * @param spannable text with emoji tags (e.g. :tagname:)
	 * @param emojis    a map of emoji tags & bitmap. every emoji bitmap has its own tag
	 */
	public static Spannable addEmojis(Context context, Spannable spannable, Map<String, Bitmap> emojis) {
		if (spannable.length() > 0 && !emojis.isEmpty()) {
			Stack<Integer> indexes = getTagIndexes(spannable);
			if (indexes.isEmpty()) {
				return spannable;
			}
			SpannableStringBuilder builder = new SpannableStringBuilder(spannable);
			while (!indexes.isEmpty()) {
				int start = indexes.pop();
				int end = indexes.pop();
				String tag = builder.subSequence(start, end).toString();
				Bitmap emoji = emojis.get(tag);
				if (emoji != null) {
					ImageSpan imgSpan = new ImageSpan(context, emoji.copy(Bitmap.Config.ARGB_8888, true));
					builder.setSpan(imgSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				} else {
					builder.delete(start, end);
				}
			}
			return builder;
		}
		return spannable;
	}

	/**
	 * remove emoji tags from spannable
	 */
	public static Spannable removeTags(Spannable spannable) {
		if (spannable.length() > 0) {
			Stack<Integer> indexes = getTagIndexes(spannable);
			if (!indexes.isEmpty()) {
				SpannableStringBuilder builder = new SpannableStringBuilder(spannable);
				while (!indexes.isEmpty()) {
					int start = indexes.pop();
					int end = indexes.pop();
					builder.delete(start, end);
				}
				return builder;
			}
		}
		return spannable;
	}

	/**
	 * create a stack with indexes of emoji tags
	 *
	 * @param spannable spannable containing emoji tags
	 * @return indexes stack
	 */
	private static Stack<Integer> getTagIndexes(Spannable spannable) {
		Matcher matcher = EMOJI_PATTERN.matcher(spannable);
		Stack<Integer> indexes = new Stack<>();
		while (matcher.find()) {
			indexes.push(matcher.end());
			indexes.push(matcher.start());
		}
		return indexes;
	}
}