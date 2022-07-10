package org.nuclearfog.twidda.ui.fragments;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.backend.async.ListManager.Action.DEL_USER;
import static org.nuclearfog.twidda.backend.async.UserLoader.NO_CURSOR;
import static org.nuclearfog.twidda.ui.activities.UserProfile.KEY_PROFILE_DATA;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.adapter.UserAdapter.UserClickListener;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.async.ListManager;
import org.nuclearfog.twidda.backend.async.ListManager.ListManagerCallback;
import org.nuclearfog.twidda.backend.async.UserLoader;
import org.nuclearfog.twidda.backend.async.UserLoader.Type;
import org.nuclearfog.twidda.backend.lists.Users;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.activities.UserProfile;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.DialogType;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

/**
 * Fragment class for lists a list of users
 *
 * @author nuclearfog
 */
public class UserFragment extends ListFragment implements UserClickListener, OnConfirmListener, ListManagerCallback {

    /**
     * key to set the type of user list to show
     * value type is integer
     */
    public static final String KEY_FRAG_USER_MODE = "user_mode";

    /**
     * key to define search string like username
     * value type is string
     */
    public static final String KEY_FRAG_USER_SEARCH = "user_search";

    /**
     * key to define user, tweet or list ID
     * value type is long
     */
    public static final String KEY_FRAG_USER_ID_ALL = "user_id_all";

    /**
     * key enable function to remove users from list
     * value type is boolean
     */
    public static final String KEY_FRAG_DEL_USER = "user_en_del";

    /**
     * value to configure to show users following the authenticating user
     * corresponding key {@link #KEY_FRAG_USER_MODE}
     */
    public static final int USER_FRAG_FOLLOWER = 0xE45DD2;

    /**
     * value to configure to show users followed by the authenticating user
     * corresponding key {@link #KEY_FRAG_USER_MODE}
     */
    public static final int USER_FRAG_FOLLOWING = 0x64D432EB;

    /**
     * value to configure to show users retweeting a tweet
     * corresponding key {@link #KEY_FRAG_USER_MODE}
     */
    public static final int USER_FRAG_RETWEET = 0x2AC31E6B;

    /**
     * value to configure to show users favoring a tweet
     * corresponding key {@link #KEY_FRAG_USER_MODE}
     */
    public static final int USER_FRAG_FAVORIT = 0xA7FB2BB4;

    /**
     * value to configure to search users matching a search string
     * corresponding key {@link #KEY_FRAG_USER_MODE}
     */
    public static final int USER_FRAG_SEARCH = 0x162C3599;

    /**
     * value to configure to show subscribers of an userlist
     * corresponding key {@link #KEY_FRAG_USER_MODE}
     */
    public static final int USER_FRAG_LIST_SUBSCRIBER = 0x21DCF91C;

    /**
     * value to configure to show members of an userlist
     * corresponding key {@link #KEY_FRAG_USER_MODE}
     */
    public static final int USER_FRAG_LIST_MEMBERS = 0x9A00B3A5;

    /**
     * value to configure a list of blocked users
     * corresponding key {@link #KEY_FRAG_USER_MODE}
     */
    public static final int USER_FRAG_BLOCKED_USERS = 0x83D186AD;

    /**
     * value to configure a list of muted users
     * corresponding key {@link #KEY_FRAG_USER_MODE}
     */
    public static final int USER_FRAG_MUTED_USERS = 0x5246DC35;

    /**
     * value to configure a list of users with incoming following request
     * corresponding key {@link #KEY_FRAG_USER_MODE}
     */
    public static final int USER_FRAG_FOLLOW_INCOMING = 0x89e5255a;

    /**
     * value to configure a list of users with outgoing following request
     * corresponding key {@link #KEY_FRAG_USER_MODE}
     */
    public static final int USER_FRAG_FOLLOW_OUTGOING = 0x72544f17;

    /**
     * Request code to update user information
     */
    private static final int REQ_USER_UPDATE = 0x3F29;

    private UserLoader userTask;
    private ListManager listTask;

    private ConfirmDialog confirmDialog;
    private UserAdapter adapter;

