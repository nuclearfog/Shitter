package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.api.holder.DirectmessageUpdate;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.ui.activities.MessageEditor;

import java.lang.ref.WeakReference;

/**
 * Background task to send a direct messages to a user
 *
 * @author nuclearfog
 * @see MessageEditor
 */
public class MessageUpdater extends AsyncTask<Void, Void, Boolean> {

    private ErrorHandler.TwitterError exception;
    private WeakReference<MessageEditor> weakRef;
    private Twitter twitter;

    private DirectmessageUpdate message;

    /**
     * send direct message
     *
     * @param activity Activity context
     */
    public MessageUpdater(@NonNull MessageEditor activity, DirectmessageUpdate message) {
        super();
        twitter = Twitter.get(activity);
        weakRef = new WeakReference<>(activity);
        this.message = message;
    }


    @Override
    protected Boolean doInBackground(Void[] v) {
        try {
            // first check if user exists
            long id = twitter.showUser(message.getName()).getId();
            // upload media if any
            long mediaId = -1;
            if (message.getMediaStream() != null) {
                mediaId = twitter.uploadMedia(message.getMediaStream());
            }
            // upload message and media ID
            if (!isCancelled()) {
                twitter.sendDirectmessage(id, message.getText(), mediaId);
            }
            return true;
        } catch (TwitterException exception) {
            this.exception = exception;
        } finally {
            // close mediastream
            message.close();
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        MessageEditor activity = weakRef.get();
        if (activity != null) {
            if (success) {
                activity.onSuccess();
            } else {
                activity.onError(exception);
            }
        }
    }
}