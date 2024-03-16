package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.Tag;

/**
 * Async loader used to follow/feature tags
 *
 * @author nuclearfog
 */
public class TagAction extends AsyncExecutor<TagAction.Param, TagAction.Result> {

	private Connection connection;

	/**
	 *
	 */
	public TagAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.action) {
				case Param.LOAD:
					Tag result = connection.showTag(param.name);
					return new Result(Result.LOAD, result, null);

				case Param.FOLLOW:
					result = connection.followTag(param.name);
					return new Result(Result.FOLLOW, result, null);

				case Param.UNFOLLOW:
					result = connection.unfollowTag(param.name);
					return new Result(Result.UNFOLLOW, result, null);

				case Param.FEATURE:
					result = connection.featureTag(param.name);
					return new Result(Result.FEATURE, result, null);

				case Param.UNFEATURE:
					result = connection.unfeatureTag(param.id);
					return new Result(Result.UNFEATURE, result, null);
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, null, exception);
		}
		return null;
	}

	/**
	 *
	 */
	public static class Param {

		public static final int LOAD = 1;
		public static final int FOLLOW = 2;
		public static final int UNFOLLOW = 3;
		public static final int FEATURE = 4;
		public static final int UNFEATURE = 5;

		final String name;
		final int action;
		final long id;

		/**
		 * @param action action to apply on a tag
		 * @param name   name of the tag
		 */
		public Param(int action, String name) {
			this(action, name, 0L);
		}

		/**
		 * @param action action to apply on a tag
		 * @param name   name of the tag
		 * @param id     tag ID or '0' if not set
		 */
		public Param(int action, String name, long id) {
			this.name = name;
			this.action = action;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int LOAD = 10;
		public static final int FOLLOW = 11;
		public static final int UNFOLLOW = 12;
		public static final int FEATURE = 13;
		public static final int UNFEATURE = 14;

		@Nullable
		public final ConnectionException exception;
		@Nullable
		public final Tag tag;
		public final int action;

		/**
		 * @param action action applied on the tag
		 * @param tag    updated tag or null if an error occured
		 */
		Result(int action, @Nullable Tag tag, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.tag = tag;
			this.action = action;
		}
	}
}