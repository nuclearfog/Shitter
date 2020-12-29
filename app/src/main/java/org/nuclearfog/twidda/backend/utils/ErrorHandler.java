package org.nuclearfog.twidda.backend.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.engine.EngineException;

/**
 * This class handles {@link EngineException} from {@link org.nuclearfog.twidda.backend.engine.TwitterEngine}
 * and prints Toast messages to current activity
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
    public static void handleFailure(@NonNull Context context, @Nullable EngineException error) {
        if (error != null) {
            switch (error.getErrorType()) {
                case RATE_LIMIT_EX:
                    int timeToWait = error.getTimeToWait();
                    if (timeToWait > 0) {
                        String errMsg = context.getString(R.string.error_limit_exceeded) + " ";
                        if (timeToWait >= 60)
                            errMsg += timeToWait / 60 + "m ";
                        errMsg += timeToWait % 60 + "s";
                        Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.error_rate_limit, Toast.LENGTH_SHORT).show();
                    }
                    break;

                case USER_NOT_FOUND:
                    Toast.makeText(context, R.string.error_user_not_found, Toast.LENGTH_SHORT).show();
                    break;

                case APP_SUSPENDED:
                    Toast.makeText(context, R.string.error_request_token, Toast.LENGTH_SHORT).show();
                    break;

                case RESOURCE_NOT_FOUND:
                    Toast.makeText(context, R.string.error_not_found, Toast.LENGTH_SHORT).show();
                    break;

                case CANT_SEND_DM:
                    Toast.makeText(context, R.string.error_send_dm_to_user, Toast.LENGTH_SHORT).show();
                    break;

                case NOT_AUTHORIZED:
                    Toast.makeText(context, R.string.error_not_authorized, Toast.LENGTH_SHORT).show();
                    break;

                case TWEET_TOO_LONG:
                    Toast.makeText(context, R.string.error_status_length, Toast.LENGTH_SHORT).show();
                    break;

                case DUPLICATE_TWEET:
                    Toast.makeText(context, R.string.error_duplicate_status, Toast.LENGTH_SHORT).show();
                    break;

                case NO_DM_TO_USER:
                    Toast.makeText(context, R.string.error_dm_send, Toast.LENGTH_SHORT).show();
                    break;

                case DM_TOO_LONG:
                    Toast.makeText(context, R.string.error_dm_length, Toast.LENGTH_SHORT).show();
                    break;

                case TOKEN_EXPIRED:
                    Toast.makeText(context, R.string.error_accesstoken, Toast.LENGTH_SHORT).show();
                    break;

                case NO_MEDIA_FOUND:
                    Toast.makeText(context, R.string.error_file_not_found, Toast.LENGTH_SHORT).show();
                    break;

                case NO_LINK_DEFINED:
                    Toast.makeText(context, R.string.error_token_not_set, Toast.LENGTH_SHORT).show();
                    break;

                case NO_CONNECTION:
                    Toast.makeText(context, R.string.error_connection_failed, Toast.LENGTH_SHORT).show();
                    break;

                case IMAGE_NOT_LOADED:
                    Toast.makeText(context, R.string.error_image_loading, Toast.LENGTH_SHORT).show();
                    break;

                case ACCESS_TOKEN_DEAD:
                    Toast.makeText(context, R.string.error_cant_login, Toast.LENGTH_SHORT).show();
                    break;

                case TWEET_CANT_REPLY:
                    Toast.makeText(context, R.string.error_cant_reply_to_tweet, Toast.LENGTH_SHORT).show();
                    break;

                case ERROR_NOT_DEFINED:
                    if (error.getMessage() != null)
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    break;

                case ACCOUNT_UPDATE_FAILED:
                    Toast.makeText(context, R.string.error_acc_update, Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            Toast.makeText(context, R.string.error_not_defined, Toast.LENGTH_SHORT).show();
        }
    }
}