package org.nuclearfog.twidda.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.activity.ListDetail;
import org.nuclearfog.twidda.activity.UserDetail;
import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.adapter.ListAdapter;
import org.nuclearfog.twidda.adapter.ListAdapter.ListClickListener;
import org.nuclearfog.twidda.backend.TwitterListLoader;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.items.TwitterList;
import org.nuclearfog.twidda.backend.tools.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.List;

import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.ListDetail.KEY_LISTDETAIL_ID;
import static org.nuclearfog.twidda.activity.ListDetail.KEY_LISTDETAIL_NAME;
import static org.nuclearfog.twidda.activity.UserDetail.KEY_USERDETAIL_ID;
import static org.nuclearfog.twidda.activity.UserDetail.KEY_USERDETAIL_MODE;
import static org.nuclearfog.twidda.activity.UserDetail.USERLIST_SUBSCRBR;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_ID;
import static org.nuclearfog.twidda.backend.TwitterListLoader.Action.DELETE;
import static org.nuclearfog.twidda.backend.TwitterListLoader.Action.FOLLOW;
import static org.nuclearfog.twidda.backend.TwitterListLoader.Action.LOAD;

/**
 * Fragment class for user lists
 */
public class ListFragment extends Fragment implements OnRefreshListener, ListClickListener {

    public static final String KEY_FRAG_LIST_OWNER_ID = "list_owner_id";
    public static final String KEY_FRAG_LIST_OWNER_NAME = "list_owner_name";

    private TwitterListLoader listTask;

    private SwipeRefreshLayout reloadLayout;
    private ListAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle param) {
        Context context = inflater.getContext();

        GlobalSettings settings = GlobalSettings.getInstance(context);
        adapter = new ListAdapter(this, settings);

        RecyclerView listView = new RecyclerView(inflater.getContext());
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setHasFixedSize(true);
        listView.setAdapter(adapter);

        reloadLayout = new SwipeRefreshLayout(context);
        reloadLayout.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        reloadLayout.setOnRefreshListener(this);
        reloadLayout.addView(listView);
        return reloadLayout;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (listTask == null) {
            load();
        }
    }


    @Override
    public void onDestroy() {
        if (listTask != null && listTask.getStatus() == RUNNING)
            listTask.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onRefresh() {
        if (listTask != null && listTask.getStatus() != RUNNING) {
            load();
        }
    }


    @Override
    public void onClick(final TwitterList listItem, Action action) {
        if (getContext() != null && !reloadLayout.isRefreshing()) {
            switch (action) {
                case PROFILE:
                    Intent profile = new Intent(getContext(), UserProfile.class);
                    profile.putExtra(KEY_PROFILE_ID, listItem.getListOwner().getId());
                    startActivity(profile);
                    break;

                case FOLLOW:
                    if (listItem.isFollowing()) {
                        Builder confirmDialog = new Builder(getContext(), R.style.ConfirmDialog);
                        confirmDialog.setMessage(R.string.confirm_unfollow_list);
                        confirmDialog.setNegativeButton(R.string.confirm_no, null);
                        confirmDialog.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listTask = new TwitterListLoader(ListFragment.this, FOLLOW);
                                listTask.execute(listItem.getId());
                            }
                        });
                        confirmDialog.show();
                    } else {
                        listTask = new TwitterListLoader(this, FOLLOW);
                        listTask.execute(listItem.getId());
                    }
                    break;

                case SUBSCRIBER:
                    Intent following = new Intent(getContext(), UserDetail.class);
                    following.putExtra(KEY_USERDETAIL_ID, listItem.getId());
                    following.putExtra(KEY_USERDETAIL_MODE, USERLIST_SUBSCRBR);
                    startActivity(following);
                    break;

                case MEMBER:
                    Intent list = new Intent(getContext(), ListDetail.class);
                    list.putExtra(KEY_LISTDETAIL_ID, listItem.getId());
                    list.putExtra(KEY_LISTDETAIL_NAME, listItem.getTitle());
                    startActivity(list);
                    break;

                case DELETE:
                    Builder confirmDialog = new Builder(getContext(), R.style.ConfirmDialog);
                    confirmDialog.setMessage(R.string.confirm_delete_list);
                    confirmDialog.setNegativeButton(R.string.confirm_no, null);
                    confirmDialog.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            listTask = new TwitterListLoader(ListFragment.this, DELETE);
                            listTask.execute(listItem.getId());
                        }
                    });
                    confirmDialog.show();
                    break;
            }
        }
    }

    /**
     * set data to list
     *
     * @param data List of Twitter list data
     */
    public void setData(List<TwitterList> data) {
        adapter.setData(data);
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
     * @param id ID of list to remove
     */
    public void removeItem(long id) {
        adapter.removeItem(id);
    }

    /**
     * called from {@link TwitterListLoader} to enable or disable RefreshLayout
     * @param enable true to enable RefreshLayout with delay
     */
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

    /**
     * called from {@link TwitterListLoader} if an error occurs
     *
     * @param error Twitter exception
     */
    public void onError(EngineException error) {
        if (getContext() != null) {
            ErrorHandler.handleFailure(getContext(), error);
        }
    }

    /**
     * load content into the list
     */
    private void load() {
        Bundle param = getArguments();
        if (param != null) {
            listTask = new TwitterListLoader(this, LOAD);
            if (param.containsKey(KEY_FRAG_LIST_OWNER_ID)) {
                long ownerId = param.getLong(KEY_FRAG_LIST_OWNER_ID);
                listTask.execute(ownerId);
            } else if (param.containsKey(KEY_FRAG_LIST_OWNER_NAME)) {
                String ownerName = param.getString(KEY_FRAG_LIST_OWNER_NAME);
                listTask.execute(ownerName);
            }
        }
    }
}