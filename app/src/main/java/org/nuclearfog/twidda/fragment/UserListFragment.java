package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.backend.UserLoader;
import org.nuclearfog.twidda.fragment.backend.UserLoader.Mode;
import org.nuclearfog.twidda.window.UserProfile;


public class UserListFragment extends Fragment implements OnRefreshListener, OnItemClickListener {

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
    private View root;
    private UserType mode;
    private String search;
    private long id;
    private boolean fixLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        Bundle b = getArguments();
        if (b != null && b.containsKey("mode")) {
            mode = (UserType) b.getSerializable("mode");
            id = b.getLong("id", -1);
            search = b.getString("search", "");
            fixLayout = b.getBoolean("fix", true);
        } else if (BuildConfig.DEBUG) {
            throw new AssertionError("Bundle error!");
        }
        GlobalSettings settings = GlobalSettings.getInstance(getContext());

        View v = inflater.inflate(R.layout.fragment_list, parent, false);
        RecyclerView list = v.findViewById(R.id.fragment_list);
        reload = v.findViewById(R.id.fragment_reload);

        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        reload.setOnRefreshListener(this);
        adapter = new UserAdapter(this);
        adapter.setColor(settings.getFontColor());
        adapter.toggleImage(settings.getImageLoad());
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setHasFixedSize(fixLayout);
        list.setAdapter(adapter);
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View v, Bundle param) {
        root = v;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (userTask == null) {
            load();
        }
    }


    @Override
    public void onStop() {
        if (userTask != null && userTask.getStatus() == Status.RUNNING)
            userTask.cancel(true);
        super.onStop();
    }


    @Override
    public void onRefresh() {
        load();
    }


    @Override
    public void onItemClick(int pos) {
        if (!reload.isRefreshing()) {
            TwitterUser user = adapter.getData(pos);
            long userID = user.getId();
            String username = user.getScreenname();
            Intent intent = new Intent(getContext(), UserProfile.class);
            intent.putExtra("userID", userID);
            intent.putExtra("username", username);
            startActivity(intent);
        }
    }


    private void load() {
        switch (mode) {
            case FOLLOWS:
                userTask = new UserLoader(root, Mode.FOLLOWS);
                userTask.execute(id);
                break;
            case FRIENDS:
                userTask = new UserLoader(root, Mode.FRIENDS);
                userTask.execute(id);
                break;
            case RETWEET:
                userTask = new UserLoader(root, Mode.RETWEET);
                userTask.execute(id);
                break;
            case FAVORIT:
                userTask = new UserLoader(root, Mode.FAVORIT);
                userTask.execute(id);
                break;
            case USEARCH:
                userTask = new UserLoader(root, Mode.SEARCH);
                userTask.execute(search);
                break;
            default:
                if (BuildConfig.DEBUG)
                    throw new AssertionError("mode failure");
                break;
        }
    }
}