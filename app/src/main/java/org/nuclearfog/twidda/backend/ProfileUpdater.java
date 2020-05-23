package org.nuclearfog.twidda.backend;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.Window;
import android.widget.ProgressBar;

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

    private WeakReference<ProfileEditor> ui;
    private WeakReference<Dialog> popup;
    private UserHolder userHolder;
    private TwitterEngine mTwitter;
    private EngineException twException;
    private AppDatabase db;


    public ProfileUpdater(ProfileEditor context) {
        ui = new WeakReference<>(context);
        popup = new WeakReference<>(new Dialog(context));
        mTwitter = TwitterEngine.getInstance(context);
        db = new AppDatabase(ui.get());
    }


    public ProfileUpdater(ProfileEditor context, UserHolder userHolder) {
        this(context);
        this.userHolder = userHolder;
    }


    @Override
    protected void onPreExecute() {
        if (popup.get() != null && ui.get() != null) {
            Dialog window = popup.get();
            window.requestWindowFeature(Window.FEATURE_NO_TITLE);
            window.setCanceledOnTouchOutside(false);
            window.setContentView(new ProgressBar(ui.get()));
            if (window.getWindow() != null)
                window.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            window.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (getStatus() == Status.RUNNING) {
                        cancel(true);
                        ui.get().finish();
                    }
                }
            });
            window.show();
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
        if (ui.get() != null && popup.get() != null) {
            popup.get().dismiss();
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