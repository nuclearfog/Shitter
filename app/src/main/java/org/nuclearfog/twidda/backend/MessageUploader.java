package org.nuclearfog.twidda.backend;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
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
    private WeakReference<Dialog> popup;
    private TwitterEngine mTwitter;
    private MessageHolder message;

    /**
     * send direct message
     *
     * @param context Activity context
     * @param message message to send
     */
    public MessageUploader(@NonNull MessagePopup context, MessageHolder message) {
        ui = new WeakReference<>(context);
        popup = new WeakReference<>(new Dialog(context));
        mTwitter = TwitterEngine.getInstance(context);
        this.message = message;
    }


    @Override
    protected void onPreExecute() {
        if (popup.get() != null && ui.get() != null) {
            final Dialog window = popup.get();
            window.requestWindowFeature(Window.FEATURE_NO_TITLE);
            window.setCanceledOnTouchOutside(false);
            if (window.getWindow() != null)
                window.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            View load = View.inflate(ui.get(), R.layout.item_load, null);
            View cancelButton = load.findViewById(R.id.kill_button);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    window.dismiss();
                }
            });
            window.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (getStatus() == Status.RUNNING) {
                        Toast.makeText(ui.get(), android.R.string.cancel, Toast.LENGTH_SHORT).show();
                        cancel(true);
                    }
                }
            });
            window.setContentView(load);
            window.show();
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
        if (ui.get() != null && popup.get() != null) {
            popup.get().dismiss();
            if (success) {
                ui.get().onSuccess();
            } else if (twException != null) {
                ui.get().onError(twException);
            }
        }
    }


    @Override
    protected void onCancelled() {
        if (popup.get() != null)
            popup.get().dismiss();
    }
}