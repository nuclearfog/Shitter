package org.nuclearfog.twidda.backend.utils;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nuclearfog
 */
public class Tagger {

	/**
	 * regex patterns used to get @usernames and #hashtags
	 */
	private static final Pattern[] PATTERNS = {
			Pattern.compile("@[^#\"“”‘’«»„＂⹂‟`*'~,;‚‛:<>|^!/§%&()=?´°{}+\\-\\[\\]\\s]+"),
			Pattern.compile("#[^@#\"“”‘’«»„＂⹂‟`*'~,;‚.‛:<>|^!/§%&()=?´°{}+\\-\\[\\]\\s]+")
	};

	/**
	 * default span type
	 */
	private static final int SPAN_TYPE = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;

	/**
	 * maximum link url length before truncating
	 */
	private static final int MAX_LINK_LENGTH = 30;

	/**
	 * Make a spannable colored String with click listener
	 *
	 * @param text  String that should be spannable
	 * @param color Text Color
	 * @param l     click listener
	 * @return Spannable String
	 */
	public static Spannable makeText(@Nullable String text, final int color, @NonNull final OnTagClickListener l) {
		SpannableStringBuilder spannable = new SpannableStringBuilder();
		/// Add '@' & '#' highlighting + listener
		if (text != null && text.length() > 0) {
			spannable.append(text);
			for (Pattern pattern : PATTERNS) {
				Matcher m = pattern.matcher(spannable);
				while (m.find()) {
					int end = m.end();
					int start = m.start();
					final String tag = spannable.subSequence(start, end).toString();
					spannable.setSpan(new ClickableSpan() {
						@Override
						public void onClick(@NonNull View widget) {
							l.onTagClick(tag);
						}

						@Override
						public void updateDrawState(@NonNull TextPaint ds) {
							ds.setColor(color);
							ds.setUnderlineText(false);
						}
					}, start, end, SPAN_TYPE);
				}
			}
		}
		return spannable;
	}

	/**
	 * Make a spannable colored String with click listener
	 * http(s) links included
	 *
	 * @param text  String that should be spannable
	 * @param color Text Color
	 * @param l     click listener
	 * @return Spannable String
	 */
	public static Spannable makeTextWithLinks(@Nullable String text, final int color, @NonNull final OnTagClickListener l) {
		SpannableStringBuilder spannable = new SpannableStringBuilder(makeText(text, color, l));
		// Add link highlight + listener
		if (spannable.length() > 0) {
			Stack<Integer> indexStack = new Stack<>();
			Matcher m = Patterns.WEB_URL.matcher(spannable.toString());
			while (m.find()) {
				indexStack.push(m.start());
				indexStack.push(m.end());
			}
			while (!indexStack.empty()) {
				int end = indexStack.pop();
				int start = indexStack.pop();
				final String link = spannable.subSequence(start, end).toString();
				if (link.startsWith("https://")) {
					spannable = spannable.delete(start, start + 8);
					end -= 8;
				} else if (link.startsWith("http://")) {
					spannable = spannable.delete(start, start + 7);
					end -= 7;
				}
				if (start + MAX_LINK_LENGTH < end) {
					spannable.replace(start + MAX_LINK_LENGTH, end, "...");
					end = start + MAX_LINK_LENGTH + 3;
				}
				spannable.setSpan(new ClickableSpan() {
					@Override
					public void onClick(@NonNull View widget) {
						l.onLinkClick(link);
					}

					@Override
					public void updateDrawState(@NonNull TextPaint ds) {
						ds.setColor(color);
						ds.setUnderlineText(false);
					}
				}, start, end, SPAN_TYPE);
			}
		}
		return spannable;
	}

	/**
	 * Make a spannable String without listener
	 *
	 * @param text  String that should be spannable
	 * @param color Text Color
	 * @return Spannable String
	 */
	public static Spannable makeText(@Nullable String text, int color) {
		SpannableStringBuilder spannable = new SpannableStringBuilder();
		// Add '@' & '#' highlighting
		if (text != null && text.length() > 0) {
			spannable.append(text);
			for (Pattern pattern : PATTERNS) {
				Matcher m = pattern.matcher(spannable.toString());
				while (m.find()) {
					int end = m.end();
					int start = m.start();
					ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
					spannable.setSpan(colorSpan, start, end, SPAN_TYPE);
				}
			}
		}
		return spannable;
	}

	/**
	 * Make a spannable String without listener
	 * http(s) links included will be shorted
	 *
	 * @param text  String that should be spannable
	 * @param color Text Color
	 * @return Spannable String
	 */
	public static Spannable makeTextWithLinks(@Nullable String text, int color) {
		SpannableStringBuilder spannable = new SpannableStringBuilder(makeText(text, color));
		// Add link highlighting
		if (spannable.length() > 0) {
			Stack<Integer> indexStack = new Stack<>();
			Matcher m = Patterns.WEB_URL.matcher(spannable.toString());
			while (m.find()) {
				indexStack.push(m.start());
				indexStack.push(m.end());
			}
			while (!indexStack.empty()) {
				int end = indexStack.pop();
				int start = indexStack.pop();
				final String link = spannable.subSequence(start, end).toString();
				if (link.startsWith("https://")) {
					spannable = spannable.delete(start, start + 8);
					end -= 8;
				} else if (link.startsWith("http://")) {
					spannable = spannable.delete(start, start + 7);
					end -= 7;
				}
				if (start + MAX_LINK_LENGTH < end) {
					spannable.replace(start + MAX_LINK_LENGTH, end, "...");
					end = start + MAX_LINK_LENGTH + 3;
				}
				ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
				spannable.setSpan(colorSpan, start, end, SPAN_TYPE);
			}
		}
		return spannable;
	}

	/**
	 * Listener for clickable spans
	 */
	public interface OnTagClickListener {
		/**
		 * Called when user clicks on a tag
		 *
		 * @param tag Tag string (starting with '@', '#')
		 */
		void onTagClick(String tag);

		/**
		 * Called when user clicks on link
		 *
		 * @param link http(s) link
		 */
		void onLinkClick(String link);
	}
}
