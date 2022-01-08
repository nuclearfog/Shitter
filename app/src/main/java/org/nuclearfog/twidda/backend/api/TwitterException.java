package org.nuclearfog.twidda.backend.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.ErrorHandler.TwitterError;

import java.io.IOException;

import okhttp3.Response;

/**
 *
 * @author nuclearfog
 */
public class TwitterException extends Exception implements TwitterError {

    private int httpCode = -1;
    private int errorCode = -1;
    private int retryAfter = -1;
    private String message = "";


    TwitterException(Exception e) {
        super(e);
        httpCode = -1;
    }


    TwitterException(Response response) {
        this.httpCode = response.code();
        this.message = response.message();
    }


    TwitterException(JSONObject json) {
        JSONArray errors = json.optJSONArray("errors");
        if (errors != null) {
            JSONObject error = errors.optJSONObject(0);
            if (error != null) {
                message = error.optString("message");
                errorCode = error.optInt("code");
                retryAfter = error.optInt("x-rate-limit-remaining", -1);
            }
        }
    }

    @Override
    public int getErrorType() {
        switch (errorCode) {
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
                return ACCESS_TOKEN_DEAD;

            case 416:
                return APP_SUSPENDED;

            case 34:    //
            case 144:   // TWEET not found
                return RESOURCE_NOT_FOUND;

            case 150:
                return CANT_SEND_DM;

            case 120:
                return ACCOUNT_UPDATE_FAILED;

            case 136:
            case 179:
                return NOT_AUTHORIZED;

            case 186:
                return TWEET_TOO_LONG;

            case 187:
                return DUPLICATE_TWEET;

            case 349:
                return NO_DM_TO_USER;

            case 215: // Invalid API keys
            case 261:
                return ERROR_API_ACCESS_DENIED;

            case 354:
                return DM_TOO_LONG;

            case 89:
                return TOKEN_EXPIRED;

            case 385: // replying tweet that is not visible or deleted
                return TWEET_CANT_REPLY;

            default:
                if (httpCode == 401) {
                    return NOT_AUTHORIZED;
                } else if (httpCode == 403) {
                    return REQUEST_FORBIDDEN;
                } else if (httpCode == 408) {
                    return REQUEST_CANCELLED;
                } else if (this.getCause() instanceof IOException) {
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
}