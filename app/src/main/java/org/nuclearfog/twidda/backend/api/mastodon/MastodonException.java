package org.nuclearfog.twidda.backend.api.mastodon;

import org.nuclearfog.twidda.backend.api.ConnectionException;

import okhttp3.Response;

/**
 * custom exception used by {@link Mastodon} class
 *
 * @author nuclearfog
 */
public class MastodonException extends ConnectionException {

	private static final long serialVersionUID = 3077198050626279691L;


	public MastodonException(Exception e) {
		super(e);
	}


	public MastodonException(Response response) {
		super(response.message());
	}


	@Override
	public int getErrorCode() {
		return 0;
	}


	@Override
	public int getTimeToWait() {
		return 0;
	}
}