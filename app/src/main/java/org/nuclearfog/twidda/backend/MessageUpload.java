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
import org.nuclearfog.twidda.backend.items.MessageHolder;
import org.nuclearfog.twidda.window.MessagePopup;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

public class MessageUpload extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<MessagePopup> ui;
    private WeakReference<Dialog> popup;
    private TwitterEngine mTwitter;
    private TwitterException twException;
    private MessageHolder message;

    public MessageUpload(@NonNull MessagePopup c, MessageHolder message) {
        ui = new WeakReference<>(c);
        popup = new WeakReference<>(new Dialog(c));
        mTwitter = TwitterEngine.getInstance(c);
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
    protected Boolean doInBackground(Void[] v) {
        try {
            mTwitter.sendMessage(message);
            return true;
        } catch (TwitterException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() != null && popup.get() != null) {
            if (success) {
                Toast.makeText(ui.get(), R.string.dmsend, Toast.LENGTH_SHORT).show();
                ui.get().finish();
            } else {
                if (twException != null)
                    ErrorHandler.printError(ui.get(), twException);
            }
            popup.get().dismiss();
        }
    }


    @Override
    protected void onCancelled() {
        if (popup.get() != null)
            popup.get().dismiss();
    }
}