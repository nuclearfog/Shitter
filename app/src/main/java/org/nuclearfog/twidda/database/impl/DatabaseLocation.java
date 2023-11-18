package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.LocationTable;
import org.nuclearfog.twidda.model.Location;

/**
 * database implementation of a location
 *
 * @author nuclearfog
 */
public class DatabaseLocation implements Location, LocationTable {

	private static final long serialVersionUID = 3719416358210741464L;

	/**
	 * SQL projection
	 */
	public static final String[] PROJECTION = {ID, PLACE, COUNTRY, FULLNAME, COORDINATES};

	private long id;
	private String name = "";
	private String coordinates = "";
	private String country = "";
	private String place = "";


	public DatabaseLocation(Cursor cursor) {
		id = cursor.getLong(0);
		String place = cursor.getString(1);
		String country = cursor.getString(2);
		String name = cursor.getString(3);
		String coordinates = cursor.getString(4);

		if (place != null)
			this.place = place;
		if (country != null)
			this.country = country;
		if (name != null)
			this.name = name;
		if (coordinates != null)
			this.coordinates = coordinates;
	}


	@Override
	public String getFullName() {
		return name;
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getCountry() {
		return country;
	}


	@Override
	public String getPlace() {
		return place;
	}


	@Override
	public String getCoordinates() {
		return coordinates;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		return obj instanceof Location && ((Location) obj).getId() == getId();
	}


	@NonNull
	@Override
	public String toString() {
		return "id=" + getId() + " name=\"" + getFullName() + "\"";
	}
}