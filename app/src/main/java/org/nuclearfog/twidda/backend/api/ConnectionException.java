package org.nuclearfog.twidda.backend.api;

/**
 * Generic exception class used by {@link Connection} interface
 *
 * @author nuclearfog
 */
public abstract class ConnectionException extends Exception {

	private static final long serialVersionUID = 8532776434582161546L;

	/**
	 * defines an error which is not listed here
	 */
	public static final int ERROR_NOT_DEFINED = -1;

	/**
	 * indicates that an API rate limit is exceeded
	 */
	public static final int RATE_LIMIT_EX = 0;

	/**
	 * indicates that an user was not found by his ID or @name
	 */
	public static final int USER_NOT_FOUND = 1;

	/**
	 * indicates that a resource was not found (e.g. status, userlist)
	 */
	public static final int RESOURCE_NOT_FOUND = 2;

	/**
	 * indicates that an access to a resource is not permitted
	 */
	public static final int NOT_AUTHORIZED = 3;

	/**
	 * indicates that the text length was exceeded
	 */
	public static final int STATUS_LENGTH = 4;

	/**
	 * indicates that a status can't be posted twice
	 */
	public static final int DUPLICATE_STATUS = 5;

	/**
	 * indicates that a message can't be send to an user
	 */
	public static final int MESSAGE_NOT_SENT = 6;

	/**
	 * indicates that the message text is too long
	 */
	public static final int MESSAGE_LENGTH = 7;

	/**
	 * indicates that the (user) access tokens are expired
	 */
	public static final int TOKEN_EXPIRED = 8;

	/**
	 * indicates that there is no internet connection
	 */
	public static final int NO_CONNECTION = 9;

	/**
	 * API keys are not valid
	 */
	public static final int API_KEYS_ERROR = 10;

	/**
	 * indicates that a status can't be replied
	 */
	public static final int STATUS_CANT_REPLY = 11;

	/**
	 * indicates that a provile update failed
	 */
	public static final int ACCOUNT_UPDATE_FAILED = 12;

	/**
	 *
	 */
	public static final int HTTP_TIMEOUT = 13;

	/**
	 *
	 */
	public static final int HTTP_FORBIDDEN = 14;

	/**
	 * indicates that the used API access is suspended
	 */
	public static final int APP_SUSPENDED = 15;

	/**
	 * invalid API keys
	 */
	public static final int ERROR_API_ACCESS_DENIED = 16;

	/**
	 * invalid media (wrong format, size)
	 */
	public static final int INVALID_MEDIA = 17;

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