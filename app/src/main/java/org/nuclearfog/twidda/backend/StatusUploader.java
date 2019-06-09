package org.nuclearfog.twidda.backend;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.TweetHolder;
import org.nuclearfog.twidda.window.TweetPopup;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

public class StatusUploader extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<TweetPopup> ui;
    private WeakReference<Dialog> popup;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private TweetHolder tweet;

    public StatusUploader(@NonNull TweetPopup context, TweetHolder tweet) {
        ui = new WeakReference<>(context);
        popup = new WeakReference<>(new Dialog(context));
        mTwitter = TwitterEngine.getInstance(context);
        this.tweet = tweet;
    }


    @Override
    protected void onPreExecute() {
        if (popup.get() == null || ui.get() == null) return;

        final Dialog window = popup.get();
        window.requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setCanceledOnTouchOutside(false);
        if (window.getWindow() != null)
            window.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        LayoutInflater inflater = LayoutInflater.from(ui.get());
        View load = inflater.inflate(R.layout.item_load, null, false);
        View cancelButton = load.findViewById(R.id.kill_button);
        window.setContentView(load);

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
        window.show();
    }


    @Override
    protected Boolean doInBackground(Void[] v) {
        try {
            mTwitter.uploadStatus(tweet);
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
        if (ui.get() == null || popup.get() == null) return;

        if (success) {
            ui.get().close();
        } else {
            if (err != null)
                ErrorHandler.printError(ui.get(), err);

            AlertDialog.Builder builder = new AlertDialog.Builder(ui.get());
            builder.setTitle(R.string.error).setMessage(R.string.error_sending_tweet)
                    .setPositiveButton(R.string.retry, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ui.get().findViewById(R.id.sendTweet).callOnClick();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null).show();
        }
        popup.get().dismiss();
    }


    @Override
    protected void onCancelled() {
        if (popup.get() == null) return;
        popup.get().dismiss();
    }
}