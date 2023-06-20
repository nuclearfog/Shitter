package org.nuclearfog.twidda.backend.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * This class provides methods to handle {@link ConnectionException}
 *
 * @author nuclearfog
 */
public class ErrorUtils {

	private ErrorUtils() {
	}

	/**
	 * show toast notification with detailed error message
	 *
	 * @param exception connection exception
	 */
	public static void showErrorMessage(Context context, @Nullable ConnectionException exception) {
		if (context != null) {
			String errorMessage = getErrorMessage(context, exception);
			if (errorMessage != null) {
				Toast.makeText(context.getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * get error message string
	 *
	 * @param context   application context
	 * @param exception connection exception
	 * @return message string
	 */
	public static String getErrorMessage(Context context, @Nullable ConnectionException exception) {
		if (exception != null) {
			switch (exception.getErrorCode()) {
				case ConnectionException.RATE_LIMIT_EX:
					if (exception.getTimeToWait() > 0) {
						String errMsg = context.getString(R.string.error_limit_exceeded);
						if (exception.getTimeToWait() >= 60)
							errMsg += " " + exception.getTimeToWait() / 60 + "m";
						errMsg += " " + exception.getTimeToWait() % 60 + "s";
						return errMsg;
					}
					return context.getString(R.string.error_rate_limit);

				case ConnectionException.NETWORK_CONNECTION:
				case ConnectionException.SERVICE_UNAVAILABLE:
					return context.getString(R.string.error_service_unavailable);

				case ConnectionException.USER_NOT_FOUND:
					return context.getString(R.string.error_user_not_found);

				case ConnectionException.RESOURCE_NOT_FOUND:
					return context.getString(R.string.error_not_found);

				case ConnectionException.NOT_AUTHORIZED:
					return context.getString(R.string.error_not_authorized);

				case ConnectionException.STATUS_LENGTH:
					return context.getString(R.string.error_status_length);

				case ConnectionException.INTERRUPTED:
					return null; // ignore exceptions caused by task termination

				case ConnectionException.DUPLICATE_STATUS:
					return context.getString(R.string.error_duplicate_status);

				case ConnectionException.MESSAGE_NOT_SENT:
					return context.getString(R.string.error_dm_send);

				case ConnectionException.MESSAGE_LENGTH:
					return context.getString(R.string.error_dm_length);

				case ConnectionException.TOKEN_EXPIRED:
					return context.getString(R.string.error_accesstoken);

				case ConnectionException.NO_CONNECTION:
					return context.getString(R.string.error_connection_failed);

				case ConnectionException.API_KEYS_ERROR:
					return context.getString(R.string.error_corrupt_api_key);

				case ConnectionException.STATUS_CANT_REPLY:
					return context.getString(R.string.error_cant_reply_to_status);

				case ConnectionException.ACCOUNT_UPDATE_FAILED:
					return context.getString(R.string.error_acc_update);

				case ConnectionException.EMPTY_TEXT:
					return context.getString(R.string.error_empty_text);

				case ConnectionException.HTTP_TIMEOUT:
					return context.getString(R.string.error_result_cancelled);

				case ConnectionException.HTTP_FORBIDDEN:
					return context.getString(R.string.error_forbidden_api_access);

				case ConnectionException.INVALID_MEDIA:
					return context.getString(R.string.error_invalid_media);

				case ConnectionException.APP_SUSPENDED:
					GlobalSettings settings = GlobalSettings.get(context);
					if (settings.getLogin().usingDefaultTokens())
						return context.getString(R.string.error_api_key_expired);
					return context.getString(R.string.error_api_access_denied);

				case ConnectionException.ERROR_API_ACCESS_DENIED:
					return context.getString(R.string.error_api_access_limited);

				case ConnectionException.JSON_FORMAT:
					return context.getString(R.string.error_json_format);

				case ConnectionException.ERROR_NOT_DEFINED:
					if (exception.getMessage() != null && !exception.getMessage().isEmpty()) {
						return exception.getMessage();
					}
					break;
			}
		}
		return context.getString(R.string.error_not_defined);
	}
}