package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.MessagePopup;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.items.MessageHolder;

import java.lang.ref.WeakReference;

/**
 * Background task to send a direct messages to a user
 * @see MessagePopup
 */
public class MessageUploader extends AsyncTask<Void, Void, Boolean> {

    @Nullable
    private EngineException twException;
    private WeakReference<MessagePopup> ui;
    private TwitterEngine mTwitter;
    private MessageHolder message;

    /**
     * send direct message
     *
     * @param context Activity context
     * @param message message to send
     */
    public MessageUploader(@NonNull MessagePopup context, MessageHolder message) {
        mTwitter = TwitterEngine.getInstance(context);
        ui = new WeakReference<>(context);
        this.message = message;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() != null) {
            ui.get().setLoading(true);
        }
    }


    @Override
    protected Boolean doInBackground(Void[] v) {
        try {
            mTwitter.sendMessage(message);
            return true;
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() != null) {
            ui.get().setLoading(false);
            if (success) {
                ui.get().onSuccess();
            } else if (twException != null) {
                ui.get().onError(twException);
            }
        }
    }
}