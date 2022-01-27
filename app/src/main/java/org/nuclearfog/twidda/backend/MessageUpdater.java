package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.activities.MessageEditor;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.api.holder.DirectmessageUpdate;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;

import java.lang.ref.WeakReference;

/**
 * Background task to send a direct messages to a user
 *
 * @author nuclearfog
 * @see MessageEditor
 */
public class MessageUpdater extends AsyncTask<Void, Void, Boolean> {

    private ErrorHandler.TwitterError twException;
    private WeakReference<MessageEditor> callback;
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
        callback = new WeakReference<>(activity);
        this.message = message;
    }


    @Override
    protected Boolean doInBackground(Void[] v) {
        try {
            // first check if user exists
            long id = twitter.showUser(message.getReceiver()).getId();
            // upload media if any
            long mediaId = -1;
            if (message.getMediaStream() != null) {
                mediaId = twitter.uploadMedia(message.getMediaStream());
            }
            // upload message and media ID
            if (!isCancelled()) {
                twitter.sendDirectmessage(id, message.getText(), mediaId);
            }
            // close mediastream
            message.close();
            return true;
        } catch (TwitterException twException) {
            this.twException = twException;
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