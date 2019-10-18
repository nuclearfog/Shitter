package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.items.UserProperties;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.window.UserProfile;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;


public class ProfileLoader extends AsyncTask<Long, TwitterUser, UserProperties> {

    public enum Mode {
        LDR_PROFILE,
        ACTION_FOLLOW,
        ACTION_BLOCK,
        ACTION_MUTE
    }

    private final Mode mode;
    private WeakReference<UserProfile> ui;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private AppDatabase db;


    public ProfileLoader(@NonNull UserProfile context, Mode mode) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        db = new AppDatabase(context);
        this.mode = mode;
    }


    @Override
    protected UserProperties doInBackground(Long[] args) {
        UserProperties connection;
        TwitterUser user;
        long userId = args[0];
        try {
            switch (mode) {
                case LDR_PROFILE:
                    user = db.getUser(userId);
                    if (user != null)
                        publishProgress(user);
                    user = mTwitter.getUser(userId);
                    publishProgress(user);
                    db.storeUser(user);

                    connection = mTwitter.getConnection(userId);
                    if (!connection.isHome())
                        if (connection.isBlocked() || connection.isMuted())
                            db.muteUser(userId, true);
                        else
                            db.muteUser(userId, false);
                    return connection;

                case ACTION_FOLLOW:
                    connection = mTwitter.getConnection(userId);
                    if (!connection.isFriend())
                        user = mTwitter.followUser(userId);
                    else
                        user = mTwitter.unfollowUser(userId);
                    publishProgress(user);
                    return mTwitter.getConnection(userId);

                case ACTION_BLOCK:
                    connection = mTwitter.getConnection(userId);
                    if (!connection.isBlocked()) {
                        user = mTwitter.blockUser(userId);
                        db.muteUser(userId, true);
                    } else {
                        user = mTwitter.unblockUser(userId);
                        db.muteUser(userId, false);
                    }
                    publishProgress(user);
                    return mTwitter.getConnection(userId);

                case ACTION_MUTE:
                    connection = mTwitter.getConnection(userId);
                    if (!connection.isMuted()) {
                        user = mTwitter.muteUser(userId);
                        db.muteUser(userId, true);
                    } else {
                        user = mTwitter.unmuteUser(userId);
                        db.muteUser(userId, false);
                    }
                    publishProgress(user);
                    return mTwitter.getConnection(userId);
            }
        } catch (TwitterException err) {
            this.err = err;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(TwitterUser[] users) {
        final TwitterUser user = users[0];
        if (ui.get() != null && user != null) {
            ui.get().setUser(user);
        }
    }


    @Override
    protected void onPostExecute(@Nullable UserProperties properties) {
        if (ui.get() != null) {
            if (properties != null) {
                ui.get().setConnection(properties);
                switch (mode) {
                    case ACTION_FOLLOW:
                        if (properties.isFriend())
                            Toast.makeText(ui.get(), R.string.followed, Toast.LENGTH_SHORT).show();
                        break;

                    case ACTION_BLOCK:
                        if (properties.isBlocked())
                            Toast.makeText(ui.get(), R.string.blocked, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ui.get(), R.string.unblocked, Toast.LENGTH_SHORT).show();
                        break;

                    case ACTION_MUTE:
                        if (properties.isMuted())
                            Toast.makeText(ui.get(), R.string.muted, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ui.get(), R.string.unmuted, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            if (err != null) {
                boolean killActivity = ErrorHandler.printError(ui.get(), err);
                if (killActivity)
                    ui.get().finish();
            }
        }
    }
}