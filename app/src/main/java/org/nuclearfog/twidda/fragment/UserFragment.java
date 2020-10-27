package org.nuclearfog.twidda.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.adapter.FragmentAdapter.FragmentChangeObserver;
import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.adapter.UserAdapter.UserClickListener;
import org.nuclearfog.twidda.backend.UserListLoader;
import org.nuclearfog.twidda.backend.UserListLoader.Action;
import org.nuclearfog.twidda.backend.UserListManager;
import org.nuclearfog.twidda.backend.UserListManager.ListManagerCallback;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.holder.TwitterUserList;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.utils.DialogBuilder;
import org.nuclearfog.twidda.backend.utils.DialogBuilder.OnDialogClick;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_ID;
import static org.nuclearfog.twidda.backend.UserListLoader.NO_CURSOR;
import static org.nuclearfog.twidda.backend.UserListManager.Action.DEL_USER;
import static org.nuclearfog.twidda.backend.utils.DialogBuilder.DialogType.DEL_USER_LIST;

/**
 * Fragment class for lists a list of users
 */
public class UserFragment extends Fragment implements OnRefreshListener, UserClickListener,
        FragmentChangeObserver, OnDialogClick, ListManagerCallback {

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

    public static final int USER_FRAG_FOLLOWS = 1;
    public static final int USER_FRAG_FRIENDS = 2;
    public static final int USER_FRAG_RETWEET = 3;
    public static final int USER_FRAG_FAVORIT = 4;
    public static final int USER_FRAG_SEARCH = 5;
    public static final int USER_FRAG_SUBSCR = 6;
    public static final int USER_FRAG_LISTS = 7;

    private UserListLoader userTask;
    private UserListManager listTask;

    private SwipeRefreshLayout reload;
    private RecyclerView list;
    private Dialog deleteDialog;
    private UserAdapter adapter;

    private String deleteUserName = "";
    private String search = "";
    private long id = 0;
    private int mode = 0;
    private boolean delUser = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle b) {
        Context context = inflater.getContext();
        GlobalSettings settings = GlobalSettings.getInstance(context);

        adapter = new UserAdapter(this, settings);
        list = new RecyclerView(context);
        list.setLayoutManager(new LinearLayoutManager(context));
        list.setHasFixedSize(true);
        list.setAdapter(adapter);

        Bundle param = getArguments();
        if (param != null) {
            mode = param.getInt(KEY_FRAG_USER_MODE, 0);
            id = param.getLong(KEY_FRAG_USER_ID, 0);
            search = param.getString(KEY_FRAG_USER_SEARCH, "");
            delUser = param.getBoolean(KEY_FRAG_DEL_USER, false);
        }
        deleteDialog = DialogBuilder.create(requireContext(), DEL_USER_LIST, this);
        reload = new SwipeRefreshLayout(context);
        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        reload.setOnRefreshListener(this);
        reload.addView(list);
        return reload;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (userTask == null) {
            load(NO_CURSOR);
        }
    }


    @Override
    public void onDestroy() {
        if (userTask != null && userTask.getStatus() == RUNNING)
            userTask.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onRefresh() {
        if (userTask != null && userTask.getStatus() != RUNNING) {
            load(NO_CURSOR);
        }
    }


    @Override
    public void onUserClick(TwitterUser user) {
        if (getContext() != null && !reload.isRefreshing()) {
            Intent intent = new Intent(getContext(), UserProfile.class);
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
    public void onTabChange() {
        if (list != null) {
            list.smoothScrollToPosition(0);
        }
    }


    @Override
    public void onReset() {
        if (list != null) {
            list.setAdapter(adapter);
            load(NO_CURSOR);
        }
    }


    @Override
    public void onConfirm(DialogBuilder.DialogType type) {
        if (type == DEL_USER_LIST) {
            if (listTask == null || listTask.getStatus() != RUNNING) {
                listTask = new UserListManager(id, DEL_USER, requireContext(), this);
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
        if (err != null) {
            ErrorHandler.handleFailure(requireContext(), err);
        }
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
        if (getContext() != null && error != null) {
            ErrorHandler.handleFailure(getContext(), error);
        }
        adapter.disableLoading();
        setRefresh(false);
    }

    /**
     * enables or disables swiperefresh
     *
     * @param enable true to enable RefreshLayout with delay
     */
    private void setRefresh(boolean enable) {
        if (enable) {
            reload.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (userTask != null && userTask.getStatus() != FINISHED
                            && !reload.isRefreshing()) {
                        reload.setRefreshing(true);
                    }
                }
            }, 500);

        } else {
            reload.setRefreshing(false);
        }
    }

    /**
     * load content into the list
     *
     * @param cursor cursor of the list or {@link UserListLoader#NO_CURSOR} if there is none
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
                action = Action.LIST;
                break;
        }
        adapter.enableDeleteButton(delUser);
        userTask = new UserListLoader(this, action, id, search);
        userTask.execute(cursor);
        if (cursor == NO_CURSOR) {
            setRefresh(true);
        }
    }
}