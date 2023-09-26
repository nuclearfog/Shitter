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
public class HashtagAction extends AsyncExecutor<HashtagAction.HashtagParam, HashtagAction.HashtagResult> {

	private Connection connection;

	/**
	 *
	 */
	public HashtagAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected HashtagResult doInBackground(@NonNull HashtagParam param) {
		try {
			switch (param.mode) {
				case HashtagParam.LOAD:
					Hashtag result = connection.showHashtag(param.name);
					return new HashtagResult(HashtagResult.LOAD, result, null);

				case HashtagParam.FOLLOW:
					result = connection.followHashtag(param.name);
					return new HashtagResult(HashtagResult.FOLLOW, result, null);

				case HashtagParam.UNFOLLOW:
					result = connection.unfollowHashtag(param.name);
					return new HashtagResult(HashtagResult.UNFOLLOW, result, null);

				case HashtagParam.FEATURE:
					result = connection.featureHashtag(param.name);
					return new HashtagResult(HashtagResult.FEATURE, result, null);

				case HashtagParam.UNFEATURE:
					result = connection.unfeatureHashtag(param.name);
					return new HashtagResult(HashtagResult.UNFEATURE, result, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new HashtagResult(HashtagResult.ERROR, null, exception);
		}
	}

	/**
	 *
	 */
	public static class HashtagParam {

		public static final int LOAD = 1;
		public static final int FOLLOW = 2;
		public static final int UNFOLLOW = 3;
		public static final int FEATURE = 4;
		public static final int UNFEATURE = 5;

		final String name;
		final int mode;

		public HashtagParam(int mode, String name) {
			this.name = name;
			this.mode = mode;
		}
	}

	/**
	 *
	 */
	public static class HashtagResult {

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

		HashtagResult(int mode, @Nullable Hashtag hashtag, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.hashtag = hashtag;
			this.mode = mode;
		}
	}
}