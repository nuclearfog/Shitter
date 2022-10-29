package org.nuclearfog.twidda.backend.api;

/**
 * Generic exception class used by {@link Connection} interface
 *
 * @author nuclearfog
 */
public abstract class ConnectionException extends Exception {

	private static final long serialVersionUID = 8532776434582161546L;

	public static final int ERROR_NOT_DEFINED = -1;
	public static final int RATE_LIMIT_EX = 0;
	public static final int USER_NOT_FOUND = 1;
	public static final int RESOURCE_NOT_FOUND = 2;
	public static final int CANT_SEND_DM = 3;
	public static final int NOT_AUTHORIZED = 4;
	public static final int TWEET_TOO_LONG = 5;
	public static final int DUPLICATE_TWEET = 6;
	public static final int NO_DM_TO_USER = 7;
	public static final int DM_TOO_LONG = 8;
	public static final int TOKEN_EXPIRED = 9;
	public static final int NO_MEDIA_FOUND = 10;
	public static final int NO_LINK_DEFINED = 11;
	public static final int NO_CONNECTION = 12;
	public static final int IMAGE_NOT_LOADED = 13;
	public static final int ACCESS_TOKEN_DEAD = 14;
	public static final int TWEET_CANT_REPLY = 15;
	public static final int ACCOUNT_UPDATE_FAILED = 16;
	public static final int REQUEST_CANCELLED = 17;
	public static final int REQUEST_FORBIDDEN = 18;
	public static final int APP_SUSPENDED = 19;
	public static final int ERROR_API_ACCESS_DENIED = 20;
	public static final int INVALID_MEDIA = 21;

	/**
	 *
	 */
	protected ConnectionException(Exception e) {
		super(e);
	}

	/**
	 *
	 */
	protected ConnectionException(String message) {
		super(message);
	}

	/**
	 * @return error code defined in this class
	 */
	public abstract int getErrorCode();

	/**
	 * if error caused by exceeding API limit, this method returns the time to wait in seconds
	 *
	 * @return time in seconds
	 */
	public abstract int getTimeToWait();
}