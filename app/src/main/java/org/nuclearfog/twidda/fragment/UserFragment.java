package org.nuclearfog.twidda.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.adapter.FragmentAdapter.FragmentChangeObserver;
import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.adapter.UserAdapter.UserClickListener;
import org.nuclearfog.twidda.backend.UserListLoader;
import org.nuclearfog.twidda.backend.UserListLoader.Action;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.holder.UserListHolder;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.tools.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_ID;
import static org.nuclearfog.twidda.backend.UserListLoader.NO_CURSOR;

/**
 * Fragment class for lists a list of users
 */
public class UserFragment extends Fragment implements OnRefreshListener, UserClickListener, FragmentChangeObserver {

    public static final String KEY_FRAG_USER_MODE = "user_mode";
    public static final String KEY_FRAG_USER_SEARCH = "user_search";
    public static final String KEY_FRAG_USER_ID = "user_id";

    public static final int USER_FRAG_FOLLOWS = 1;
    public static final int USER_FRAG_FRIENDS = 2;
    public static final int USER_FRAG_RETWEET = 3;
    public static final int USER_FRAG_FAVORIT = 4;
    public static final int USER_FRAG_SEARCH = 5;
    public static final int USER_FRAG_SUBSCR = 6;
    public static final int USER_FRAG_LISTS = 7;

    private SwipeRefreshLayout reload;
    private UserAdapter adapter;
    private UserListLoader userTask;
    private RecyclerView list;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle param) {
        Context context = inflater.getContext();
        GlobalSettings settings = GlobalSettings.getInstance(context);

        adapter = new UserAdapter(this, settings);
        list = new RecyclerView(context);
        list.setLayoutManager(new LinearLayoutManager(context));
        list.setHasFixedSize(true);
        list.setAdapter(adapter);

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
    public void onTabChange() {
        if (list != null) {
            list.smoothScrollToPosition(0);
        }
    }


    @Override
    public void onReset() {
    }

    /**
     * set List data
     *
     * @param data list of twitter users
     */
    public void setData(UserListHolder data) {
        adapter.setData(data);
    }

    /**
     * called from {@link UserListLoader} to enable or disable RefreshLayout
     * @param enable true to enable RefreshLayout with delay
     */
    public void setRefresh(boolean enable) {
        if (enable) {
            reload.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (userTask.getStatus() != FINISHED && !reload.isRefreshing())
                        reload.setRefreshing(true);
                }
            }, 500);
        } else {
            reload.setRefreshing(false);
        }
    }

    /**
     * called when an error occurs
     *
     * @param error Engine exception
     */
    public void onError(EngineException error) {
        if (getContext() != null) {
            ErrorHandler.handleFailure(getContext(), error);
        }
        adapter.disableLoading();
    }

    /**
     * load content into the list
     *
     * @param cursor cursor of the list or {@link UserListLoader#NO_CURSOR} if there is none
     */
    private void load(long cursor) {
        Bundle param = getArguments();
        if (param != null) {
            int mode = param.getInt(KEY_FRAG_USER_MODE, 0);
            long id = param.getLong(KEY_FRAG_USER_ID, 1);
            String search = param.getString(KEY_FRAG_USER_SEARCH, "");
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
            userTask = new UserListLoader(this, action, id, cursor, search);
            userTask.execute();
        }
    }
}