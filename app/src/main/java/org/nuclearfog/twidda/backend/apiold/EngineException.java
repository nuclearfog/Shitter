package org.nuclearfog.twidda.backend.apiold;

import org.nuclearfog.twidda.backend.utils.ErrorHandler.TwitterError;

import twitter4j.TwitterException;

/**
 * Exception class for {@link TwitterEngine}
 *
 * @author nuclearfog
 */
public class EngineException extends Exception implements TwitterError {

    private int errorType;
    private String msg;
    private int retryAfter;


    /**
     * Constructor for Twitter4J errors
     *
     * @param exception Twitter4J Exception
     */
    EngineException(Exception exception) {
        super(exception);
        if (exception instanceof TwitterException) {
            TwitterException error = (TwitterException) exception;
            switch (error.getErrorCode()) {
                case 88:
                case 420:   //
                case 429:   // Rate limit exceeded!
                    errorType = RATE_LIMIT_EX;
                    retryAfter = error.getRetryAfter();
                    break;

                case 17:
                case 50:    // USER not found
                case 63:    // USER suspended
                case 108:
                    errorType = USER_NOT_FOUND;
                    break;

                case 32:
                    errorType = ACCESS_TOKEN_DEAD;
                    break;

                case 416:
                    errorType = APP_SUSPENDED;
                    break;

                case 34:    //
                case 144:   // TWEET not found
                    errorType = RESOURCE_NOT_FOUND;
                    break;

                case 150:
                    errorType = CANT_SEND_DM;
                    break;

                case 120:
                    errorType = ACCOUNT_UPDATE_FAILED;
                    break;

                case 136:
                case 179:
                    errorType = NOT_AUTHORIZED;
                    break;

                case 186:
                    errorType = TWEET_TOO_LONG;
                    break;

                case 187:
                    errorType = DUPLICATE_TWEET;
                    break;

                case 349:
                    errorType = NO_DM_TO_USER;
                    break;

                case 215: // Invalid API keys
                case 261:
                    errorType = ERROR_API_ACCESS_DENIED;
                    break;

                case 354:
                    errorType = DM_TOO_LONG;
                    break;

                case 89:
                    errorType = TOKEN_EXPIRED;
                    break;

                case 385: // replying tweet that is not visible or deleted
                    errorType = TWEET_CANT_REPLY;
                    break;

                default:
                    if (error.getStatusCode() == 401) {
                        errorType = NOT_AUTHORIZED;
                    } else if (error.getStatusCode() == 403) {
                        errorType = REQUEST_FORBIDDEN;
                    } else if (error.getStatusCode() == 408) {
                        errorType = REQUEST_CANCELLED;
                    } else if (error.isCausedByNetworkIssue()) {
                        errorType = NO_CONNECTION;
                    } else {
                        errorType = ERROR_NOT_DEFINED;
                        msg = error.getErrorMessage();
                    }
                    break;
            }
        } else {
            errorType = ERROR_NOT_DEFINED;
            msg = exception.getMessage();
        }
    }

    /**
     * Constructor for app errors
     *
     * @param errorCode custom error code
     */
    EngineException(int errorCode) {
        switch (errorCode) {
            case FILENOTFOUND:
                errorType = NO_MEDIA_FOUND;
                break;

            case TOKENNOTSET:
                errorType = NO_LINK_DEFINED;
                break;

            case BITMAP_FAILURE:
                errorType = IMAGE_NOT_LOADED;
                break;

            default:
                errorType = ERROR_NOT_DEFINED;
                break;
        }
    }


    @Override
    public String getMessage() {
        if (msg == null || msg.isEmpty())
            return super.getMessage();
        return msg;
    }

    /**
     * get type of error defined by twitter API
     *
     * @return error code
     */
    public int getErrorType() {
        return errorType;
    }

    /**
     * check if a resource was not found or current user is not authorized
     *
     * @return true if resource not found or access denied
     */
    public boolean resourceNotFound() {
        return errorType == RESOURCE_NOT_FOUND || errorType == USER_NOT_FOUND;
    }

    /**
     * return time to wait after unlock access in seconds
     *
     * @return time in seconds
     */
    public int getTimeToWait() {
        return retryAfter;
    }
}