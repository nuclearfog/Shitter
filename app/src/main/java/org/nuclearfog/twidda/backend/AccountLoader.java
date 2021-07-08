package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.model.Account;
import org.nuclearfog.twidda.backend.model.User;
import org.nuclearfog.twidda.database.AccountDatabase;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.AccountFragment;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * backend loader to get local accounts and user information
 *
 * @author nuclearfog
 */
public class AccountLoader extends AsyncTask<Account, Void, List<Account>> {

    @Nullable
    private EngineException err;
    private AccountDatabase database;
    private TwitterEngine mTwitter;
    private WeakReference<AccountFragment> callback;

    private boolean loggedIn;

    /**
     *
     */
    public AccountLoader(AccountFragment fragment) {
        super();
        callback = new WeakReference<>(fragment);
        database = AccountDatabase.getInstance(fragment.requireContext());
        mTwitter = TwitterEngine.getInstance(fragment.requireContext());

        GlobalSettings settings = GlobalSettings.getInstance(fragment.requireContext());
        loggedIn = settings.isLoggedIn();
    }


    @Override
    protected List<Account> doInBackground(Account... param) {
        List<Account> result = null;
        try {
            // remove account if parameter is set
            if (param != null && param.length > 0) {
                database.removeLogin(param[0].getId());
            }
            // get registered users
            result = database.getLogins();
            // download user information
            if (!result.isEmpty()) {
                // get all user IDs
                long[] ids = new long[result.size()];
                for (int i = 0; i < ids.length; i++) {
                    ids[i] = result.get(i).getId();
                }
                // attach user information if logged in
                if (loggedIn) {
                    // get user information
                    List<User> users = mTwitter.getUsers(ids);
                    for (int i = 0; i < users.size(); i++) {
                        result.get(i).attachUser(users.get(i));
                    }
                }
            }
        } catch (EngineException err) {
            this.err = err;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return result;
    }


    @Override
    protected void onPostExecute(List<Account> accounts) {
        AccountFragment fragment = callback.get();
        if (fragment != null) {
            if (accounts != null) {
                fragment.onSuccess(accounts);
            } else {
                fragment.onError(err);
            }
        }
    }
}