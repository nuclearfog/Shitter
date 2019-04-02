package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.nuclearfog.twidda.R;

import twitter4j.TwitterException;


abstract class ErrorHandler {

    /**
     * Print twitter error message
     *
     * @param c     Application Context
     * @param error TwitterException
     * @return if Activity should shut down
     */
    public static boolean printError(Context c, @NonNull TwitterException error) {

        switch (error.getErrorCode()) {
            case 88:
            case 420:   //
            case 429:   // Rate limit exceeded!
                String msg = c.getString(R.string.rate_limit_exceeded);
                msg += error.getRetryAfter();
                Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
                break;

            case 17:
            case 50:    // USER not found
            case 63:    // USER suspended
                Toast.makeText(c, R.string.user_not_found, Toast.LENGTH_SHORT).show();
                return true;

            case 32:
                Toast.makeText(c, R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                break;

            case 34:    //
            case 144:   // TWEET not found
                Toast.makeText(c, R.string.tweet_not_found, Toast.LENGTH_SHORT).show();
                return true;

            case 150:
                Toast.makeText(c, R.string.cant_send_dm, Toast.LENGTH_SHORT).show();
                break;

            case 179:
            case 136:
                Toast.makeText(c, R.string.status_private, Toast.LENGTH_SHORT).show();
                break;

            case 186:
                Toast.makeText(c, R.string.status_too_long, Toast.LENGTH_SHORT).show();
                break;

            case 187:
                Toast.makeText(c, R.string.duplicate_status, Toast.LENGTH_SHORT).show();
                break;

            case 354:
                Toast.makeText(c, R.string.directmessage_too_long, Toast.LENGTH_SHORT).show();
                break;

            case 89:
                Toast.makeText(c, R.string.error_accesstoken, Toast.LENGTH_SHORT).show();
                break;

            case -1:
            default:
                ConnectivityManager mConnect = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (mConnect.getActiveNetworkInfo() == null || !mConnect.getActiveNetworkInfo().isConnected()) {
                    Toast.makeText(c, R.string.connection_failed, Toast.LENGTH_SHORT).show();
                } else {
                    String strMsg = error.getMessage();
                    if (strMsg != null && !strMsg.trim().isEmpty())
                        Toast.makeText(c, strMsg, Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(c, R.string.error, Toast.LENGTH_LONG).show();
                }
        }
        return false;
    }
}