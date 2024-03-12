package org.nuclearfog.twidda.backend.api.mastodon;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.utils.StringUtils;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
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
	private int timeToWait = 0;

	/**
	 *
	 */
	MastodonException(Exception e) {
		super(e);
		if (e instanceof JSONException) {
			errorCode = JSON_FORMAT;
		} else if (e instanceof ConnectException || e instanceof UnknownHostException || e instanceof SocketTimeoutException) {
			errorCode = NO_CONNECTION;
		} else if (getCause() instanceof InterruptedException || e instanceof InterruptedIOException) {
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
				if (jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
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

			case 422:
				errorCode = INVALID_DATA;
				break;

			case 429:
				errorCode = RATE_LIMIT_EX;
				break;

			case 503:
				errorCode = SERVICE_UNAVAILABLE;
				break;
		}

		String ratelimitReset = response.header("X-RateLimit-Reset");
		if (ratelimitReset != null && !ratelimitReset.trim().isEmpty()) {
			timeToWait = (int) ((StringUtils.getIsoTime(ratelimitReset) - System.currentTimeMillis()) / 1000L);
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
		return timeToWait;
	}


	@Nullable
	@Override
	public String getMessage() {
		return errorMessage;
	}
}