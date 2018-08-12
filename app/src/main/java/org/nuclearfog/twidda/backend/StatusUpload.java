package org.nuclearfog.twidda.backend;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.ErrorLog;
import org.nuclearfog.twidda.window.TweetPopup;

import java.lang.ref.WeakReference;

public class StatusUpload extends AsyncTask<Object, Void, Boolean> implements OnClickListener {

    private WeakReference<TweetPopup> ui;
    private TwitterEngine mTwitter;
    private LayoutInflater inflater;
    private ErrorLog errorLog;
    private Dialog popup;
    private String[] path;


    public StatusUpload(Context context, String[] path) {
        ui = new WeakReference<>((TweetPopup)context);
        mTwitter = TwitterEngine.getInstance(context);
        inflater = LayoutInflater.from(context);
        errorLog = new ErrorLog(context);
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
                if (!isCancelled())
                    cancel(true);
            }
        });
        popup.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
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
        } catch(Exception err) {
            errorLog.add("E: Upload, " + err.getMessage());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        TweetPopup connect = ui.get();
        if(connect == null)
            return;

        popup.dismiss();
        if(success) {
            Toast.makeText(connect, R.string.tweet_sent, Toast.LENGTH_LONG).show();
            connect.finish();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ui.get());
            builder.setTitle(R.string.error).setMessage(R.string.error_sending_tweet)
                    .setPositiveButton(R.string.retry, this)
                    .setNegativeButton(R.string.cancel, null).show();
        }
    }

    @Override
    public void onClick(DialogInterface d, int id) {
        TweetPopup tweetPopup = ui.get();
        tweetPopup.send();
    }

    public interface OnTweetSending {
        void send();
    }
}