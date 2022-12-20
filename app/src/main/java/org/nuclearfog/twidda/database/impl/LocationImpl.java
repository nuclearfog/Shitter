package org.nuclearfog.twidda.database.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Location;

/**
 * database implementation of a location
 *
 * @author nuclearfog
 */
public class LocationImpl implements Location {

	private static final long serialVersionUID = 3719416358210741464L;

	private int worldId = -1;
	private String name = "";
	private String coordinates = "";

	/**
	 * @param name    place name
	 * @param worldId world id
	 */
	public LocationImpl(String name, int worldId) {
		this.name = name;
		this.worldId = worldId;
	}

	/**
	 * @param name        place name
	 * @param coordinates comma separated gps coordinates
	 */
	public LocationImpl(String name, String coordinates) {
		if (name != null) {
			this.name = name;
		}
		if (coordinates != null) {
			this.coordinates = coordinates;
		}
	}


	@Override
	public String getFullName() {
		return name;
	}


	@Override
	public long getId() {
		return 0;
	}


	@Override
	public int getWorldId() {
		return worldId;
	}


	@Override
	public String getCountry() {
		return "";
	}


	@Override
	public String getPlace() {
		return "";
	}


	@Override
	public String getCoordinates() {
		return coordinates;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Location))
			return false;
		return ((Location) obj).getWorldId() == worldId;
	}


	@NonNull
	@Override
	public String toString() {
		return "id=" + worldId + " name=\"" + name + "\"";
	}
}