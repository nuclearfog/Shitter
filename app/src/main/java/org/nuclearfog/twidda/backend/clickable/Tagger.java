package org.nuclearfog.twidda.backend.clickable;

import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Tagger {

    private static final String PATTERN = "[@#][^\\s@#\\.\\,]+";
    private static final int MODE = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;


    public static Spannable makeText(final String text, final int color, @NonNull final OnTagClickListener l) {
        SpannableStringBuilder sText = new SpannableStringBuilder(text);
        Pattern p = Pattern.compile(PATTERN);
        Matcher m = p.matcher(text);
        while (m.find()) {
            final int start = m.start();
            final int end = m.end();
            sText.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    l.onClick(text.substring(start, end));
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setColor(color);
                    ds.setUnderlineText(false);
                }
            }, start, end, MODE);
        }
        return sText;
    }


    public static Spannable makeText(String text, int color) {
        SpannableStringBuilder sText = new SpannableStringBuilder(text);
        Pattern p = Pattern.compile(PATTERN);
        Matcher m = p.matcher(text);
        while (m.find()) {
            final int start = m.start();
            final int end = m.end();
            ForegroundColorSpan sColor = new ForegroundColorSpan(color);
            sText.setSpan(sColor, start, end, MODE);
        }
        return sText;
    }


    public interface OnTagClickListener {
        void onClick(String tag);
    }
}