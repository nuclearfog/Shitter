package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.nuclearfog.twidda.R;

import twitter4j.TwitterException;


abstract class ErrorHandling {

    /**
     * Print twitter error message
     * @param c Application Context
     * @param error TwitterException
     * @return if Activity should shut down
     */
    public static boolean printError(Context c, @NonNull TwitterException error) {

        switch(error.getErrorCode()) {

            case 420:   //
            case 429:   // Rate limit exceeded!
                Toast.makeText(c, R.string.rate_limit_exceeded, Toast.LENGTH_SHORT).show();
                break;

            case 50:    // USER not found
            case 63:    // USER suspended
            case 136:   // Blocked!
                Toast.makeText(c, R.string.user_not_found, Toast.LENGTH_SHORT).show();
                return true;

            case 34:    //
            case 144:   // TWEET not found
                Toast.makeText(c, R.string.tweet_not_found, Toast.LENGTH_SHORT).show();
                return true;

            case 150:
                Toast.makeText(c, R.string.cant_send_dm, Toast.LENGTH_SHORT).show();
                break;

            case -1:
                Toast.makeText(c, R.string.error_not_specified, Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(c, error.getErrorMessage(), Toast.LENGTH_LONG).show();
        }
        return false;
    }
}