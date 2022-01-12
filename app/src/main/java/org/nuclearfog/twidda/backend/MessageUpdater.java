package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.activities.MessageEditor;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;

import java.lang.ref.WeakReference;

/**
 * Background task to send a direct messages to a user
 *
 * @author nuclearfog
 * @see MessageEditor
 */
public class MessageUpdater extends AsyncTask<String, Void, Boolean> {

    private ErrorHandler.TwitterError twException;
    private WeakReference<MessageEditor> callback;
    private Twitter twitter;

    /**
     * send direct message
     *
     * @param activity Activity context
     */
    public MessageUpdater(@NonNull MessageEditor activity) {
        super();
        twitter = Twitter.get(activity);
        callback = new WeakReference<>(activity);
    }


    @Override
    protected Boolean doInBackground(String[] param) {
        try {
            // first check if user exists
            long id = twitter.showUser(param[0]).getId();
            // upload media if any
            long mediaId = -1;
            String mediaPath = param[2];
            if (mediaPath != null && !mediaPath.isEmpty()) {
                mediaId = twitter.uploadImage(param[2]);
            }
            // upload message and media ID if defined
            if (!isCancelled()) {
                twitter.sendDirectmessage(id, param[1], mediaId);
            }
            return true;
        } catch (TwitterException twException) {
            this.twException = twException;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (callback.get() != null) {
            if (success) {
                callback.get().onSuccess();
            } else {
                callback.get().onError(twException);
            }
        }
    }
}