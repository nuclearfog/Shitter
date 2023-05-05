package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.Trend;

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
				case HashtagParam.FOLLOW:
					Trend result = connection.followHashtag(param.name);
					return new HashtagResult(HashtagResult.FOLLOW, result, null);

				case HashtagParam.UNFOLLOW:
					result = connection.unfollowHashtag(param.name);
					return new HashtagResult(HashtagResult.UNFOLLOW, result, null);
			}
		} catch (ConnectionException exception) {
			return new HashtagResult(HashtagResult.ERROR, null, exception);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return new HashtagResult(HashtagResult.ERROR, null, null);
	}

	/**
	 *
	 */
	public static class HashtagParam {

		public static final int FOLLOW = 1;
		public static final int UNFOLLOW = 2;

		final String name;
		final int mode;

		public HashtagParam(String name, int mode) {
			this.name = name;
			this.mode = mode;
		}
	}

	/**
	 *
	 */
	public static class HashtagResult {

		public static final int ERROR = -1;
		public static final int FOLLOW = 3;
		public static final int UNFOLLOW = 4;

		@Nullable
		public final ConnectionException exception;
		@Nullable
		public final Trend trend;
		public final int mode;

		HashtagResult(int mode, @Nullable Trend trend, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.trend = trend;
			this.mode = mode;
		}
	}
}