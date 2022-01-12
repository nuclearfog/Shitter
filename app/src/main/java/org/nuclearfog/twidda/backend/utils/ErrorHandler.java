package org.nuclearfog.twidda.backend.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * This class handles {@link TwitterError}
 * and prints Toast messages to current activity
 *
 * @author nuclearfog
 */
public final class ErrorHandler {

    private ErrorHandler() {
    }

    /**
     * show error messages and handle failures
     *
     * @param context current activity context
     * @param error   Error exception thrown by TwitterEngine
     */
    public static void handleFailure(@NonNull Context context, @Nullable TwitterError error) {
        String message = getErrorMessage(context, error);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * get error message string
     *
     * @param context application context
     * @param error   Twitter error
     * @return message string
     */
    public static String getErrorMessage(Context context, @Nullable TwitterError error) {
        if (error != null) {
            switch (error.getErrorType()) {
                case TwitterError.RATE_LIMIT_EX:
                    if (error.getTimeToWait() > 0) {
                        String errMsg = context.getString(R.string.error_limit_exceeded);
                        if (error.getTimeToWait() >= 60)
                            errMsg += " " + error.getTimeToWait() / 60 + "m";
                        errMsg += " " + error.getTimeToWait() % 60 + "s";
                        return errMsg;
                    }
                    return context.getString(R.string.error_rate_limit);

                case TwitterError.USER_NOT_FOUND:
                    return context.getString(R.string.error_user_not_found);

                case TwitterError.RESOURCE_NOT_FOUND:
                    return context.getString(R.string.error_not_found);

                case TwitterError.CANT_SEND_DM:
                    return context.getString(R.string.error_send_dm_to_user);

                case TwitterError.NOT_AUTHORIZED:
                    return context.getString(R.string.error_not_authorized);

                case TwitterError.TWEET_TOO_LONG:
                    return context.getString(R.string.error_status_length);

                case TwitterError.DUPLICATE_TWEET:
                    return context.getString(R.string.error_duplicate_status);

                case TwitterError.NO_DM_TO_USER:
                    return context.getString(R.string.error_dm_send);

                case TwitterError.DM_TOO_LONG:
                    return context.getString(R.string.error_dm_length);

                case TwitterError.TOKEN_EXPIRED:
                    return context.getString(R.string.error_accesstoken);

                case TwitterError.NO_MEDIA_FOUND:
                    return context.getString(R.string.error_file_not_found);

                case TwitterError.NO_LINK_DEFINED:
                    return context.getString(R.string.error_token_not_set);

                case TwitterError.NO_CONNECTION:
                    return context.getString(R.string.error_connection_failed);

                case TwitterError.IMAGE_NOT_LOADED:
                    return context.getString(R.string.error_image_loading);

                case TwitterError.ACCESS_TOKEN_DEAD:
                    return context.getString(R.string.error_corrupt_api_key);

                case TwitterError.TWEET_CANT_REPLY:
                    return context.getString(R.string.error_cant_reply_to_tweet);

                case TwitterError.ACCOUNT_UPDATE_FAILED:
                    return context.getString(R.string.error_acc_update);

                case TwitterError.REQUEST_CANCELLED:
                    return context.getString(R.string.error_result_cancelled);

                case TwitterError.REQUEST_FORBIDDEN:
                    return context.getString(R.string.error_forbidden_api_access);

                case TwitterError.APP_SUSPENDED:
                case TwitterError.ERROR_API_ACCESS_DENIED:
                    GlobalSettings settings = GlobalSettings.getInstance(context);
                    if (settings.isCustomApiSet())
                        return context.getString(R.string.error_api_access_denied);
                    return context.getString(R.string.error_api_key_expired);

                case TwitterError.ERROR_NOT_DEFINED:
                    return error.getMessage();

                default:
                    return context.getString(R.string.error_not_defined);
            }
        } else {
            return context.getString(R.string.error_not_defined);
        }
    }

    public interface TwitterError {

        int ERROR_NOT_DEFINED = -1;
        int RATE_LIMIT_EX = 0;
        int USER_NOT_FOUND = 1;
        int RESOURCE_NOT_FOUND = 2;
        int CANT_SEND_DM = 3;
        int NOT_AUTHORIZED = 4;
        int TWEET_TOO_LONG = 5;
        int DUPLICATE_TWEET = 6;
        int NO_DM_TO_USER = 7;
        int DM_TOO_LONG = 8;
        int TOKEN_EXPIRED = 9;
        int NO_MEDIA_FOUND = 10;
        int NO_LINK_DEFINED = 11;
        int NO_CONNECTION = 12;
        int IMAGE_NOT_LOADED = 13;
        int ACCESS_TOKEN_DEAD = 14;
        int TWEET_CANT_REPLY = 15;
        int ACCOUNT_UPDATE_FAILED = 16;
        int REQUEST_CANCELLED = 17;
        int REQUEST_FORBIDDEN = 18;
        int APP_SUSPENDED = 19;
        int ERROR_API_ACCESS_DENIED = 20;
        int FILENOTFOUND = 23;
        int TOKENNOTSET = 22;
        int BITMAP_FAILURE = 21;

        int getErrorType();

        int getTimeToWait();

        String getMessage();
    }
}