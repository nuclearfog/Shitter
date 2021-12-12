package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.backend.model.Account;
import org.nuclearfog.twidda.backend.model.User;
import org.nuclearfog.twidda.database.AccountDatabase;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.fragments.AccountFragment;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * backend loader to get local accounts and user information
 *
 * @author nuclearfog
 */
public class AccountLoader extends AsyncTask<Account, Void, List<Account>> {

    private AccountDatabase accountDatabase;
    private AppDatabase appDatabase;
    private WeakReference<AccountFragment> callback;

    /**
     *
     */
    public AccountLoader(AccountFragment fragment) {
        super();
        callback = new WeakReference<>(fragment);
        accountDatabase = new AccountDatabase(fragment.requireContext());
        appDatabase = new AppDatabase(fragment.requireContext());
    }


    @Override
    protected List<Account> doInBackground(Account... param) {
        List<Account> result = null;
        try {
            // remove account if parameter is set
            if (param != null && param.length > 0) {
                accountDatabase.removeLogin(param[0].getId());
            }
            // get registered users
            result = accountDatabase.getLogins();
            // download user information
            if (!result.isEmpty()) {
                for (Account account : result) {
                    long id = account.getId();
                    User user = appDatabase.getUser(id);
                    account.attachUser(user);
                }
            }
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
            }
        }
    }
}