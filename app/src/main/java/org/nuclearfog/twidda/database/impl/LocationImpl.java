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

	private int id;
	private String name;

	/**
	 * construct location object from local
	 *
	 * @param placeName name of locale
	 * @param worldId   woe id
	 */
	public LocationImpl(String placeName, int worldId) {
		this.name = placeName;
		this.id = worldId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Location))
			return false;
		return ((Location) obj).getId() == id;
	}

	@NonNull
	@Override
	public String toString() {
		return "id=" + id + " name=\"" + name + "\"";
	}
}