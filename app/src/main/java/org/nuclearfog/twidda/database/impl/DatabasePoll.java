package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.database.DatabaseAdapter.PollTable;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.PollOption;

import java.util.regex.Pattern;

/**
 * Database implementation of {@link Poll}
 *
 * @author nuclearfog
 */
public class DatabasePoll implements Poll, PollTable {

	private static final long serialVersionUID = 3534663789678017084L;

	private static final Pattern SEPARATOR = Pattern.compile(";");

	public static final String[] PROJECTION = {ID, EXPIRATION, OPTIONS};

	private long id;
	private long endTime;
	private PollOption[] options = {};

	/**
	 *
	 */
	public DatabasePoll(Cursor cursor) {
		id = cursor.getLong(0);
		endTime = cursor.getLong(1);
		String optionStr = cursor.getString(2);
		if (optionStr != null && !optionStr.isEmpty()) {
			String[] optArray = SEPARATOR.split(optionStr);
			options = new PollOption[optArray.length];
			for (int i = 0; i < optArray.length; i++) {
				options[i] = new DatabasePollOption(optArray[i]);
			}
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public boolean voted() {
		return false;
	}


	@Override
	public boolean closed() {
		return false;
	}


	@Override
	public boolean multipleChoiceEnabled() {
		return false;
	}


	@Override
	public long getEndTime() {
		return endTime;
	}


	@Override
	public int voteCount() {
		return 0;
	}


	@Override
	public PollOption[] getOptions() {
		return options;
	}


	@Override
	public Emoji[] getEmojis() {
		return new Emoji[0];
	}


	@Override
	public boolean equals(Object o) {
		return o instanceof Poll && ((Poll) o).getId() == getId();
	}


	@NonNull
	@Override
	public String toString() {
		StringBuilder optionsBuf = new StringBuilder();
		if (getOptions().length > 0) {
			optionsBuf.append(" options=(");
			for (PollOption option : getOptions())
				optionsBuf.append(option).append(',');
			optionsBuf.deleteCharAt(optionsBuf.length() - 1).append(')');
		}
		return "id=" + getId() + " expired=" + getEndTime() + optionsBuf;
	}
}