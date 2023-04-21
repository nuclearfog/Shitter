package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.PollTable;
import org.nuclearfog.twidda.model.Poll;

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
	private DatabasePollOption[] options = {};

	/**
	 *
	 */
	public DatabasePoll(Cursor cursor) {
		id = cursor.getLong(0);
		endTime = cursor.getLong(1);
		String optionStr = cursor.getString(2);
		if (optionStr != null && !optionStr.isEmpty()) {
			String[] optArray = SEPARATOR.split(optionStr);
			options = new DatabasePollOption[optArray.length];
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
	public Option[] getOptions() {
		return options;
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
			for (Option option : getOptions())
				optionsBuf.append(option).append(',');
			optionsBuf.deleteCharAt(optionsBuf.length() - 1).append(')');
		}
		return "id=" + getId() + " expired=" + getEndTime() + optionsBuf;
	}

	/**
	 *
	 */
	private static class DatabasePollOption implements Option {

		private static final long serialVersionUID = 6059042489609655610L;

		private String title;


		private DatabasePollOption(String title) {
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
			return obj instanceof Option && ((Option) obj).getTitle().equals(getTitle());
		}
	}
}