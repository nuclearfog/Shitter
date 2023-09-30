package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.Hashtag;

/**
 * Async loader for hashtag follow/unfollow action
 *
 * @author nuclearfog
 */
public class HashtagAction extends AsyncExecutor<HashtagAction.Param, HashtagAction.Result> {

	private Connection connection;

	/**
	 *
	 */
	public HashtagAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.mode) {
				case Param.LOAD:
					Hashtag result = connection.showHashtag(param.name);
					return new Result(Result.LOAD, result, null);

				case Param.FOLLOW:
					result = connection.followHashtag(param.name);
					return new Result(Result.FOLLOW, result, null);

				case Param.UNFOLLOW:
					result = connection.unfollowHashtag(param.name);
					return new Result(Result.UNFOLLOW, result, null);

				case Param.FEATURE:
					result = connection.featureHashtag(param.name);
					return new Result(Result.FEATURE, result, null);

				case Param.UNFEATURE:
					result = connection.unfeatureHashtag(param.id);
					return new Result(Result.UNFEATURE, result, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, null, exception);
		}
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
		final int mode;
		final long id;

		public Param(int mode, String name) {
			this.name = name;
			this.mode = mode;
			id = 0L;
		}

		public Param(int mode, String name, long id) {
			this.name = name;
			this.mode = mode;
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
		public final Hashtag hashtag;
		public final int mode;

		Result(int mode, @Nullable Hashtag hashtag, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.hashtag = hashtag;
			this.mode = mode;
		}
	}
}