package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.ListAction;
import org.nuclearfog.twidda.backend.ListManager;
import org.nuclearfog.twidda.backend.ListManager.ListManagerCallback;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.items.UserList;
import org.nuclearfog.twidda.backend.utils.DialogBuilder;
import org.nuclearfog.twidda.backend.utils.DialogBuilder.OnDialogClick;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.regex.Pattern;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.ListPopup.KEY_LIST_DESCR;
import static org.nuclearfog.twidda.activity.ListPopup.KEY_LIST_TITLE;
import static org.nuclearfog.twidda.activity.ListPopup.KEY_LIST_VISIB;
import static org.nuclearfog.twidda.backend.ListAction.Action.DELETE;
import static org.nuclearfog.twidda.backend.ListAction.Action.FOLLOW;
import static org.nuclearfog.twidda.backend.ListAction.Action.LOAD;
import static org.nuclearfog.twidda.backend.ListAction.Action.UNFOLLOW;
import static org.nuclearfog.twidda.backend.ListManager.Action.ADD_USER;
import static org.nuclearfog.twidda.backend.utils.DialogBuilder.DialogType.LIST_DELETE;
import static org.nuclearfog.twidda.backend.utils.DialogBuilder.DialogType.LIST_UNFOLLOW;
import static org.nuclearfog.twidda.fragment.UserListFragment.RESULT_REMOVED_LIST_ID;
import static org.nuclearfog.twidda.fragment.UserListFragment.RETURN_LIST_REMOVED;

/**
 * Activity to show an user list, members and tweets
 */
