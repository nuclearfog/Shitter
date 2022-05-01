package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import org.nuclearfog.twidda.database.AccountDatabase;
import org.nuclearfog.twidda.ui.fragments.AccountFragment;
import org.nuclearfog.twidda.model.Account;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * backend loader to get login information of local accounts
 *
 * @author nuclearfog
 */
public class AccountLoader extends AsyncTask<Account, Void, List<Account>> {

    private AccountDatabase accountDatabase;
    private WeakReference<AccountFragment> weakRef;


    public AccountLoader(AccountFragment fragment) {
        super();
        weakRef = new WeakReference<>(fragment);
        accountDatabase = new AccountDatabase(fragment.requireContext());
    }


    @Override
    protected List<Account> doInBackground(Account... param) {
        List<Account> result = null;
        try {
            // remove account if parameter is set
            if (param.length > 0 && param[0] != null) {
                accountDatabase.removeLogin(param[0].getId());
            }
            // get registered users
            result = accountDatabase.getLogins();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return result;
    }


    @Override
    protected void onPostExecute(List<Account> accounts) {
        AccountFragment fragment = weakRef.get();
        if (fragment != null) {
            if (accounts != null) {
                fragment.onSuccess(accounts);
            } else {
                fragment.onError();
            }
        }
    }
}