package org.nuclearfog.twidda.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.adapter.UserAdapter.UserClickListener;
import org.nuclearfog.twidda.backend.ListManager;
import org.nuclearfog.twidda.backend.ListManager.ListManagerCallback;
import org.nuclearfog.twidda.backend.UserLoader;
import org.nuclearfog.twidda.backend.UserLoader.Type;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.lists.UserList;
import org.nuclearfog.twidda.backend.model.User;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.dialog.ConfirmDialog;
import org.nuclearfog.twidda.dialog.ConfirmDialog.DialogType;
import org.nuclearfog.twidda.dialog.ConfirmDialog.OnConfirmListener;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_DATA;
import static org.nuclearfog.twidda.backend.ListManager.Action.DEL_USER;
import static org.nuclearfog.twidda.backend.UserLoader.NO_CURSOR;

/**
 * Fragment class for lists a list of users
 *
 * @author nuclearfog
 */
public class UserFragment extends ListFragment implements UserClickListener,
        OnConfirmListener, ListManagerCallback {

    /**
     * key to set the type of user list to show
     */
    public static final String KEY_FRAG_USER_MODE = "user_mode";

    /**
     * key to define search string like username
     */
    public static final String KEY_FRAG_USER_SEARCH = "user_search";

    /**
     * key to define user, tweet or list ID
     */
    public static final String KEY_FRAG_USER_ID = "user_id";

    /**
     * key to enable function to remove users from list
     */
    public static final String KEY_FRAG_DEL_USER = "user_en_del";

    /**
     * key to send updated user data
     */
    public static final String KEY_USER_UPDATE = "user_update";

    /**
     * configuration for a list of users following the specified user
     */
    public static final int USER_FRAG_FOLLOWS = 0xE45DD2;

    /**
     * configuration for a list of users followed by the specified user
     */
    public static final int USER_FRAG_FRIENDS = 0x64D432EB;

    /**
     * configuration to get a list of users retweeting a tweet
     */
    public static final int USER_FRAG_RETWEET = 0x2AC31E6B;

    /**
     * configuration to get a list of users favoring a tweet
     * todo implement this function if there is an API for it
     */
    public static final int USER_FRAG_FAVORIT = 0xA7FB2BB4;

    /**
     * configuration for a list of searched users
     */
    public static final int USER_FRAG_SEARCH = 0x162C3599;

    /**
     * configuration for a list of userlist subscriber
     */
    public static final int USER_FRAG_SUBSCR = 0x21DCF91C;

    /**
     * configuration for a list of users added to a list
     */
    public static final int USER_FRAG_LISTS = 0x9A00B3A5;

    /**
     * Request code to update user information
     */
    private static final int REQ_USER_UPDATE = 0x3F29;

    /**
     * Return code to update user information
     */
    public static final int RETURN_USER_UPDATED = 0x9996498C;

    private UserLoader userTask;
    private ListManager listTask;

    private Dialog deleteDialog;
    private UserAdapter adapter;

    private String deleteUserName = "";
    private String search = "";
    private long id = 0;
    private int mode = 0;
    private boolean delUser = false;


    @Override
    protected void onCreate() {
        Bundle param = getArguments();
        if (param != null) {
            mode = param.getInt(KEY_FRAG_USER_MODE, 0);
            id = param.getLong(KEY_FRAG_USER_ID, 0);
            search = param.getString(KEY_FRAG_USER_SEARCH, "");
            delUser = param.getBoolean(KEY_FRAG_DEL_USER, false);
        }
        deleteDialog = new ConfirmDialog(requireContext(), DialogType.LIST_REMOVE_USER, this);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (userTask == null) {
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
        if (userTask != null && userTask.getStatus() == RUNNING)
            userTask.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_USER_UPDATE && resultCode == RETURN_USER_UPDATED && data != null) {
            Object result = data.getSerializableExtra(KEY_USER_UPDATE);
            if (result instanceof User) {
                User update = (User) result;
                adapter.updateUser(update);
            }
        }
    }


    @Override
    protected UserAdapter initAdapter() {
        adapter = new UserAdapter(settings, this);
        adapter.enableDeleteButton(delUser);
        return adapter;
    }


    @Override
    protected void onReload() {
        if (userTask != null && userTask.getStatus() != RUNNING) {
            load(NO_CURSOR);
        }
    }


    @Override
    public void onUserClick(User user) {
        if (!isRefreshing()) {
            Intent intent = new Intent(requireContext(), UserProfile.class);
            intent.putExtra(KEY_PROFILE_DATA, user);
            startActivityForResult(intent, REQ_USER_UPDATE);
        }
    }


    @Override
    public boolean onFooterClick(long cursor) {
        if (userTask != null && userTask.getStatus() != RUNNING) {
            load(cursor);
            return true;
        }
        return false;
    }


    @Override
    public void onDelete(String name) {
        deleteUserName = name;
        if (!deleteDialog.isShowing()) {
            deleteDialog.show();
        }
    }


    @Override
    public void onConfirm(DialogType type) {
        if (type == DialogType.LIST_REMOVE_USER) {
            if (listTask == null || listTask.getStatus() != RUNNING) {
                listTask = new ListManager(id, DEL_USER, requireContext(), this);
                listTask.execute(deleteUserName);
            }
        }
    }


    @Override
    public void onSuccess(String[] names) {
        Toast.makeText(requireContext(), R.string.info_user_removed, Toast.LENGTH_SHORT).show();
        adapter.removeUser(names[0]);
    }


    @Override
    public void onFailure(@Nullable EngineException err) {
        ErrorHandler.handleFailure(requireContext(), err);
    }

    /**
     * set List data
     *
     * @param data list of twitter users
     */
    public void setData(UserList data) {
        adapter.setData(data);
        setRefresh(false);
    }

    /**
     * called when an error occurs
     *
     * @param error Engine exception
     */
    public void onError(EngineException error) {
        ErrorHandler.handleFailure(requireContext(), error);
        adapter.disableLoading();
        setRefresh(false);
    }


    /**
     * load content into the list
     *
     * @param cursor cursor of the list or {@link UserLoader#NO_CURSOR} if there is none
     */
    private void load(long cursor) {
        Type listType = Type.NONE;
        switch (mode) {
            case USER_FRAG_FOLLOWS:
                listType = Type.FOLLOWS;
                break;

            case USER_FRAG_FRIENDS:
                listType = UserLoader.Type.FRIENDS;
                break;

            case USER_FRAG_RETWEET:
                listType = Type.RETWEET;
                break;

            case USER_FRAG_FAVORIT:
                listType = Type.FAVORIT;
                break;

            case USER_FRAG_SEARCH:
                listType = Type.SEARCH;
                break;

            case USER_FRAG_SUBSCR:
                listType = Type.SUBSCRIBER;
                break;

            case USER_FRAG_LISTS:
                listType = Type.LISTMEMBER;
                break;
        }
        userTask = new UserLoader(this, listType, id, search);
        userTask.execute(cursor);
        if (cursor == NO_CURSOR) {
            setRefresh(true);
        }
    }
}