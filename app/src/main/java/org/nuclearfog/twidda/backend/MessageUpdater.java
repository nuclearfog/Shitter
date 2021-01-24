package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.activity.MessageEditor;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.holder.MessageHolder;

import java.lang.ref.WeakReference;

/**
 * Background task to send a direct messages to a user
 *
 * @see MessageEditor
 */
public class MessageUpdater extends AsyncTask<Void, Void, Boolean> {

    private EngineException twException;
    private WeakReference<MessageEditor> callback;
    private TwitterEngine mTwitter;
    private MessageHolder message;

    /**
     * send direct message
     *
     * @param callback Activity context
     * @param message  message to send
     */
    public MessageUpdater(@NonNull MessageEditor callback, MessageHolder message) {
        super();
        mTwitter = TwitterEngine.getInstance(callback);
        this.callback = new WeakReference<>(callback);
        this.message = message;
    }


    @Override
    protected void onPreExecute() {
        if (callback.get() != null) {
            callback.get().setLoading(true);
        }
    }


    @Override
    protected Boolean doInBackground(Void[] v) {
        try {
            mTwitter.sendMessage(message);
            return true;
        } catch (EngineException twException) {
            this.twException = twException;
            return false;
        }
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (callback.get() != null) {
            callback.get().setLoading(false);
            if (success) {
                callback.get().onSuccess();
            } else {
                callback.get().onError(twException);
            }
        }
    }
}