package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

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

	public static final String[] PROJECTION = {ID, LIMIT, EXPIRATION, OPTIONS};

	private long id;
	private long expired;
	private int limit;
	private DatabasePollOption[] options = {};

	/**
	 *
	 */
	public DatabasePoll(Cursor cursor) {
		id = cursor.getLong(0);
		limit = cursor.getInt(1);
		expired = cursor.getLong(2);
		String optionStr = cursor.getString(3);
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
	public int getLimit() {
		return limit;
	}


	@Override
	public long expirationTime() {
		return expired;
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
	public int compareTo(Poll o) {
		return 0;
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
		public boolean selected() {
			return false;
		}
	}
}