package org.nuclearfog.twidda.backend.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * This class handles {@link ConnectionException}
 * and prints Toast messages to current activity
 *
 * @author nuclearfog
 */
public final class ErrorHandler {

	private ErrorHandler() {
	}

	/**
	 * show error messages and handle failures
	 *
	 * @param context current activity context
	 * @param error   Error exception thrown by TwitterEngine
	 */
	public static void handleFailure(@NonNull Context context, @Nullable ConnectionException error) {
		String message = getErrorMessage(context, error);
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * get error message string
	 *
	 * @param context application context
	 * @param error   Twitter error
	 * @return message string
	 */
	public static String getErrorMessage(Context context, @Nullable ConnectionException error) {
		if (error != null) {
			switch (error.getErrorCode()) {
				case ConnectionException.RATE_LIMIT_EX:
					if (error.getTimeToWait() > 0) {
						String errMsg = context.getString(R.string.error_limit_exceeded);
						if (error.getTimeToWait() >= 60)
							errMsg += " " + error.getTimeToWait() / 60 + "m";
						errMsg += " " + error.getTimeToWait() % 60 + "s";
						return errMsg;
					}
					return context.getString(R.string.error_rate_limit);

				case ConnectionException.USER_NOT_FOUND:
					return context.getString(R.string.error_user_not_found);

				case ConnectionException.RESOURCE_NOT_FOUND:
					return context.getString(R.string.error_not_found);

				case ConnectionException.CANT_SEND_DM:
					return context.getString(R.string.error_send_dm_to_user);

				case ConnectionException.NOT_AUTHORIZED:
					return context.getString(R.string.error_not_authorized);

				case ConnectionException.TWEET_TOO_LONG:
					return context.getString(R.string.error_status_length);

				case ConnectionException.DUPLICATE_TWEET:
					return context.getString(R.string.error_duplicate_status);

				case ConnectionException.NO_DM_TO_USER:
					return context.getString(R.string.error_dm_send);

				case ConnectionException.DM_TOO_LONG:
					return context.getString(R.string.error_dm_length);

				case ConnectionException.TOKEN_EXPIRED:
					return context.getString(R.string.error_accesstoken);

				case ConnectionException.NO_MEDIA_FOUND:
					return context.getString(R.string.error_file_not_found);

				case ConnectionException.NO_LINK_DEFINED:
					return context.getString(R.string.error_token_not_set);

				case ConnectionException.NO_CONNECTION:
					return context.getString(R.string.error_connection_failed);

				case ConnectionException.IMAGE_NOT_LOADED:
					return context.getString(R.string.error_image_loading);

				case ConnectionException.ACCESS_TOKEN_DEAD:
					return context.getString(R.string.error_corrupt_api_key);

				case ConnectionException.TWEET_CANT_REPLY:
					return context.getString(R.string.error_cant_reply_to_tweet);

				case ConnectionException.ACCOUNT_UPDATE_FAILED:
					return context.getString(R.string.error_acc_update);

				case ConnectionException.REQUEST_CANCELLED:
					return context.getString(R.string.error_result_cancelled);

				case ConnectionException.REQUEST_FORBIDDEN:
					return context.getString(R.string.error_forbidden_api_access);

				case ConnectionException.INVALID_MEDIA:
					return context.getString(R.string.error_invalid_media);

				case ConnectionException.APP_SUSPENDED:
				case ConnectionException.ERROR_API_ACCESS_DENIED:
					GlobalSettings settings = GlobalSettings.getInstance(context);
					if (settings.isCustomApiSet())
						return context.getString(R.string.error_api_access_denied);
					return context.getString(R.string.error_api_key_expired);

				case ConnectionException.ERROR_NOT_DEFINED:
					return error.getMessage();

				default:
					return context.getString(R.string.error_not_defined);
			}
		} else {
			return context.getString(R.string.error_not_defined);
		}
	}
}