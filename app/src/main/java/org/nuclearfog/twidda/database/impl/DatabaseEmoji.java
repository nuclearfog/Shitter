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
public class DatabaseEmoji implements Emoji, EmojiTable {

	private static final long serialVersionUID = 4915542258264850899L;

	/**
	 * projection of the emoji table columns
	 */
	public static final String[] PROJECTION = {CODE, URL, CATEGORY};

	private String code = "";
	private String url = "";
	private String category = "";

	/**
	 * @param cursor database cursor
	 */
	public DatabaseEmoji(Cursor cursor) {
		String code = cursor.getString(0);
		String url = cursor.getString(1);
		String category = cursor.getString(2);

		if (code != null)
			this.code = code;
		if (url != null)
			this.url = url;
		if (category != null)
			this.category = category;
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
		return ((Emoji) obj).getCode().equals(getCode());
	}


	@NonNull
	@Override
	public String toString() {
		return "code=\"" + getCode() + "\" category=\"" + getCategory() + "\" url=\"" + getUrl() + "\"";
	}
}