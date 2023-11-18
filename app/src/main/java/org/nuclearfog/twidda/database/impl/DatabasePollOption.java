package org.nuclearfog.twidda.database.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.PollOption;

/**
 * @author nuclearfog
 */
public class DatabasePollOption implements PollOption {

	private static final long serialVersionUID = 6059042489609655610L;

	private String title;

	/**
	 * @param title option name
	 */
	public DatabasePollOption(String title) {
		this.title = title;
	}


	@Override
	public String getTitle() {
		return title;
	}


	@Override
	public int getVotes() {
		return 0;
	}


	@Override
	public boolean isSelected() {
		return false;
	}


	@NonNull
	@Override
	public String toString() {
		return "title=\"" + getTitle() + "\" votes=" + getVotes() + " selected=" + isSelected();
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		return obj instanceof PollOption && ((PollOption) obj).getTitle().equals(getTitle());
	}
}