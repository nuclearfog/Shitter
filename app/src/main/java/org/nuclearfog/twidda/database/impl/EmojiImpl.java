package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.EmojiTable;
import org.nuclearfog.twidda.model.Emoji;

/**
 * Emoji datrabase implementation
 *
 * @author nuclearfog
 */
public class EmojiImpl implements Emoji {

	/**
	 * projection of the emoji table columns
	 */
	public static final String[] PROJECTION = {
			EmojiTable.CODE,
			EmojiTable.URL,
			EmojiTable.CATEGORY
	};

	private String code;
	private String url;
	private String category;

	/**
	 * @param cursor database cursor
	 */
	public EmojiImpl(Cursor cursor) {
		code = cursor.getString(0);
		url = cursor.getString(1);
		category = cursor.getString(2);
	}


	@Override
	public String getCode() {
		return code;
	}


	@Override
	public String getUrl() {
		return url;
	}


	@Override
	public String getCategory() {
		return category;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Emoji))
			return false;
		return ((Emoji)obj).getCode().equals(code);
	}


	@NonNull
	@Override
	public String toString() {
		return "code=\"" + code + "\" category=\"" + category + "\" url=\"" + url + "\"";
	}
}