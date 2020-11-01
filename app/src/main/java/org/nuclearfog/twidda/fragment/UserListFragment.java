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
import org.nuclearfog.twidda.activity.ListDetail;
import org.nuclearfog.twidda.activity.UserDetail;
import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.adapter.FragmentAdapter.FragmentChangeObserver;
import org.nuclearfog.twidda.adapter.ListAdapter;
import org.nuclearfog.twidda.adapter.ListAdapter.ListClickListener;
import org.nuclearfog.twidda.backend.TwitterListLoader;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.holder.UserListList;
import org.nuclearfog.twidda.backend.items.TwitterList;
import org.nuclearfog.twidda.backend.utils.DialogBuilder;
import org.nuclearfog.twidda.backend.utils.DialogBuilder.OnDialogClick;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.ListDetail.KEY_CURRENT_USER_OWNS;
import static org.nuclearfog.twidda.activity.ListDetail.KEY_LISTDETAIL_DESCR;
import static org.nuclearfog.twidda.activity.ListDetail.KEY_LISTDETAIL_ID;
import static org.nuclearfog.twidda.activity.ListDetail.KEY_LISTDETAIL_TITLE;
import static org.nuclearfog.twidda.activity.ListDetail.KEY_LISTDETAIL_VISIB;
import static org.nuclearfog.twidda.activity.UserDetail.KEY_USERDETAIL_ID;
import static org.nuclearfog.twidda.activity.UserDetail.KEY_USERDETAIL_MODE;
import static org.nuclearfog.twidda.activity.UserDetail.USERLIST_SUBSCRBR;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_ID;
import static org.nuclearfog.twidda.backend.TwitterListLoader.Action.DELETE;
import static org.nuclearfog.twidda.backend.TwitterListLoader.Action.FOLLOW;
import static org.nuclearfog.twidda.backend.TwitterListLoader.Action.LOAD_MEMBERSHIPS;
import static org.nuclearfog.twidda.backend.TwitterListLoader.Action.LOAD_USERLISTS;
import static org.nuclearfog.twidda.backend.TwitterListLoader.NO_CURSOR;
import static org.nuclearfog.twidda.backend.utils.DialogBuilder.DialogType.LIST_DELETE;
import static org.nuclearfog.twidda.backend.utils.DialogBuilder.DialogType.LIST_UNFOLLOW;

/**
 * Fragment class for user lists
 */
public class UserListFragment extends Fragment implements OnRefreshListener, ListClickListener,
        FragmentChangeObserver, OnDialogClick {

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
     * setup for lists of an user
     */
    public static final int LIST_USER_OWNS = 1;

    /**
     * setup for list an user is subscribed to
     */
    public static final int LIST_USER_SUBSCR_TO = 2;

    private TwitterListLoader listTask;

    private SwipeRefreshLayout reloadLayout;
    private RecyclerView list;
    private ListAdapter adapter;

    private Dialog followDialog, deleteDialog;

    private long selectedList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle param) {
        Context context = inflater.getContext();

        GlobalSettings settings = GlobalSettings.getInstance(context);
        adapter = new ListAdapter(this, settings);

        list = new RecyclerView(inflater.getContext());
        list.setLayoutManager(new LinearLayoutManager(context));
        list.setHasFixedSize(true);
        list.setAdapter(adapter);

        followDialog = DialogBuilder.create(requireContext(), LIST_UNFOLLOW, this);
        deleteDialog = DialogBuilder.create(requireContext(), LIST_DELETE, this);
        reloadLayout = new SwipeRefreshLayout(context);
        reloadLayout.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        reloadLayout.setOnRefreshListener(this);
        reloadLayout.addView(list);
        return reloadLayout;
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
    public void onDestroy() {
        if (listTask != null && listTask.getStatus() == RUNNING)
            listTask.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onRefresh() {
        if (listTask != null && listTask.getStatus() != RUNNING) {
            load(NO_CURSOR);
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
                        if (!followDialog.isShowing()) {
                            selectedList = listItem.getId();
                            followDialog.show();
                        }
                    } else {
                        Toast.makeText(requireContext(), R.string.info_following_list, Toast.LENGTH_SHORT).show();
                        listTask = new TwitterListLoader(this, FOLLOW, listItem.getId(), "");
                        listTask.execute(listItem.getId());
                    }
                    break;

                case SUBSCRIBER:
                    Intent subscriberIntent = new Intent(getContext(), UserDetail.class);
                    subscriberIntent.putExtra(KEY_USERDETAIL_ID, listItem.getId());
                    subscriberIntent.putExtra(KEY_USERDETAIL_MODE, USERLIST_SUBSCRBR);
                    startActivity(subscriberIntent);
                    break;

                case MEMBER:
                    Intent listIntent = new Intent(getContext(), ListDetail.class);
                    listIntent.putExtra(KEY_CURRENT_USER_OWNS, listItem.isListOwner());
                    listIntent.putExtra(KEY_LISTDETAIL_ID, listItem.getId());
                    listIntent.putExtra(KEY_LISTDETAIL_TITLE, listItem.getTitle());
                    listIntent.putExtra(KEY_LISTDETAIL_DESCR, listItem.getDescription());
                    listIntent.putExtra(KEY_LISTDETAIL_VISIB, !listItem.isPrivate());
                    startActivity(listIntent);
                    break;

                case DELETE:
                    if (!deleteDialog.isShowing()) {
                        selectedList = listItem.getId();
                        deleteDialog.show();
                    }
                    break;
            }
        }
    }


    @Override
    public void onConfirm(DialogBuilder.DialogType type) {
        if (type == LIST_UNFOLLOW) {
            listTask = new TwitterListLoader(this, FOLLOW, selectedList, "");
            listTask.execute();
        } else if (type == LIST_DELETE) {
            listTask = new TwitterListLoader(this, DELETE, selectedList, "");
            listTask.execute();
        }
    }


    public void onFooterClick(long cursor) {
        if (listTask != null && listTask.getStatus() != RUNNING) {
            load(cursor);
        }
    }


    @Override
    public void onReset() {
        if (list != null) {
            list.setAdapter(adapter);
            setRefresh(true);
            load(NO_CURSOR);
        }
    }


    @Override
    public void onTabChange() {
        if (list != null) {
            list.smoothScrollToPosition(0);
        }
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
        adapter.removeItem(list);
    }

    /**
     * called from {@link TwitterListLoader} to enable or disable RefreshLayout
     *
     * @param enable true to enable RefreshLayout with delay
     */
    private void setRefresh(boolean enable) {
        if (enable) {
            reloadLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (listTask != null && listTask.getStatus() != FINISHED
                            && !reloadLayout.isRefreshing())
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
    public void onError(@Nullable EngineException error) {
        if (getContext() != null && error != null)
            ErrorHandler.handleFailure(getContext(), error);
        adapter.disableLoading();
        setRefresh(false);
    }

    /**
     * load content into the list
     */
    private void load(long cursor) {
        Bundle param = getArguments();
        if (param != null) {
            long id = param.getLong(KEY_FRAG_LIST_OWNER_ID, 0);
            String ownerName = param.getString(KEY_FRAG_LIST_OWNER_NAME, "");
            int type = param.getInt(KEY_FRAG_LIST_LIST_TYPE);
            if (type == LIST_USER_OWNS)
                listTask = new TwitterListLoader(this, LOAD_USERLISTS, id, ownerName);
            else if (type == LIST_USER_SUBSCR_TO)
                listTask = new TwitterListLoader(this, LOAD_MEMBERSHIPS, id, ownerName);
            listTask.execute(cursor);
        }
    }
}