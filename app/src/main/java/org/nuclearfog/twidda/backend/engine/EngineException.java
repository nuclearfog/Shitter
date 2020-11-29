package org.nuclearfog.twidda.backend.engine;

import twitter4j.TwitterException;

import static org.nuclearfog.twidda.backend.engine.EngineException.ErrorType.NOT_AUTHORIZED;
import static org.nuclearfog.twidda.backend.engine.EngineException.ErrorType.RESOURCE_NOT_FOUND;
import static org.nuclearfog.twidda.backend.engine.EngineException.ErrorType.USER_NOT_FOUND;

/**
 * Exception class for {@link TwitterEngine}
 */
public class EngineException extends Exception {

    public enum ErrorType {
        RATE_LIMIT_EX,
        USER_NOT_FOUND,
        REQ_TOKEN_EXPIRED,
        RESOURCE_NOT_FOUND,
        CANT_SEND_DM,
        NOT_AUTHORIZED,
        TWEET_TOO_LONG,
        DUPLICATE_TWEET,
        NO_DM_TO_USER,
        DM_TOO_LONG,
        TOKEN_EXPIRED,
        NO_MEDIA_FOUND,
        NO_LINK_DEFINED,
        NO_CONNECTION,
        IMAGE_NOT_LOADED,
        ERROR_NOT_DEFINED
    }

    enum InternalErrorType {
        FILENOTFOUND,
        TOKENNOTSET,
        BITMAP_FAILURE
    }

    private final ErrorType errorType;
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
                    errorType = ErrorType.RATE_LIMIT_EX;
                    retryAfter = error.getRetryAfter();
                    break;

                case 17:
                case 50:    // USER not found
                case 63:    // USER suspended
                case 108:
                    errorType = ErrorType.USER_NOT_FOUND;
                    break;

                case 32:
                    errorType = ErrorType.REQ_TOKEN_EXPIRED;
                    break;

                case 34:    //
                case 144:   // TWEET not found
                    errorType = ErrorType.RESOURCE_NOT_FOUND;
                    break;

                case 150:
                    errorType = ErrorType.CANT_SEND_DM;
                    break;

                case 136:
                case 179:
                    errorType = ErrorType.NOT_AUTHORIZED;
                    break;

                case 186:
                    errorType = ErrorType.TWEET_TOO_LONG;
                    break;

                case 187:
                    errorType = ErrorType.DUPLICATE_TWEET;
                    break;

                case 349:
                    errorType = ErrorType.NO_DM_TO_USER;
                    break;

                case 354:
                    errorType = ErrorType.DM_TOO_LONG;
                    break;

                case 89:
                    errorType = ErrorType.TOKEN_EXPIRED;
                    break;

                default:
                    if (error.getStatusCode() == 401) {
                        errorType = ErrorType.NOT_AUTHORIZED;
                    } else if (error.isCausedByNetworkIssue()) {
                        errorType = ErrorType.NO_CONNECTION;
                    } else {
                        errorType = ErrorType.ERROR_NOT_DEFINED;
                    }
                    break;
            }
        } else {
            errorType = ErrorType.ERROR_NOT_DEFINED;
        }
    }

    /**
     * Constructor for non Twitter4J errors
     *
     * @param errorCode custom error code
     */
    EngineException(InternalErrorType errorCode) {
        switch (errorCode) {
            case FILENOTFOUND:
                errorType = ErrorType.NO_MEDIA_FOUND;
                break;

            case TOKENNOTSET:
                errorType = ErrorType.NO_LINK_DEFINED;
                break;

            case BITMAP_FAILURE:
                errorType = ErrorType.IMAGE_NOT_LOADED;
                break;

            default:
                errorType = ErrorType.ERROR_NOT_DEFINED;
                break;
        }
    }

    /**
     * get type of error defined by twitter API
     *
     * @return type of error {@link ErrorType}
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * check if a resource was not found or current user is not authorized
     *
     * @return true if resource not found or access denied
     */
    public boolean resourceNotFound() {
        return errorType == RESOURCE_NOT_FOUND || errorType == NOT_AUTHORIZED || errorType == USER_NOT_FOUND;
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