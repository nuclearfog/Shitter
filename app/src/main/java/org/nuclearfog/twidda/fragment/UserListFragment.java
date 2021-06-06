package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.ListDetail;
import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.adapter.ListAdapter;
import org.nuclearfog.twidda.adapter.ListAdapter.ListClickListener;
import org.nuclearfog.twidda.backend.ListLoader;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.backend.model.TwitterList;
import org.nuclearfog.twidda.backend.model.User;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.ListDetail.KEY_LIST_DATA;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_DATA;
import static org.nuclearfog.twidda.backend.ListLoader.NO_CURSOR;
import static org.nuclearfog.twidda.backend.ListLoader.Type.LOAD_MEMBERSHIPS;
import static org.nuclearfog.twidda.backend.ListLoader.Type.LOAD_USERLISTS;

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

    /**
     * return code for {@link #REQUEST_OPEN_LIST} when an user list was deleted
     */
    public static final int RETURN_LIST_REMOVED = 0xDAD518B4;

    /**
     * return code for {@link #REQUEST_OPEN_LIST} when an user list was deleted
     */
    public static final int RETURN_LIST_UPDATED = 0x5D0F5E8D;

    /**
     * activity result key to return the ID of a removed list
     * called with {@link #RETURN_LIST_REMOVED}
     */
    public static final String RESULT_REMOVED_LIST_ID = "removed-list-id";

    /**
     * result key to update an user list
     */
    public static final String RESULT_UPDATE_LIST = "update-user-list";

    private ListLoader listTask;
    private ListAdapter adapter;

    private String ownerName = "";
    private long id = 0;
    private int type = 0;


    @Override
    protected void onCreate() {
        Bundle param = getArguments();
        if (param != null) {
            id = param.getLong(KEY_FRAG_LIST_OWNER_ID, -1);
            ownerName = param.getString(KEY_FRAG_LIST_OWNER_NAME, "");
            type = param.getInt(KEY_FRAG_LIST_LIST_TYPE);
        }
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
            if (resultCode == RETURN_LIST_REMOVED) {
                long removedListId = data.getLongExtra(RESULT_REMOVED_LIST_ID, 0);
                adapter.removeItem(removedListId);
            } else if (resultCode == RETURN_LIST_UPDATED) {
                Object result = data.getSerializableExtra(RESULT_UPDATE_LIST);
                if (result instanceof TwitterList) {
                    TwitterList update = (TwitterList) result;
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
    public void onListClick(TwitterList listItem) {
        Intent listIntent = new Intent(requireContext(), ListDetail.class);
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


    @Override
    protected ListAdapter initAdapter() {
        adapter = new ListAdapter(settings, this);
        return adapter;
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
    public void onError(@Nullable EngineException error) {
        ErrorHandler.handleFailure(requireContext(), error);
        adapter.disableLoading();
        setRefresh(false);
    }

    /**
     * load content into the list
     */
    private void load(long cursor) {
        if (type == LIST_USER_OWNS) {
            listTask = new ListLoader(this, LOAD_USERLISTS, id, ownerName);
            listTask.execute(cursor);
        } else if (type == LIST_USER_SUBSCR_TO) {
            listTask = new ListLoader(this, LOAD_MEMBERSHIPS, id, ownerName);
            listTask.execute(cursor);
        }
    }
}