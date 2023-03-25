package org.nuclearfog.twidda.backend.utils;

import android.graphics.Bitmap;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.widget.TextView;

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
	 * @param textView textview containing text with emoji tags (e.g. :tagname:)
	 * @param emojis   a map of emoji tags & bitmap. every emoji bitmap has its own tag
	 */
	public static void addEmojis(TextView textView, Map<String, Bitmap> emojis) {
		if (textView.length() > 0) {
			SpannableStringBuilder builder = new SpannableStringBuilder(textView.getText());
			Matcher matcher = EMOJI_PATTERN.matcher(textView.getText());
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
					ImageSpan imgSpan = new ImageSpan(textView.getContext(), emoji);
					builder.setSpan(imgSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			textView.setText(builder);
		}
	}
}