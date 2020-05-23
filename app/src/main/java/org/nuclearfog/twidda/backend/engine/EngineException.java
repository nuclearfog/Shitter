package org.nuclearfog.twidda.backend.engine;

import androidx.annotation.StringRes;

import org.nuclearfog.twidda.R;

import twitter4j.TwitterException;


public class EngineException extends Exception {

    static final int FILENOTFOUND = 600;
    static final int TOKENNOTSET = 601;

    @StringRes
    private int messageResource = 0;
    private boolean errorDefined = true;
    private boolean hardFault = false;
    private boolean statusNotFound = false;
    private boolean rateLimitExceeded = false;
    private String retryAfterString = "";

    /**
     * Constructor for Twitter4J errors
     *
     * @param error Twitter4J Exception
     */
    EngineException(TwitterException error) {
        super(error);
        switch (error.getErrorCode()) {
            case 88:
            case 420:   //
            case 429:   // Rate limit exceeded!
                rateLimitExceeded = true;
                int retryAfter = error.getRetryAfter();
                if (retryAfter >= 60)
                    retryAfterString = retryAfter / 60 + "m ";
                retryAfterString += retryAfter % 60 + "s";
                break;

            case 17:
            case 50:    // USER not found
            case 63:    // USER suspended
                messageResource = R.string.error_user_not_found;
                statusNotFound = true;
                hardFault = true;
                break;

            case 32:
                messageResource = R.string.error_request_token;
                break;

            case 34:    //
            case 144:   // TWEET not found
                messageResource = R.string.error_not_found;
                statusNotFound = true;
                hardFault = true;
                break;

            case 150:
                messageResource = R.string.error_send_dm;
                break;

            case 136:
            case 179:
                messageResource = R.string.info_not_authorized;
                hardFault = true;
                break;

            case 186:
                messageResource = R.string.error_status_too_long;
                break;

            case 187:
                messageResource = R.string.error_duplicate_status;
                break;

            case 349:
                messageResource = R.string.error_dm_send;
                break;

            case 354:
                messageResource = R.string.error_dm_length;
                break;

            case 89:
                messageResource = R.string.error_accesstoken;
                break;

            default:
                errorDefined = false;
                if (error.getStatusCode() == 401)
                    messageResource = R.string.info_not_authorized;
                else
                    messageResource = R.string.error_connection_failed;
                break;
        }
    }

    /**
     * Constructor for non Twitter4J errors
     *
     * @param errorCode custom error code
     */
    EngineException(int errorCode) {
        switch (errorCode) {
            case FILENOTFOUND:
                messageResource = R.string.error_media_not_found;
                break;

            case TOKENNOTSET:
                messageResource = R.string.error_token_not_set;
                break;

            default:
                messageResource = R.string.info_error;
                break;
        }
    }

    /**
     * get String resource of error message
     *
     * @return string recource for
     */
    @StringRes
    public int getMessageResource() {
        return messageResource;
    }

    /**
     * check if error is defined by twitter exception
     *
     * @return true if error is defined
     */
    public boolean isErrorDefined() {
        return errorDefined;
    }

    /**
     * return if activity should closed
     *
     * @return true if hard fault
     */
    public boolean isHardFault() {
        return hardFault;
    }

    /**
     * return if tweet or author was not found
     *
     * @return true if author or tweet not found
     */
    public boolean statusNotFound() {
        return statusNotFound;
    }

    /**
     * Check if rate limit of twitter is exceeded
     *
     * @return true if exceeded
     */
    public boolean isRateLimitExceeded() {
        return rateLimitExceeded;
    }

    /**
     * get time to wait for new access
     *
     * @return time in seconds
     */
    public String getRetryAfter() {
        return retryAfterString;
    }
}