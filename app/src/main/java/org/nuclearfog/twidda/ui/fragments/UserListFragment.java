package org.nuclearfog.twidda.ui.fragments;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.backend.async.ListLoader.NO_CURSOR;
import static org.nuclearfog.twidda.ui.activities.UserProfile.KEY_PROFILE_DATA;
import static org.nuclearfog.twidda.ui.activities.UserlistActivity.KEY_LIST_DATA;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.adapter.UserlistAdapter;
import org.nuclearfog.twidda.adapter.UserlistAdapter.ListClickListener;
import org.nuclearfog.twidda.backend.async.ListLoader;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.activities.UserProfile;
import org.nuclearfog.twidda.ui.activities.UserlistActivity;

/**
 * Fragment class for user lists
 *
 * @author nuclearfog
 */
public class UserListFragment extends ListFragment implements ListClickListener {

    /**
     * Key for the owner ID
     */
    public static final String KEY_FRAG_LIST_OWNER_ID = "list_owner_id";

    /**
     * alternative key for the owner name
     */
    public static final String KEY_FRAG_LIST_OWNER_NAME = "list_owner_name";

    /**
     * key to define the type of the list
     * {@link #LIST_USER_OWNS} or {@link #LIST_USER_SUBSCR_TO}
     */
    public static final String KEY_FRAG_LIST_LIST_TYPE = "list_type";

    /**
     * setup the list to show all user lists owned by a specified user
     */
    public static final int LIST_USER_OWNS = 0x5F36F90D;

    /**
     * setup the list to show all user lists the specified user is added to
     */
    public static final int LIST_USER_SUBSCR_TO = 0xAA7386AA;

    /**
     * request code to open an user list to check for changes
     */
    public static final int REQUEST_OPEN_LIST = 0x9541;

    private ListLoader listTask;
    private UserlistAdapter adapter;

    private String ownerName = "";
    private long id = 0;
    private int type = 0;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle param = getArguments();
        if (param != null) {
            id = param.getLong(KEY_FRAG_LIST_OWNER_ID, -1);
            ownerName = param.getString(KEY_FRAG_LIST_OWNER_NAME, "");
            type = param.getInt(KEY_FRAG_LIST_LIST_TYPE);
        }
        adapter = new UserlistAdapter(requireContext(), this);
        setAdapter(adapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (listTask == null) {
            setRefresh(true);
            load(NO_CURSOR);
        }
    }


    @Override
    protected void onReset() {
        load(NO_CURSOR);
        setRefresh(true);
    }


    @Override
    public void onDestroy() {
        if (listTask != null && listTask.getStatus() == RUNNING)
            listTask.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == REQUEST_OPEN_LIST) {
            // check if userlist was removed
            if (resultCode == UserlistActivity.RETURN_LIST_REMOVED) {
                long removedListId = data.getLongExtra(UserlistActivity.RESULT_REMOVED_LIST_ID, 0);
                adapter.removeItem(removedListId);
            }
            // check if userlist was updated
            else if (resultCode == UserlistActivity.RETURN_LIST_UPDATED) {
                Object result = data.getSerializableExtra(UserlistActivity.RESULT_UPDATE_LIST);
                if (result instanceof UserList) {
                    UserList update = (UserList) result;
                    adapter.updateItem(update);
                }
            }
        }
    }


    @Override
    protected void onReload() {
        if (listTask != null && listTask.getStatus() != RUNNING) {
            load(NO_CURSOR);
        }
    }


    @Override
    public void onListClick(UserList listItem) {
        Intent listIntent = new Intent(requireContext(), UserlistActivity.class);
        listIntent.putExtra(KEY_LIST_DATA, listItem);
        startActivityForResult(listIntent, REQUEST_OPEN_LIST);
    }


    @Override
    public void onProfileClick(User user) {
        Intent profile = new Intent(requireContext(), UserProfile.class);
        profile.putExtra(KEY_PROFILE_DATA, user);
        startActivity(profile);
    }


    @Override
    public boolean onFooterClick(long cursor) {
        if (listTask != null && listTask.getStatus() != RUNNING) {
            load(cursor);
            return true;
        }
        return false;
    }

    /**
     * set data to list
     *
     * @param data List of Twitter list data
     */
    public void setData(UserLists data) {
        adapter.setData(data);
        setRefresh(false);
    }

    /**
     * called from {@link ListLoader} if an error occurs
     *
     * @param error Twitter exception
     */
    public void onError(@Nullable ErrorHandler.TwitterError error) {
        ErrorHandler.handleFailure(requireContext(), error);
        adapter.disableLoading();
        setRefresh(false);
    }

    /**
     * load content into the list
     */
    private void load(long cursor) {
        if (type == LIST_USER_OWNS) {
            listTask = new ListLoader(this, ListLoader.LOAD_USERLISTS, id, ownerName);
            listTask.execute(cursor);
        } else if (type == LIST_USER_SUBSCR_TO) {
            listTask = new ListLoader(this, ListLoader.LOAD_MEMBERSHIPS, id, ownerName);
            listTask.execute(cursor);
        }
    }
}