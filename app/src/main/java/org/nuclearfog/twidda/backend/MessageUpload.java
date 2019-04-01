package org.nuclearfog.twidda.backend;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.window.MessagePopup;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

public class MessageUpload extends AsyncTask<String, Void, Boolean> {

    private WeakReference<MessagePopup> ui;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private LayoutInflater inflater;
    private Dialog popup;


    public MessageUpload(@NonNull MessagePopup c) {
        ui = new WeakReference<>(c);
        popup = new Dialog(c);
        inflater = LayoutInflater.from(c);
        mTwitter = TwitterEngine.getInstance(c);
    }


    @Override
    protected void onPreExecute() {
        popup.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popup.setCanceledOnTouchOutside(false);
        if (popup.getWindow() != null)
            popup.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View load = inflater.inflate(R.layout.item_load, null, false);
        View cancelButton = load.findViewById(R.id.kill_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
        popup.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (getStatus() == Status.RUNNING) {
                    Toast.makeText(ui.get(), R.string.abort, Toast.LENGTH_SHORT).show();
                    cancel(true);
                }
            }
        });
        popup.setContentView(load);
        popup.show();
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
            Log.e("DirectMessage", err.getMessage());
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() == null) return;

        popup.dismiss();
        if (success) {
            Toast.makeText(ui.get(), R.string.dmsend, Toast.LENGTH_SHORT).show();
            ui.get().finish();
        } else {
            if (err != null)
                ErrorHandler.printError(ui.get(), err);
        }
    }


    @Override
    protected void onCancelled() {
        popup.dismiss();
    }
}