    private String deleteUserName = "";
    private String search = "";
    private long id = 0;
    private int mode = 0;
    private boolean delUser = false;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle param = getArguments();
        if (param != null) {
            mode = param.getInt(KEY_FRAG_USER_MODE, 0);
            id = param.getLong(KEY_FRAG_USER_ID_ALL, 0);
            search = param.getString(KEY_FRAG_USER_SEARCH, "");
            delUser = param.getBoolean(KEY_FRAG_DEL_USER, false);
        }
        confirmDialog = new ConfirmDialog(requireContext());
        adapter = new UserAdapter(requireContext(), this);
        adapter.enableDeleteButton(delUser);
        setAdapter(adapter);

        confirmDialog.setConfirmListener(this);
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
        if (requestCode == REQ_USER_UPDATE && resultCode == UserProfile.RETURN_USER_UPDATED && data != null) {
            Object result = data.getSerializableExtra(UserProfile.KEY_USER_UPDATE);
            if (result instanceof User) {
                User update = (User) result;
                adapter.updateUser(update);
            }
        }
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
        if (!confirmDialog.isShowing()) {
            deleteUserName = name;
            confirmDialog.show(DialogType.LIST_REMOVE_USER);
        }
    }


    @Override
    public void onConfirm(DialogType type, boolean rememberChoice) {
        if (type == DialogType.LIST_REMOVE_USER) {
            if (listTask == null || listTask.getStatus() != RUNNING) {
                listTask = new ListManager(requireContext(), id, DEL_USER, deleteUserName, this);
                listTask.execute();
            }
        }
    }


    @Override
    public void onSuccess(String name) {
        if (name.startsWith("@"))
            name = name.substring(1);
        String info = getString(R.string.info_user_removed, name);
        Toast.makeText(requireContext(), info, Toast.LENGTH_SHORT).show();
        adapter.removeUser(name);
    }


    @Override
    public void onFailure(@Nullable ErrorHandler.TwitterError err) {
        ErrorHandler.handleFailure(requireContext(), err);
    }

    /**
     * set List data
     *
     * @param data list of twitter users
     */
    public void setData(Users data) {
        adapter.setData(data);
        setRefresh(false);
    }

    /**
     * called when an error occurs
     *
     * @param exception Twitter exception
     */
    public void onError(TwitterException exception) {
        ErrorHandler.handleFailure(requireContext(), exception);
        adapter.disableLoading();
        setRefresh(false);
    }


    /**
     * load content into the list
     *
     * @param cursor cursor of the list or {@link UserLoader#NO_CURSOR} if there is none
     */
    private void load(long cursor) {
        switch (mode) {
            case USER_FRAG_FOLLOWER:
                userTask = new UserLoader(this, Type.FOLLOWS, id, search);
                userTask.execute(cursor);
                break;

            case USER_FRAG_FOLLOWING:
                userTask = new UserLoader(this, Type.FRIENDS, id, search);
                userTask.execute(cursor);
                break;

            case USER_FRAG_RETWEET:
                userTask = new UserLoader(this, Type.RETWEET, id, search);
                userTask.execute(cursor);
                break;

            case USER_FRAG_FAVORIT:
                userTask = new UserLoader(this, Type.FAVORIT, id, search);
                userTask.execute(cursor);
                break;

            case USER_FRAG_SEARCH:
                userTask = new UserLoader(this, Type.SEARCH, id, search);
                userTask.execute(cursor);
                break;

            case USER_FRAG_LIST_SUBSCRIBER:
                userTask = new UserLoader(this, Type.SUBSCRIBER, id, search);
                userTask.execute(cursor);
                break;

            case USER_FRAG_LIST_MEMBERS:
                userTask = new UserLoader(this, Type.LISTMEMBER, id, search);
                userTask.execute(cursor);
                break;

            case USER_FRAG_BLOCKED_USERS:
                userTask = new UserLoader(this, Type.BLOCK, id, search);
                userTask.execute(cursor);
                break;

            case USER_FRAG_MUTED_USERS:
                userTask = new UserLoader(this, Type.MUTE, id, search);
                userTask.execute(cursor);
                break;

            case USER_FRAG_FOLLOW_OUTGOING:
                userTask = new UserLoader(this, Type.FOLLOWER_REQ, id, search);
                userTask.execute(cursor);
                break;

            case USER_FRAG_FOLLOW_INCOMING:
                userTask = new UserLoader(this, Type.FOLLOWING_REQ, id, search);
                userTask.execute(cursor);
                break;
        }
        if (cursor == NO_CURSOR) {
            setRefresh(true);
        }
    }
}