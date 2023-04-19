package org.nuclearfog.twidda.config.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Location;

/**
 * {@link Location} implementation for app settings
 *
 * @author nuclearfog
 */
public class ConfigLocation implements Location {

	private static final long serialVersionUID = 917895381491189806L;

	private long id;
	private String name;


	public ConfigLocation() {
		this(NO_ID, "");
	}

	/**
	 * @param name place name
	 * @param id   world id
	 */
	public ConfigLocation(long id, String name) {
		this.name = name;
		this.id = id;
	}


	@Override
	public long getId() {
		return id;
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
		return "";
	}


	@Override
	public String getFullName() {
		return name;
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