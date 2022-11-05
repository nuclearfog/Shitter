package org.nuclearfog.twidda.backend.api.twitter;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.api.ConnectionException;

import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * custom exception implementation containing additional information like http status code and API error code
 *
 * @author nuclearfog
 */
class TwitterException extends ConnectionException {

	private static final long serialVersionUID = -7760582201674916919L;

	private String message;
	private int httpCode = -1;
	private int responseCode = -1;
	private int retryAfter = -1;

	/**
	 * @param message exception message
	 */
	TwitterException(String message) {
		super(message);
		this.message = message;
	}

	/**
	 * create exception caused by another exception
	 */
	TwitterException(Exception e) {
		super(e);
		message = e.getMessage();
	}

	/**
	 * create exception caused by response error
	 *
	 * @param response response from API containing additional error information
	 */
	TwitterException(Response response) {
		super(response.message());
		// basic information
		this.httpCode = response.code();
		this.message = response.message();
		ResponseBody body = response.body();

		// get extra information
		if (body != null) {
			try {
				String bodyStr = body.string();
				JSONObject json = new JSONObject(bodyStr);
				JSONArray errors = json.optJSONArray("errors");
				if (errors != null) {
					JSONObject error = errors.optJSONObject(0);
					if (error != null) {
						message = error.optString("message");
						responseCode = error.optInt("code");
						retryAfter = error.optInt("x-rate-limit-remaining", -1);
					}
				} else {
					message = json.optString("error");
				}
			} catch (Exception e) {
				// ignore extra information
			}
		}
	}


	@Override
	public int getErrorCode() {
		switch (responseCode) {
			case 88:
			case 420:   //
			case 429:   // Rate limit exceeded!
				return RATE_LIMIT_EX;

			case 17:
			case 50:    // USER not found
			case 63:    // USER suspended
			case 108:
				return USER_NOT_FOUND;

			case 32:
				return API_KEYS_ERROR;

			case 416:
				return APP_SUSPENDED;

			case 34:    //
			case 144:   // TWEET not found
				return RESOURCE_NOT_FOUND;

			case 120:
				return ACCOUNT_UPDATE_FAILED;

			case 136:
			case 179:
				return NOT_AUTHORIZED;

			case 186:
				return STATUS_LENGTH;

			case 187:
				return DUPLICATE_STATUS;

			case 349:
			case 150:
				return MESSAGE_NOT_SENT;

			case 215: // Invalid API keys
			case 261:
				return ERROR_API_ACCESS_DENIED;

			case 324:
				return INVALID_MEDIA;

			case 354:
				return MESSAGE_LENGTH;

			case 89:
				return TOKEN_EXPIRED;

			case 385: // replying tweet that is not visible or deleted
				return STATUS_CANT_REPLY;

			default:
				if (httpCode == 401) {
					return NOT_AUTHORIZED;
				} else if (httpCode == 403) {
					return HTTP_FORBIDDEN;
				} else if (httpCode == 408) {
					return HTTP_TIMEOUT;
				} else if (getCause() instanceof IOException) {
					return NO_CONNECTION;
				} else {
					return ERROR_NOT_DEFINED;
				}
		}
	}


	@Override
	public int getTimeToWait() {
		return retryAfter;
	}


	@Override
	public String getMessage() {
		return message;
	}


	@NonNull
	@Override
	public String toString() {
		return "http=" + httpCode + " errorcode=" + responseCode + " message=\"" + message + "\"";
	}
}