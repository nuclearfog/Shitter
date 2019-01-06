package org.nuclearfog.twidda.backend;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.window.MessagePopup;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

public class MessageUpload extends AsyncTask<String, Void, Void> {

    private WeakReference<MessagePopup> ui;
    private TwitterEngine mTwitter;
    private LayoutInflater inflater;
    private Dialog popup;
    private String errorMsg = "E MessageUpload: ";
    private int returnCode = 0;



    public MessageUpload(MessagePopup c) {
        ui = new WeakReference<>(c);
        popup = new Dialog(c);
        inflater = LayoutInflater.from(c);
        mTwitter = TwitterEngine.getInstance(c);
    }


    @Override
    @SuppressLint("InflateParams")
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
    protected Void doInBackground(String... param) {
        String username = param[0];
        String message = param[1];
        String path = param[2];
        try {
            if (!username.startsWith("@"))
                username = '@' + username;
            mTwitter.sendMessage(username, message, path);
        } catch (TwitterException err) {
            returnCode = err.getErrorCode();
            errorMsg += err.getErrorMessage();
        } catch (Exception err) {
            Log.e("DirectMessage", err.getMessage());
            err.printStackTrace();
            returnCode = -1;
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void v) {
        if (ui.get() == null) return;

        popup.dismiss();
        if (returnCode == 0) {
            Toast.makeText(ui.get(), R.string.dmsend, Toast.LENGTH_SHORT).show();
            ui.get().finish();
        } else {
            switch (returnCode) {
                case 420:
                    Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_SHORT).show();
                    break;

                case 150:
                    Toast.makeText(ui.get(), R.string.cant_send_dm, Toast.LENGTH_SHORT).show();
                    break;

                case 50:
                    Toast.makeText(ui.get(), R.string.user_not_found, Toast.LENGTH_SHORT).show();
                    break;

                default:
                    Toast.makeText(ui.get(), errorMsg, Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onCancelled(Void v) {
        popup.dismiss();
    }
}