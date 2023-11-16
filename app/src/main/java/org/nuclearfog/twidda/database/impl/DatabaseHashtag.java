package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.HashtagTable;
import org.nuclearfog.twidda.model.Hashtag;

/**
 * database implementation of a hashtag
 *
 * @author nuclearfog
 */
public class DatabaseHashtag implements Hashtag, HashtagTable {

	private static final long serialVersionUID = 1799880502954768985L;

	/**
	 * SQLite columns
	 */
	public static final String[] COLUMNS = {TAG_NAME, VOL, INDEX, LOCATION, ID};

	private String name = "";
	private int popularity;
	private int rank;
	private long id;
	private long locationId;

	/**
	 * @param cursor database cursor using this {@link #COLUMNS} projection
	 */
	public DatabaseHashtag(Cursor cursor) {
		String name = cursor.getString(0);
		popularity = cursor.getInt(1);
		rank = cursor.getInt(2);
		locationId = cursor.getLong(3);
		id = cursor.getLong(4);
		if (name != null) {
			this.name = name;
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public long getLocationId() {
		return locationId;
	}


	@Override
	public int getRank() {
		return rank;
	}


	@Override
	public int getPopularity() {
		return popularity;
	}


	@Override
	public boolean following() {
		return false; // todo implement this
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Hashtag))
			return false;
		Hashtag hashtag = (Hashtag) obj;
		return getName().equals(hashtag.getName()) && getLocationId() == hashtag.getLocationId();
	}


	@NonNull
	@Override
	public String toString() {
		return "name=\"" + getName() + "\" rank=" + rank;
	}
}