package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.ProfileEditor;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.items.UserHolder;
import org.nuclearfog.twidda.database.AppDatabase;

import java.lang.ref.WeakReference;

/**
 * Task for editing profile information and updating images
 *
 * @see ProfileEditor
 */
public class ProfileUpdater extends AsyncTask<Void, Void, TwitterUser> {

    @Nullable
    private EngineException twException;
    private WeakReference<ProfileEditor> ui;
    private UserHolder userHolder;
    private TwitterEngine mTwitter;
    private AppDatabase db;


    public ProfileUpdater(ProfileEditor context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        db = new AppDatabase(ui.get());
    }


    public ProfileUpdater(ProfileEditor context, UserHolder userHolder) {
        this(context);
        this.userHolder = userHolder;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() != null) {
            ui.get().setLoading(true);
        }
    }


    @Override
    protected TwitterUser doInBackground(Void[] v) {
        try {
            if (userHolder == null) {
                return mTwitter.getCurrentUser();
            } else {
                TwitterUser user = mTwitter.updateProfile(userHolder);
                db.storeUser(user);
            }
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(@Nullable TwitterUser user) {
        if (ui.get() != null) {
            ui.get().setLoading(false);
            if (twException != null) {
                ui.get().setError(twException);
            } else if (user != null) {
                ui.get().setUser(user);
            } else if (userHolder != null) {
                ui.get().setSuccess();
            }
        }
    }
}