package org.nuclearfog.twidda.fragment;

import android.content.Context;
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

import org.nuclearfog.twidda.activity.UserDetail;
import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.adapter.ListAdapter;
import org.nuclearfog.twidda.adapter.ListAdapter.ListClickListener;
import org.nuclearfog.twidda.backend.ListLoader;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.UserDetail.KEY_USERDETAIL_ID;
import static org.nuclearfog.twidda.activity.UserDetail.KEY_USERDETAIL_MODE;
import static org.nuclearfog.twidda.activity.UserDetail.UserType.SUBSCRIBER;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_ID;
import static org.nuclearfog.twidda.backend.ListLoader.Action.FOLLOW;
import static org.nuclearfog.twidda.backend.ListLoader.Action.LOAD;

public class ListFragment extends Fragment implements OnRefreshListener, ListClickListener {

    public static final String KEY_FRAG_LIST = "list_owner";

    private SwipeRefreshLayout reloadLayout;
    private ListAdapter adapter;
    private ListLoader listTask;

    private long userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        Bundle b = getArguments();
        if (b != null)
            userId = b.getLong(KEY_FRAG_LIST);

        Context context = inflater.getContext();
        GlobalSettings settings = GlobalSettings.getInstance(context);

        adapter = new ListAdapter(this);

        RecyclerView listView = new RecyclerView(inflater.getContext());
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setHasFixedSize(true);
        listView.setAdapter(adapter);

        reloadLayout = new SwipeRefreshLayout(context);
        reloadLayout.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        reloadLayout.addView(listView);
        reloadLayout.setOnRefreshListener(this);
        return reloadLayout;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (listTask == null)
            load();
    }


    @Override
    public void onDestroy() {
        if (listTask != null && listTask.getStatus() == RUNNING)
            listTask.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onRefresh() {
        if (listTask != null && listTask.getStatus() != RUNNING)
            load();
    }


    @Override
    public void onClick(long id, Action action) {
        switch (action) {
            case PROFILE:
                Intent profile = new Intent(getContext(), UserProfile.class);
                profile.putExtra(KEY_PROFILE_ID, id);
                startActivity(profile);
                break;

            case FOLLOW:
                if (listTask != null && listTask.getStatus() != RUNNING) {
                    listTask = new ListLoader(this, FOLLOW);
                    listTask.execute(id);
                }
                break;

            case SUBSCRIBER:
                Intent following = new Intent(getContext(), UserDetail.class);
                following.putExtra(KEY_USERDETAIL_ID, id);
                following.putExtra(KEY_USERDETAIL_MODE, SUBSCRIBER);
                startActivity(following);
                break;
        }
    }


    public ListAdapter getAdapter() {
        return adapter;
    }


    public void setRefresh(boolean enable) {
        if (enable) {
            reloadLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (listTask.getStatus() != FINISHED && !reloadLayout.isRefreshing())
                        reloadLayout.setRefreshing(true);
                }
            }, 500);
        } else {
            reloadLayout.setRefreshing(false);
        }
    }


    private void load() {
        listTask = new ListLoader(this, LOAD);
        listTask.execute(userId);
    }
}
