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
import org.nuclearfog.twidda.backend.UserLoader.Action;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.holder.TwitterUserList;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.utils.DialogBuilder;
import org.nuclearfog.twidda.backend.utils.DialogBuilder.OnDialogClick;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_ID;
import static org.nuclearfog.twidda.backend.ListManager.Action.DEL_USER;
import static org.nuclearfog.twidda.backend.UserLoader.NO_CURSOR;
import static org.nuclearfog.twidda.backend.utils.DialogBuilder.DialogType.DEL_USER_LIST;

/**
 * Fragment class for lists a list of users
 */
public class UserFragment extends ListFragment implements UserClickListener,
        OnDialogClick, ListManagerCallback {

    /**
     * key to set the type of user list to show
     */
    public static final String KEY_FRAG_USER_MODE = "user_mode";

    /**
     * key to define search string
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
     * configuration for a list of users following the specified user
     */
    public static final int USER_FRAG_FOLLOWS = 1;

    /**
     * configuration for a list of users followed by the specified user
     */
    public static final int USER_FRAG_FRIENDS = 2;

    /**
     * configuration to get a list of users retweeting a tweet
     */
    public static final int USER_FRAG_RETWEET = 3;

    /**
     * configuration to get a list of users favoriting a tweet
     * todo implement this function if there is an API for it
     */
    public static final int USER_FRAG_FAVORIT = 4;

    /**
     * configuration for a list of searched users
     */
    public static final int USER_FRAG_SEARCH = 5;

    /**
     * configuration for a list of userlist subscriber
     */
    public static final int USER_FRAG_SUBSCR = 6;

    /**
     * configuration for a list of users added to a list
     */
    public static final int USER_FRAG_LISTS = 7;

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
    protected void onCreated() {
        Bundle param = getArguments();
        if (param != null) {
            mode = param.getInt(KEY_FRAG_USER_MODE, 0);
            id = param.getLong(KEY_FRAG_USER_ID, 0);
            search = param.getString(KEY_FRAG_USER_SEARCH, "");
            delUser = param.getBoolean(KEY_FRAG_DEL_USER, false);
        }
        deleteDialog = DialogBuilder.create(requireContext(), DEL_USER_LIST, this);
        settings = GlobalSettings.getInstance(requireContext());
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
    protected UserAdapter initAdapter() {
        adapter = new UserAdapter(this, settings);
        return adapter;
    }


    @Override
    protected void onReload() {
        if (userTask != null && userTask.getStatus() != RUNNING) {
            load(NO_CURSOR);
        }
    }


    @Override
    public void onUserClick(TwitterUser user) {
        if (!isRefreshing()) {
            Intent intent = new Intent(requireContext(), UserProfile.class);
            intent.putExtra(KEY_PROFILE_ID, user.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onFooterClick(long cursor) {
        if (userTask != null && userTask.getStatus() != RUNNING) {
            load(cursor);
        }
    }


    @Override
    public void onDelete(String name) {
        deleteUserName = name;
        if (!deleteDialog.isShowing()) {
            deleteDialog.show();
        }
    }


    @Override
    public void onConfirm(DialogBuilder.DialogType type) {
        if (type == DEL_USER_LIST) {
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
    public void onFailure(EngineException err) {
        ErrorHandler.handleFailure(requireContext(), err);
    }

    /**
     * set List data
     *
     * @param data list of twitter users
     */
    public void setData(TwitterUserList data) {
        adapter.setData(data);
        setRefresh(false);
    }

    /**
     * called when an error occurs
     *
     * @param error Engine exception
     */
    public void onError(@Nullable EngineException error) {
        if (error != null) {
            ErrorHandler.handleFailure(requireContext(), error);
        }
        adapter.disableLoading();
        setRefresh(false);
    }


    /**
     * load content into the list
     *
     * @param cursor cursor of the list or {@link UserLoader#NO_CURSOR} if there is none
     */
    private void load(long cursor) {
        Action action = Action.NONE;
        switch (mode) {
            case USER_FRAG_FOLLOWS:
                action = Action.FOLLOWS;
                break;

            case USER_FRAG_FRIENDS:
                action = Action.FRIENDS;
                break;

            case USER_FRAG_RETWEET:
                action = Action.RETWEET;
                break;

            case USER_FRAG_FAVORIT:
                action = Action.FAVORIT;
                break;

            case USER_FRAG_SEARCH:
                action = Action.SEARCH;
                break;

            case USER_FRAG_SUBSCR:
                action = Action.SUBSCRIBER;
                break;

            case USER_FRAG_LISTS:
                action = Action.LISTMEMBER;
                break;
        }
        adapter.enableDeleteButton(delUser);
        userTask = new UserLoader(this, action, id, search);
        userTask.execute(cursor);
        if (cursor == NO_CURSOR) {
            setRefresh(true);
        }
    }
}