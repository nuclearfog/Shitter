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
public class HashtagAction extends AsyncExecutor<HashtagAction.HashtagActionParam, HashtagAction.HashtagActionResult> {

	private Connection connection;

	/**
	 *
	 */
	public HashtagAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected HashtagActionResult doInBackground(@NonNull HashtagActionParam param) {
		try {
			switch (param.mode) {
				case HashtagActionParam.LOAD:
					Hashtag result = connection.showHashtag(param.name);
					return new HashtagActionResult(HashtagActionResult.LOAD, result, null);

				case HashtagActionParam.FOLLOW:
					result = connection.followHashtag(param.name);
					return new HashtagActionResult(HashtagActionResult.FOLLOW, result, null);

				case HashtagActionParam.UNFOLLOW:
					result = connection.unfollowHashtag(param.name);
					return new HashtagActionResult(HashtagActionResult.UNFOLLOW, result, null);

				case HashtagActionParam.FEATURE:
					result = connection.featureHashtag(param.name);
					return new HashtagActionResult(HashtagActionResult.FEATURE, result, null);

				case HashtagActionParam.UNFEATURE:
					result = connection.unfeatureHashtag(param.id);
					return new HashtagActionResult(HashtagActionResult.UNFEATURE, result, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new HashtagActionResult(HashtagActionResult.ERROR, null, exception);
		}
	}

	/**
	 *
	 */
	public static class HashtagActionParam {

		public static final int LOAD = 1;
		public static final int FOLLOW = 2;
		public static final int UNFOLLOW = 3;
		public static final int FEATURE = 4;
		public static final int UNFEATURE = 5;

		final String name;
		final int mode;
		final long id;

		public HashtagActionParam(int mode, String name) {
			this.name = name;
			this.mode = mode;
			id = 0L;
		}

		public HashtagActionParam(int mode, String name, long id) {
			this.name = name;
			this.mode = mode;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class HashtagActionResult {

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

		HashtagActionResult(int mode, @Nullable Hashtag hashtag, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.hashtag = hashtag;
			this.mode = mode;
		}
	}
}