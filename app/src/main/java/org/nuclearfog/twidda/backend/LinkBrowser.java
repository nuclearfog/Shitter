package org.nuclearfog.twidda.backend;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.window.TweetDetail;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.TwitterException;

public class LinkBrowser extends AsyncTask<Uri, Void, Void> {

    private WeakReference<MainActivity> ui;
    private TwitterEngine mTwitter;
    private DatabaseAdapter mData;
    private Tweet tweet;
    private LayoutInflater inflater;
    private TwitterException err;
    private Dialog popup;

    public LinkBrowser(@NonNull MainActivity context) {
        ui = new WeakReference<>(context);
        popup = new Dialog(context);
        mData = new DatabaseAdapter(context);
        mTwitter = TwitterEngine.getInstance(context);
        inflater = LayoutInflater.from(context);
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
    protected Void doInBackground(Uri... links) {
        try {
            String path = links[0].getPath();
            if (path != null) {
                Pattern linkPattern = Pattern.compile("\\/@?[\\w_]+\\/status\\/\\d{1,20}");
                Matcher linkMatch = linkPattern.matcher(path);
                if (linkMatch.matches()) {
                    Pattern idPattern = Pattern.compile("\\d{1,20}");
                    Matcher idMatcher = idPattern.matcher(path);

                    if (idMatcher.find()) {
                        int start = idMatcher.start();
                        int end = idMatcher.end();
                        String idString = path.substring(start, end);
                        long tweetId = Long.parseLong(idString);

                        tweet = mData.getStatus(tweetId);
                        if (tweet == null)
                            tweet = mTwitter.getStatus(tweetId);
                    }
                }
            }
        } catch (TwitterException err) {
            this.err = err;
        } catch (Exception err) {
            Log.e("LinkBrowser", err.getMessage());
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void mode) {
        if (ui.get() == null) return;

        popup.dismiss();

        if (tweet != null) {
            Intent tweetActivity = new Intent(ui.get(), TweetDetail.class);
            tweetActivity.putExtra("username", tweet.getUser().getScreenname());
            tweetActivity.putExtra("userID", tweet.getUser().getId());
            tweetActivity.putExtra("tweetID", tweet.getId());
            ui.get().startActivity(tweetActivity);
        } else {
            if (err != null)
                ErrorHandling.printError(ui.get(), err);
        }
    }


    @Override
    protected void onCancelled() {
        popup.dismiss();
    }
}