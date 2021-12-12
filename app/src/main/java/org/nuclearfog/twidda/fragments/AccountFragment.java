package org.nuclearfog.twidda.fragments;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activities.AccountActivity.RET_ACCOUNT_CHANGE;
import static org.nuclearfog.twidda.dialog.ConfirmDialog.DialogType;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.adapter.AccountAdapter;
import org.nuclearfog.twidda.adapter.AccountAdapter.OnAccountClickListener;
import org.nuclearfog.twidda.backend.AccountLoader;
import org.nuclearfog.twidda.backend.model.Account;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.dialog.ConfirmDialog;
import org.nuclearfog.twidda.dialog.ConfirmDialog.OnConfirmListener;

import java.util.List;

/**
 * fragment class of the {@link org.nuclearfog.twidda.activities.AccountActivity}
 * all registered user accounts are listed here
 *
 * @author nuclearfog
 */
public class AccountFragment extends ListFragment implements OnAccountClickListener, OnConfirmListener {

    @Nullable
    private AccountLoader loginTask;
    private GlobalSettings settings;
    private AccountAdapter adapter;
    private ConfirmDialog dialog;
    private Account selection;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialog = new ConfirmDialog(requireContext(), DialogType.REMOVE_ACCOUNT, this);
        settings = GlobalSettings.getInstance(requireContext());
        adapter = new AccountAdapter(requireContext(), this);
        setAdapter(adapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (loginTask == null) {
            setRefresh(true);
            loginTask = new AccountLoader(this);
            loginTask.execute();
        }
    }


    @Override
    public void onDestroy() {
        if (loginTask != null && loginTask.getStatus() == RUNNING)
            loginTask.cancel(true);
        super.onDestroy();
    }


    @Override
    protected void onReload() {
        if (loginTask == null || loginTask.getStatus() != RUNNING)
            loginTask = new AccountLoader(this);
        loginTask.execute();
    }


    @Override
    protected void onReset() {
        adapter.clear();
        loginTask = new AccountLoader(this);
        loginTask.execute();
        setRefresh(true);
    }


    @Override
    public void onAccountClick(Account account) {
        // set new account
        String[] token = account.getKeys();
        settings.setConnection(token[0], token[1], account.getId());
        // finish activity and return to parent activity
        requireActivity().setResult(RET_ACCOUNT_CHANGE);
        requireActivity().finish();
    }


    @Override
    public void onAccountRemove(Account account) {
        if (!dialog.isShowing()) {
            selection = account;
            dialog.show();
        }
    }


    @Override
    public void onConfirm(DialogType type) {
        loginTask = new AccountLoader(this);
        loginTask.execute(selection);
    }

    /**
     * called from {@link AccountLoader}
     *
     * @param result login information
     */
    public void onSuccess(List<Account> result) {
        adapter.setData(result);
        setRefresh(false);
    }
}