public class ListDetail extends AppCompatActivity implements OnTabSelectedListener,
        OnQueryTextListener, ListManagerCallback, OnDialogClick {

    /**
     * Key for the list ID, required
     */
    public static final String KEY_LIST_ID = "list-id";

    /**
     * Key to check if this list is owned by the current user
     */
    public static final String KEY_LIST_OWNER = "list-owner";

    /**
     * Key to get user list object
     */
    public static final String KEY_LIST_DATA = "list_data";

    /**
     * Request code for list editing
     */
    public static final int REQ_LIST_CHANGE = 1;

    /**
     * Return code when this list was successfully changed
     */
    public static final int RET_LIST_CHANGED = 2;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("@?\\w{1,15}");

    private FragmentAdapter adapter;
    private ListAction listLoaderTask;
    private ListManager userListManager;

    private TabLayout tablayout;
    private ViewPager pager;
    private Toolbar toolbar;
    private Dialog unfollowDialog, deleteDialog;

    private UserList userList;
    private long listId;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_listdetail);
        View root = findViewById(R.id.listdetail_root);
        toolbar = findViewById(R.id.listdetail_toolbar);
        tablayout = findViewById(R.id.listdetail_tab);
        pager = findViewById(R.id.listdetail_pager);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());

        adapter = new FragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(2);
        tablayout.setupWithViewPager(pager);
        tablayout.setSelectedTabIndicatorColor(settings.getHighlightColor());
        tablayout.addOnTabSelectedListener(this);
        deleteDialog = DialogBuilder.create(this, LIST_DELETE, this);
        unfollowDialog = DialogBuilder.create(this, LIST_UNFOLLOW, this);

        Bundle param = getIntent().getExtras();
        if (param != null) {
            boolean currentUserOwnsList;
            if (param.containsKey(KEY_LIST_DATA)) {
                userList = (UserList) param.getSerializable(KEY_LIST_DATA);
                currentUserOwnsList = userList.isListOwner();
                listId = userList.getId();
            } else {
                currentUserOwnsList = param.getBoolean(KEY_LIST_OWNER, false);
                listId = param.getLong(KEY_LIST_ID);
            }
            adapter.setupListContentPage(listId, currentUserOwnsList);
        }

        Tab tweetTab = tablayout.getTabAt(0);
        Tab userTab = tablayout.getTabAt(1);
        Tab subscrTab = tablayout.getTabAt(2);
        if (tweetTab != null && userTab != null && subscrTab != null) {
            tweetTab.setIcon(R.drawable.list);
            userTab.setIcon(R.drawable.user);
            subscrTab.setIcon(R.drawable.subscriber);
        }
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        FontTool.setViewFontAndColor(settings, root);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (listLoaderTask == null && userList == null) {
            if (userList != null) {
                toolbar.setTitle(userList.getTitle());
                toolbar.setSubtitle(userList.getDescription());
                invalidateOptionsMenu();
            }
            loadList();
        }
    }


    @Override
    protected void onDestroy() {
        if (listLoaderTask != null && listLoaderTask.getStatus() == RUNNING) {
            listLoaderTask.cancel(true);
        }
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.userlist, m);
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        MenuItem editList = m.findItem(R.id.menu_list_edit);
        MenuItem deleteList = m.findItem(R.id.menu_delete_list);
        MenuItem followList = m.findItem(R.id.menu_follow_list);
        MenuItem search = m.findItem(R.id.menu_list_add_user);
        SearchView searchUser = (SearchView) search.getActionView();
        search.collapseActionView();
        if (userList != null) {
            if (userList.isListOwner()) {
                searchUser.setQueryHint(getString(R.string.menu_add_user));
                searchUser.setOnQueryTextListener(this);
                editList.setVisible(true);
                deleteList.setVisible(true);
                search.setVisible(true);
            } else {
                followList.setVisible(true);
                if (userList.isFollowing()) {
                    followList.setTitle(R.string.menu_unfollow_list);
                } else {
                    followList.setTitle(R.string.menu_list_follow);
                }
            }
        }
        return super.onPrepareOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (userList != null && (listLoaderTask == null || listLoaderTask.getStatus() != RUNNING)) {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_list_edit) {
                Intent editList = new Intent(this, ListPopup.class);
                editList.putExtra(ListPopup.KEY_LIST_ID, userList.getId());
                editList.putExtra(KEY_LIST_TITLE, userList.getTitle());
                editList.putExtra(KEY_LIST_DESCR, userList.getDescription());
                editList.putExtra(KEY_LIST_VISIB, !userList.isPrivate());
                startActivityForResult(editList, REQ_LIST_CHANGE);
            } else if (itemId == R.id.menu_delete_list) {
                if (!deleteDialog.isShowing()) {
                    deleteDialog.show();
                }
            } else if (itemId == R.id.menu_follow_list) {
                if (userList.isFollowing()) {
                    if (!unfollowDialog.isShowing()) {
                        unfollowDialog.show();
                    }
                } else {
                    listLoaderTask = new ListAction(this, FOLLOW);
                    listLoaderTask.execute(userList.getId());
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (tablayout.getSelectedTabPosition() > 0) {
            pager.setCurrentItem(0);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onActivityResult(int reqCode, int returnCode, @Nullable Intent data) {
        if (reqCode == REQ_LIST_CHANGE && returnCode == RET_LIST_CHANGED)
            loadList();
        super.onActivityResult(reqCode, returnCode, data);
    }


    @Override
    public void onConfirm(DialogBuilder.DialogType type) {
        switch (type) {
            case LIST_DELETE:
                listLoaderTask = new ListAction(this, DELETE);
                listLoaderTask.execute(userList.getId());
                break;

            case LIST_UNFOLLOW:
                listLoaderTask = new ListAction(this, UNFOLLOW);
                listLoaderTask.execute(userList.getId());
                break;
        }
    }


    @Override
    public void onTabSelected(Tab tab) {
    }


    @Override
    public void onTabUnselected(Tab tab) {
        adapter.scrollToTop(tab.getPosition());
    }


    @Override
    public void onTabReselected(Tab tab) {
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        if (USERNAME_PATTERN.matcher(query).matches()) {
            if (userListManager == null || userListManager.getStatus() != RUNNING) {
                Toast.makeText(this, R.string.info_adding_user_to_list, Toast.LENGTH_SHORT).show();
                userListManager = new ListManager(listId, ADD_USER, this, this);
                userListManager.execute(query);
                return true;
            }
        } else {
            Toast.makeText(this, R.string.error_username_format, Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    @Override
    public void onSuccess(String[] names) {
        String info = names[0];
        if (!info.startsWith("@"))
            info = '@' + info;
        info += getString(R.string.info_user_added_to_list);
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    /**
     * called from {@link ListAction} to update userlist information
     *
     * @param userList userlist information
     * @param action   what action was performed
     */
    public void onSuccess(UserList userList, ListAction.Action action) {
        this.userList = userList;
        switch (action) {
            case LOAD:
                toolbar.setTitle(userList.getTitle());
                toolbar.setSubtitle(userList.getDescription());
                invalidateOptionsMenu();
                break;

            case FOLLOW:
                Toast.makeText(this, R.string.info_list_followed, Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                break;

            case UNFOLLOW:
                Toast.makeText(this, R.string.info_list_unfollowed, Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                break;

            case DELETE:
                Intent result = new Intent();
                result.putExtra(RESULT_REMOVED_LIST_ID, userList.getId());
                setResult(RETURN_LIST_REMOVED, result);
                Toast.makeText(this, R.string.info_list_removed, Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }

    /**
     * called from {@link ListAction} if an error occurs
     *
     * @param err error information
     */
    public void onFailure(EngineException err) {
        ErrorHandler.handleFailure(this, err);
        if (userList == null) {
            if (err.resourceNotFound()) {
                Intent result = new Intent();
                result.putExtra(RESULT_REMOVED_LIST_ID, listId);
                setResult(RETURN_LIST_REMOVED, result);
            }
            finish();
        }
    }

    /**
     * load list information
     */
    private void loadList() {
        listLoaderTask = new ListAction(this, LOAD);
        listLoaderTask.execute(listId);
    }
}