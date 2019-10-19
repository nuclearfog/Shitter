package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter.FragmentChangeObserver;
import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.backend.UserLoader;
import org.nuclearfog.twidda.fragment.backend.UserLoader.Mode;
import org.nuclearfog.twidda.window.UserProfile;

import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.window.UserProfile.KEY_PROFILE_ID;


public class UserListFragment extends Fragment implements OnRefreshListener, OnItemClickListener, FragmentChangeObserver {

    public static final String KEY_FRAG_USER_MODE = "mode";
    public static final String KEY_FRAG_USER_SEARCH = "search";
    public static final String KEY_FRAG_USER_ID = "ID";
    public static final String KEY_FRAG_USER_FIX = "fix";

    public enum UserType {
        FOLLOWS,
        FRIENDS,
        RETWEET,
        FAVORIT,
        USEARCH
    }

    private SwipeRefreshLayout reload;
    private UserAdapter adapter;
    private UserLoader userTask;
    private RecyclerView list;
    private UserType mode;
    private String search;
    private long id;
    private boolean fixLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        super.onCreateView(inflater, parent, param);
        Bundle b = getArguments();
        if (b != null && b.containsKey(KEY_FRAG_USER_MODE)) {
            mode = (UserType) b.getSerializable(KEY_FRAG_USER_MODE);
            id = b.getLong(KEY_FRAG_USER_ID, -1);
            search = b.getString(KEY_FRAG_USER_SEARCH, "");
            fixLayout = b.getBoolean(KEY_FRAG_USER_FIX, true);
        } else if (BuildConfig.DEBUG) {
            throw new AssertionError("Bundle error!");
        }

        View v = inflater.inflate(R.layout.fragment_list, parent, false);
        list = v.findViewById(R.id.fragment_list);
        reload = v.findViewById(R.id.fragment_reload);

        GlobalSettings settings = GlobalSettings.getInstance(getContext());
        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        reload.setOnRefreshListener(this);
        adapter = new UserAdapter(this);
        adapter.setColor(settings.getFontColor());
        adapter.setImage(settings.getImageLoad());
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setHasFixedSize(fixLayout);
        list.setAdapter(adapter);

        return v;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (userTask == null)
            load();
    }


    @Override
    public void onStop() {
        if (userTask != null && userTask.getStatus() == RUNNING)
            userTask.cancel(true);
        super.onStop();
    }


    @Override
    public void onRefresh() {
        if (userTask != null && userTask.getStatus() != RUNNING)
            load();
    }


    @Override
    public void onItemClick(int pos) {
        if (reload != null && !reload.isRefreshing()) {
            TwitterUser user = adapter.getData(pos);
            Intent intent = new Intent(getContext(), UserProfile.class);
            intent.putExtra(KEY_PROFILE_ID, user.getId());
            startActivity(intent);
        }
    }


    @Override
    public void onTabChange() {
        if (list != null)
            list.smoothScrollToPosition(0);
    }


    @Override
    public void onSettingsChange() {
    }


    @Override
    public void onDataClear() {
    }


    public UserAdapter getAdapter() {
        return adapter;
    }


    public void setRefresh(boolean enable) {
        if (enable) {
            reload.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (userTask.getStatus() != FINISHED)
                        reload.setRefreshing(true);
                }
            }, 500);
        } else {
            reload.setRefreshing(false);
        }
    }


    private void load() {
        switch (mode) {
            case FOLLOWS:
                userTask = new UserLoader(this, Mode.FOLLOWS);
                userTask.execute(id);
                break;
            case FRIENDS:
                userTask = new UserLoader(this, Mode.FRIENDS);
                userTask.execute(id);
                break;
            case RETWEET:
                userTask = new UserLoader(this, Mode.RETWEET);
                userTask.execute(id);
                break;
            case FAVORIT:
                userTask = new UserLoader(this, Mode.FAVORIT);
                userTask.execute(id);
                break;
            case USEARCH:
                userTask = new UserLoader(this, Mode.SEARCH);
                userTask.execute(search);
                break;
        }
    }
}