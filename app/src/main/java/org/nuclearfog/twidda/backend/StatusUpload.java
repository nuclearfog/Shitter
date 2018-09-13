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

public class StatusUpload extends AsyncTask<Object, Void, Boolean> implements OnClickListener {

    private WeakReference<TweetPopup> ui;
    private TwitterEngine mTwitter;
    private LayoutInflater inflater;
    private Dialog popup;
    private String[] path;

    public StatusUpload(TweetPopup context, String[] path) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        inflater = LayoutInflater.from(context);
        popup = new Dialog(context);
        this.path = path;
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
                if (!isCancelled())
                    cancel(true);
            }
        });
        popup.show();
    }

    @Override
    protected Boolean doInBackground(Object... args) {
        try {
            Long id = -1L;
            String tweet = (String) args[0];
            if(args.length > 1) {
                id = (Long) args[1];
            }
            if(path.length == 0) {
                mTwitter.sendStatus(tweet,id);
            } else {
                mTwitter.sendStatus(tweet,id,path);
            }
            return true;
        } catch (TwitterException err) {
            return false;
        } catch (Exception err) {
            Log.e("Status Upload", err.getMessage());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        TweetPopup connect = ui.get();
        if (connect != null) {
            popup.dismiss();
            if (success) {
                Toast.makeText(connect, R.string.tweet_sent, Toast.LENGTH_LONG).show();
                connect.finish();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(ui.get());
                builder.setTitle(R.string.error).setMessage(R.string.error_sending_tweet)
                        .setPositiveButton(R.string.retry, this)
                        .setNegativeButton(R.string.cancel, null).show();
            }
        }
    }

    @Override
    public void onClick(DialogInterface d, int id) {
        ui.get().send();
    }

    public interface OnTweetSending {
        void send();
    }
}