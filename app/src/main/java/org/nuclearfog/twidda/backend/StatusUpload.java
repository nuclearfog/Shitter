package org.nuclearfog.twidda.backend;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.window.TweetPopup;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

public class StatusUpload extends AsyncTask<String, Void, Boolean> {

    private WeakReference<TweetPopup> ui;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private LayoutInflater inflater;
    private Dialog popup;
    private String tweet;
    private long replyId;


    public StatusUpload(TweetPopup context, String tweet, long replyId) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        inflater = LayoutInflater.from(context);
        popup = new Dialog(context);
        this.tweet = tweet;
        this.replyId = replyId;
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
        popup.setContentView(load);

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
        popup.show();
    }


    @Override
    protected Boolean doInBackground(String... path) {
        try {
            if (path.length == 0) {
                mTwitter.sendStatus(tweet, replyId, null);
            } else {
                mTwitter.sendStatus(tweet, replyId, path);
            }
        } catch (TwitterException err) {
            this.err = err;
            return false;
        } catch (Exception err) {
            Log.e("Status Upload", err.getMessage());
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() == null) return;

        popup.dismiss();
        if (success) {
            ui.get().close();

        } else {
            if(err != null)
                ErrorHandling.printError(ui.get(),err);

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
    }


    @Override
    protected void onCancelled(Boolean b) {
        popup.dismiss();
    }
}