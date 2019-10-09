package org.nuclearfog.twidda.backend;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.window.MessagePopup;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

public class MessageUpload extends AsyncTask<String, Void, Boolean> {

    private WeakReference<MessagePopup> ui;
    private WeakReference<Dialog> popup;
    private TwitterEngine mTwitter;
    private TwitterException err;


    public MessageUpload(@NonNull MessagePopup c) {
        ui = new WeakReference<>(c);
        popup = new WeakReference<>(new Dialog(c));
        mTwitter = TwitterEngine.getInstance(c);
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
                        Toast.makeText(ui.get(), R.string.abort, Toast.LENGTH_SHORT).show();
                        cancel(true);
                    }
                }
            });
            window.setContentView(load);
            window.show();
        }
    }


    @Override
    protected Boolean doInBackground(String... param) {
        String username = param[0];
        String message = param[1];
        String path = param[2];
        try {
            if (!username.startsWith("@"))
                username = '@' + username;
            mTwitter.sendMessage(username, message, path);
        } catch (TwitterException err) {
            this.err = err;
            return false;
        } catch (Exception err) {
            err.printStackTrace();
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() != null && popup.get() != null) {
            if (success) {
                Toast.makeText(ui.get(), R.string.dmsend, Toast.LENGTH_SHORT).show();
                ui.get().finish();
            } else {
                if (err != null)
                    ErrorHandler.printError(ui.get(), err);
            }
            popup.get().dismiss();
        }
    }


    @Override
    protected void onCancelled() {
        if (popup.get() != null) {
            popup.get().dismiss();
        }
    }
}