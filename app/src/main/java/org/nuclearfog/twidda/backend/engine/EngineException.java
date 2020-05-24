package org.nuclearfog.twidda.backend.engine;

import twitter4j.TwitterException;


public class EngineException extends Exception {

    static final int FILENOTFOUND = 600;
    static final int TOKENNOTSET = 601;

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
        ERROR_NOT_DEFINED
    }

    private final ErrorType messageResource;
    private int retryAfter;


    /**
     * Constructor for Twitter4J errors
     * @param error Twitter4J Exception
     */
    EngineException(TwitterException error) {
        super(error);
        switch (error.getErrorCode()) {
            case 88:
            case 420:   //
            case 429:   // Rate limit exceeded!
                messageResource = ErrorType.RATE_LIMIT_EX;
                retryAfter = error.getRetryAfter();
                break;

            case 17:
            case 50:    // USER not found
            case 63:    // USER suspended
                messageResource = ErrorType.USER_NOT_FOUND;
                break;

            case 32:
                messageResource = ErrorType.REQ_TOKEN_EXPIRED;
                break;

            case 34:    //
            case 144:   // TWEET not found
                messageResource = ErrorType.RESOURCE_NOT_FOUND;
                break;

            case 150:
                messageResource = ErrorType.CANT_SEND_DM;
                break;

            case 136:
            case 179:
                messageResource = ErrorType.NOT_AUTHORIZED;
                break;

            case 186:
                messageResource = ErrorType.TWEET_TOO_LONG;
                break;

            case 187:
                messageResource = ErrorType.DUPLICATE_TWEET;
                break;

            case 349:
                messageResource = ErrorType.NO_DM_TO_USER;
                break;

            case 354:
                messageResource = ErrorType.DM_TOO_LONG;
                break;

            case 89:
                messageResource = ErrorType.TOKEN_EXPIRED;
                break;

            default:
                if (error.getStatusCode() == 401) {
                    messageResource = ErrorType.NOT_AUTHORIZED;
                } else if (error.isCausedByNetworkIssue()) {
                    messageResource = ErrorType.NO_CONNECTION;
                } else {
                    messageResource = ErrorType.ERROR_NOT_DEFINED;
                }
                break;
        }
    }

    /**
     * Constructor for non Twitter4J errors
     * @param errorCode custom error code
     */
    EngineException(int errorCode) {
        switch (errorCode) {
            case FILENOTFOUND:
                messageResource = ErrorType.NO_MEDIA_FOUND;
                break;

            case TOKENNOTSET:
                messageResource = ErrorType.NO_LINK_DEFINED;
                break;

            default:
                messageResource = ErrorType.ERROR_NOT_DEFINED;
                break;
        }
    }

    /**
     * get type of error defined by twitter API
     * @return type of error {@link ErrorType}
     */
    public ErrorType getErrorType() {
        return messageResource;
    }


    /**
     * return time to wait after unlock access in seconds
     * @return time in seconds
     */
    public int getTimeToWait() {
        return retryAfter;
    }
}