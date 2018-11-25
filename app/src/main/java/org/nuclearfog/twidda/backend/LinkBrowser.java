package org.nuclearfog.twidda.backend;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.window.TweetDetail;
import org.nuclearfog.twidda.window.UserProfile;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.TwitterException;

public class LinkBrowser extends AsyncTask<Uri, Void, Integer> {

    private static final int NO_MATCH = 0;
    private static final int GET_USER = 1;
    private static final int GET_TWEET = 2;
    private static final int FAILURE = 3;

    private WeakReference<MainActivity> ui;
    private TwitterEngine mTwitter;
    private DatabaseAdapter mData;
    private TwitterUser user;
    private Tweet tweet;
    private LayoutInflater inflater;
    private Dialog popup;

    private String errMsg = "";

    public LinkBrowser(MainActivity context) {
        ui = new WeakReference<>(context);
        popup = new Dialog(context);
        mData = new DatabaseAdapter(context);
        mTwitter = TwitterEngine.getInstance(context);
        inflater = LayoutInflater.from(context);
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
    protected Integer doInBackground(Uri... links) {
        try {
            String path = links[0].getPath();
            if (path != null) {

                Pattern pattern = Pattern.compile("[^\\/\\?]+");
                Matcher matcher = pattern.matcher(path);

                if (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    if (!matcher.find()) {
                        String username = path.substring(start, end);
                        user = mTwitter.getUser(username);
                        mData.storeUser(user);
                        return GET_USER;
                    }
                }
                if (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    String id = path.substring(start, end);
                    long tweetId = Long.parseLong(id);
                    tweet = mTwitter.getStatus(tweetId);
                    return GET_TWEET;
                }
            }
        } catch (TwitterException err) {
            errMsg = err.getErrorMessage();
            return FAILURE;
        } catch (Exception err) {
            Log.e("LinkBrowser", err.getMessage());
            return FAILURE;
        }
        return NO_MATCH;
    }


    @Override
    protected void onPostExecute(Integer mode) {
        if (ui.get() == null) return;

        popup.dismiss();

        switch (mode) {
            case GET_TWEET:
                Intent tweetActivity = new Intent(ui.get(), TweetDetail.class);
                tweetActivity.putExtra("username", tweet.getUser().getScreenname());
                tweetActivity.putExtra("userID", tweet.getUser().getId());
                tweetActivity.putExtra("tweetID", tweet.getId());
                ui.get().startActivity(tweetActivity);
                break;

            case GET_USER:
                Intent userActivity = new Intent(ui.get(), UserProfile.class);
                userActivity.putExtra("username", user.getScreenname());
                userActivity.putExtra("userID", user.getId());
                ui.get().startActivity(userActivity);
                break;

            case FAILURE:
                if (errMsg.isEmpty()) {
                    Toast.makeText(ui.get(), R.string.site_load_failure, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ui.get(), errMsg, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onCancelled(Integer i) {
        popup.dismiss();
    }
}