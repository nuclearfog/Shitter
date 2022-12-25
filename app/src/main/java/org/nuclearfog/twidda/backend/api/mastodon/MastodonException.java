package org.nuclearfog.twidda.backend.api.mastodon;

import org.nuclearfog.twidda.backend.api.ConnectionException;

import java.net.UnknownHostException;

import okhttp3.Response;

/**
 * custom exception used by {@link Mastodon} class
 *
 * @author nuclearfog
 */
public class MastodonException extends ConnectionException {

	private static final long serialVersionUID = 3077198050626279691L;

	private int errorCode = 0;

	/**
	 *
	 */
	public MastodonException(Exception e) {
		super(e);
		if (e instanceof UnknownHostException) {
			errorCode = NO_CONNECTION;
		}
	}

	/**
	 * @param response response containing error information
	 */
	public MastodonException(Response response) {
		super(response.message());
		switch (response.code()) {
			case 401:
				errorCode = NOT_AUTHORIZED;
				break;

			case 403:
			case 404:
				errorCode = HTTP_FORBIDDEN;
				break;

			case 429:
				errorCode = RATE_LIMIT_EX;
				break;

			case 422:
				errorCode = INVALID_MEDIA;
				break;

			case 503:
				errorCode = SERVICE_UNAVAILABLE;
				break;
		}
	}

	/**
	 * @param message error message
	 */
	public MastodonException(String message) {
		super(message);
	}


	@Override
	public int getErrorCode() {
		return errorCode;
	}


	@Override
	public int getTimeToWait() {
		return 0;
	}
}