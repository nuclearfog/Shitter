package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.TagTable;
import org.nuclearfog.twidda.model.Tag;

/**
 * database implementation of a {@link Tag}
 *
 * @author nuclearfog
 */
public class DatabaseTag implements Tag, TagTable {

	private static final long serialVersionUID = 1799880502954768985L;

	/**
	 * Table columns
	 */
	public static final String[] COLUMNS = {TAG_NAME, VOL, INDEX, LOCATION, ID, FLAGS};

	private String name = "";
	private int popularity;
	private int rank;
	private long id;
	private long locationId;
	private boolean followed;

	/**
	 * @param cursor database cursor using this {@link #COLUMNS} projection
	 */
	public DatabaseTag(Cursor cursor) {
		String name = cursor.getString(0);
		popularity = cursor.getInt(1);
		rank = cursor.getInt(2);
		locationId = cursor.getLong(3);
		id = cursor.getLong(4);
		int flags = cursor.getInt(5);
		followed = (flags & FLAG_FOLLOWED) != 0;
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
	public boolean isFollowed() {
		return followed;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Tag))
			return false;
		Tag tag = (Tag) obj;
		return getName().equals(tag.getName()) && getLocationId() == tag.getLocationId();
	}


	@NonNull
	@Override
	public String toString() {
		return "name=\"" + getName() + "\" rank=" + getRank();
	}
}