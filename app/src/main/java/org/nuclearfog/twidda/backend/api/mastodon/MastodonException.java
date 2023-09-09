package org.nuclearfog.twidda.backend.api.mastodon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.backend.api.ConnectionException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import okhttp3.Response;

/**
 * custom exception used by {@link Mastodon} class
 *
 * @author nuclearfog
 */
public class MastodonException extends ConnectionException {

	private static final long serialVersionUID = 3077198050626279691L;

	private static final String MESSAGE_NOT_FOUND = "Record not found";


	private int errorCode = ERROR_NOT_DEFINED;
	private String errorMessage = "";

	/**
	 *
	 */
	MastodonException(Exception e) {
		super(e);
		if (e instanceof JSONException) {
			errorCode = JSON_FORMAT;
		} else if (e instanceof ConnectException || e instanceof UnknownHostException) {
			errorCode = NO_CONNECTION;
		} else if (getCause() instanceof InterruptedException) {
			errorCode = INTERRUPTED;
		}
	}

	/**
	 * @param response response containing error information
	 */
	MastodonException(Response response) {
		super(response.message());
		if (response.body() != null) {
			try {
				String jsonStr = response.body().string();
				if (!jsonStr.isEmpty()) {
					JSONObject json = new JSONObject(jsonStr);
					errorMessage = json.getString("error");
					String descr = json.optString("error_description", "");
					if (!descr.isEmpty()) {
						errorMessage += ": " + descr;
					}
				}
			} catch (JSONException | IOException exception) {
				if (BuildConfig.DEBUG) {
					exception.printStackTrace();
				}
			}
		}
		switch (response.code()) {
			case 404:
				if (errorMessage.startsWith(MESSAGE_NOT_FOUND)) {
					errorCode = RESOURCE_NOT_FOUND;
					break;
				}
				// fall through

			case 401:
			case 403:
				errorCode = HTTP_FORBIDDEN;
				break;

			case 429:
				errorCode = RATE_LIMIT_EX;
				break;

			case 503:
				errorCode = SERVICE_UNAVAILABLE;
				break;
		}
	}

	/**
	 * @param message error message
	 */
	MastodonException(String message) {
		super(message);
		errorMessage = message;
	}


	@Override
	public int getErrorCode() {
		return errorCode;
	}


	@Override
	public int getTimeToWait() {
		return 0; // not used
	}


	@Nullable
	@Override
	public String getMessage() {
		return errorMessage;
	}
}