package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.ListDetail;
import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.adapter.ListAdapter;
import org.nuclearfog.twidda.adapter.ListAdapter.ListClickListener;
import org.nuclearfog.twidda.backend.TwitterListLoader;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.holder.UserListList;
import org.nuclearfog.twidda.backend.items.TwitterList;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.ListDetail.KEY_CURRENT_USER_OWNS;
import static org.nuclearfog.twidda.activity.ListDetail.KEY_LISTDETAIL_ID;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_ID;
import static org.nuclearfog.twidda.backend.TwitterListLoader.Action.LOAD_MEMBERSHIPS;
import static org.nuclearfog.twidda.backend.TwitterListLoader.Action.LOAD_USERLISTS;
import static org.nuclearfog.twidda.backend.TwitterListLoader.NO_CURSOR;

/**
 * Fragment class for user lists
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
     * setup the list to show all userlists owned by a specified user
     */
    public static final int LIST_USER_OWNS = 1;

    /**
     * setup the list to show all userlists the specified user is added to
     */
    public static final int LIST_USER_SUBSCR_TO = 2;

    /**
     * request code to open an user list to check for changes
     */
    public static final int REQUEST_OPEN_LIST = 3;

    /**
     * activity result key to return the ID of a removed list
     * called with {@link #RETURN_LIST_REMOVED}
     */
    public static final String RESULT_REMOVED_LIST_ID = "removed-list-id";

    /**
     * return code for {@link #REQUEST_OPEN_LIST} when an userlist was deleted
     */
    public static final int RETURN_LIST_REMOVED = 4;

    private TwitterListLoader listTask;
    private GlobalSettings settings;
    private ListAdapter adapter;


    @Override
    protected void onCreate() {
        settings = GlobalSettings.getInstance(requireContext());
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
        setRefresh(true);
        load(NO_CURSOR);
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
        if (requestCode == REQUEST_OPEN_LIST && resultCode == RETURN_LIST_REMOVED && data != null) {
            long removedListId = data.getLongExtra(RESULT_REMOVED_LIST_ID, 0);
            adapter.removeItem(removedListId);
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
        listIntent.putExtra(KEY_LISTDETAIL_ID, listItem.getId());
        listIntent.putExtra(KEY_CURRENT_USER_OWNS, listItem.isListOwner());
        startActivityForResult(listIntent, REQUEST_OPEN_LIST);
    }


    @Override
    public void onProfileClick(TwitterUser user) {
        Intent profile = new Intent(requireContext(), UserProfile.class);
        profile.putExtra(KEY_PROFILE_ID, user.getId());
        startActivity(profile);
    }


    @Override
    public void onFooterClick(long cursor) {
        if (listTask != null && listTask.getStatus() != RUNNING) {
            load(cursor);
        }
    }


    @Override
    protected ListAdapter initAdapter() {
        adapter = new ListAdapter(this, settings);
        return adapter;
    }

    /**
     * set data to list
     *
     * @param data List of Twitter list data
     */
    public void setData(UserListList data) {
        adapter.setData(data);
        setRefresh(false);
    }

    /**
     * update item in list
     *
     * @param item Twitter list item
     */
    public void updateItem(TwitterList item) {
        adapter.updateItem(item);
    }

    /**
     * remove item with specific ID
     *
     * @param list Twitter list item
     */
    public void removeItem(TwitterList list) {
        adapter.removeItem(list.getId());
    }

    /**
     * called from {@link TwitterListLoader} if an error occurs
     *
     * @param error Twitter exception
     */
    public void onError(@Nullable EngineException error) {
        if (error != null)
            ErrorHandler.handleFailure(requireContext(), error);
        adapter.disableLoading();
        setRefresh(false);
    }

    /**
     * load content into the list
     */
    private void load(long cursor) {
        Bundle param = getArguments();
        if (param != null) {
            long id = param.getLong(KEY_FRAG_LIST_OWNER_ID, -1);
            String ownerName = param.getString(KEY_FRAG_LIST_OWNER_NAME, "");
            int type = param.getInt(KEY_FRAG_LIST_LIST_TYPE);
            if (type == LIST_USER_OWNS) {
                listTask = new TwitterListLoader(this, LOAD_USERLISTS, id, ownerName);
                listTask.execute(cursor);
            } else if (type == LIST_USER_SUBSCR_TO) {
                listTask = new TwitterListLoader(this, LOAD_MEMBERSHIPS, id, ownerName);
                listTask.execute(cursor);
            }
        }
    }
}