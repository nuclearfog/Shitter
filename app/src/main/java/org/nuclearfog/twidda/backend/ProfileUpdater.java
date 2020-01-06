package org.nuclearfog.twidda.backend;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.items.UserHolder;
import org.nuclearfog.twidda.database.AppDatabase;

import java.lang.ref.WeakReference;

import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.UserProfile.RETURN_PROFILE_CHANGED;


public class ProfileUpdater extends AsyncTask<Void, Void, TwitterUser> {

    private WeakReference<org.nuclearfog.twidda.activity.ProfileEditor> ui;
    private WeakReference<Dialog> popup;
    private UserHolder userHolder;
    private TwitterEngine mTwitter;
    private TwitterEngine.EngineException twException;
    private AppDatabase db;


    /**
     * Read User settings from server
     *
     * @param context Activity context
     */
    public ProfileUpdater(org.nuclearfog.twidda.activity.ProfileEditor context) {
        ui = new WeakReference<>(context);
        popup = new WeakReference<>(new Dialog(context));
        mTwitter = TwitterEngine.getInstance(context);
        db = new AppDatabase(ui.get());
    }


    /**
     * Write User settings to server
     *
     * @param context    Activity context
     * @param userHolder user data
     */
    public ProfileUpdater(org.nuclearfog.twidda.activity.ProfileEditor context, UserHolder userHolder) {
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
                if (!userHolder.getImageLink().isEmpty())
                    mTwitter.updateProfileImage(userHolder.getImageLink());
                TwitterUser user = mTwitter.updateProfile(userHolder);
                db.storeUser(user);
            }
        } catch (TwitterEngine.EngineException twException) {
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
            if (user != null) {
                ui.get().setUser(user);
            } else if (userHolder != null) {
                Toast.makeText(ui.get(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
                ui.get().setResult(RETURN_PROFILE_CHANGED);
                ui.get().finish();
            } else {
                Toast.makeText(ui.get(), twException.getMessageResource(), LENGTH_SHORT).show();
                ui.get().finish();
            }
        }
    }
}