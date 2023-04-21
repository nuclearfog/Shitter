package org.nuclearfog.twidda.backend.api.mastodon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.api.ConnectionException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * custom exception used by {@link Mastodon} class
 *
 * @author nuclearfog
 */
public class MastodonException extends ConnectionException {

	private static final long serialVersionUID = 3077198050626279691L;

	private static final String MESSAGE_NOT_FOUND = "Record not found";

	/**
	 * not defined error
	 */
	private static final int UNKNOWN_ERROR = -1;

	/**
	 * error caused by network connection
	 */
	private static final int ERROR_NETWORK = -2;

	/**
	 * error caused by parsing json format
	 */
	private static final int ERROR_JSON = -3;

	/**
	 * caused by interrupt
	 */
	private static final int ERROR_INTERRUPTED = -4;


	private int httpCode = 0;
	private int errorCode = UNKNOWN_ERROR;
	private String errorMessage = "";


	/**
	 *
	 */
	MastodonException(Exception e) {
		super(e);
		if (e instanceof UnknownHostException) {
			errorCode = ERROR_NETWORK;
		} else if (e instanceof JSONException) {
			errorCode = ERROR_JSON;
		} else if (e instanceof ConnectException) {
			errorCode = NO_CONNECTION;
		} else if (getCause() instanceof InterruptedException) {
			errorCode = ERROR_INTERRUPTED;
		}
	}

	/**
	 * @param response response containing error information
	 */
	MastodonException(Response response) {
		super(response.message());
		httpCode = response.code();
		ResponseBody body = response.body();
		if (body != null) {
			try {
				String jsonStr = body.string();
				JSONObject json = new JSONObject(jsonStr);
				String title = json.getString("error");
				String descr = json.optString("error_description", "");
				if (descr.isEmpty())
					errorMessage = title;
				else
					errorMessage = title + ": " + descr;
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
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
		if (errorCode != UNKNOWN_ERROR)
			return errorCode;
		switch (httpCode) {
			case 404:
				if (errorMessage.startsWith(MESSAGE_NOT_FOUND)) {
					return RESOURCE_NOT_FOUND;
				}
				// fall through

			case 401:
			case 403:
				return HTTP_FORBIDDEN;

			case 429:
				return RATE_LIMIT_EX;

			case 503:
				return SERVICE_UNAVAILABLE;

			default:
				return errorCode;
		}
	}


	@Override
	public int getTimeToWait() {
		return 0;
	}


	@Nullable
	@Override
	public String getMessage() {
		return errorMessage;
	}


	@NonNull
	@Override
	public String toString() {
		return "error_code=" + errorCode + " message=\"" + errorMessage + "\"";
	}
}