package org.nuclearfog.twidda.backend;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.activity.TweetPopup;
import org.nuclearfog.twidda.backend.items.TweetHolder;

import java.lang.ref.WeakReference;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.Window.FEATURE_NO_TITLE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

/**
 * Background task for uploading tweet
 */
public class StatusUploader extends AsyncTask<Void, Void, Boolean> {

    @Nullable
    private TwitterEngine.EngineException twException;
    private WeakReference<TweetPopup> ui;
    private WeakReference<Dialog> popup;
    private TwitterEngine mTwitter;
    private TweetHolder tweet;

    /**
     * initialize task
     *
     * @param context Activity context
     * @param tweet   tweet information
     */
    public StatusUploader(@NonNull TweetPopup context, TweetHolder tweet) {
        ui = new WeakReference<>(context);
        popup = new WeakReference<>(new Dialog(context));
        mTwitter = TwitterEngine.getInstance(context);
        this.tweet = tweet;
    }


    @Override
    protected void onPreExecute() {
        if (popup.get() != null && ui.get() != null) {
            final Dialog loadingCircle = popup.get();
            loadingCircle.requestWindowFeature(FEATURE_NO_TITLE);
            loadingCircle.setCanceledOnTouchOutside(false);
            if (loadingCircle.getWindow() != null)
                loadingCircle.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            View load = View.inflate(ui.get(), R.layout.item_load, null);
            View cancelButton = load.findViewById(R.id.kill_button);
            loadingCircle.setContentView(load);

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadingCircle.dismiss();
                }
            });
            loadingCircle.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (ui.get() != null && getStatus() == RUNNING) {
                        Toast.makeText(ui.get(), R.string.abort, Toast.LENGTH_SHORT).show();
                        cancel(true);
                    }
                }
            });
            loadingCircle.show();
        }
    }


    @Override
    protected Boolean doInBackground(Void[] v) {
        try {
            mTwitter.uploadStatus(tweet);
        } catch (TwitterEngine.EngineException twException) {
            this.twException = twException;
            return false;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() != null && popup.get() != null) {
            if (success) {
                Toast.makeText(ui.get(), R.string.tweet_sent, LENGTH_LONG).show();
                ui.get().finish();
            } else {
                if (twException != null)
                    Toast.makeText(ui.get(), twException.getMessageResource(), LENGTH_SHORT).show();
                ui.get().showErrorMsg(tweet);
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