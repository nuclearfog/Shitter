package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.activity.MessageEditor;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;

import java.lang.ref.WeakReference;

/**
 * Background task to send a direct messages to a user
 *
 * @author nuclearfog
 * @see MessageEditor
 */
public class MessageUpdater extends AsyncTask<String, Void, Boolean> {

    private EngineException twException;
    private WeakReference<MessageEditor> callback;
    private TwitterEngine mTwitter;

    /**
     * send direct message
     *
     * @param callback Activity context
     */
    public MessageUpdater(@NonNull MessageEditor callback) {
        super();
        mTwitter = TwitterEngine.getInstance(callback);
        this.callback = new WeakReference<>(callback);
    }


    @Override
    protected Boolean doInBackground(String[] param) {
        try {
            mTwitter.sendMessage(param[0], param[1], param[2]);
            return true;
        } catch (EngineException twException) {
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