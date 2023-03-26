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
public class TextWithEmoji {

	private static final Pattern EMOJI_PATTERN = Pattern.compile(":\\w+:");

	private TextWithEmoji() {}

	/**
	 * replace tags with emojis
	 *
	 * @param spannable text with emoji tags (e.g. :tagname:)
	 * @param emojis    a map of emoji tags & bitmap. every emoji bitmap has its own tag
	 */
	public static Spannable addEmojis(Context context, Spannable spannable, Map<String, Bitmap> emojis) {
		if (spannable.length() > 0) {
			SpannableStringBuilder builder = new SpannableStringBuilder(spannable);
			Matcher matcher = EMOJI_PATTERN.matcher(spannable);
			Stack<Integer> indexes = new Stack<>();
			while (matcher.find()) {
				indexes.push(matcher.start());
				indexes.push(matcher.end());
			}
			while (!indexes.isEmpty()) {
				int end = indexes.pop();
				int start = indexes.pop();
				String tag = builder.subSequence(start + 1, end - 1).toString();
				Bitmap emoji = emojis.get(tag);
				if (emoji != null) {
					ImageSpan imgSpan = new ImageSpan(context, emoji);
					builder.setSpan(imgSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			return builder;
		}
		return spannable;
	}
}