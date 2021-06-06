package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
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
import org.nuclearfog.twidda.backend.model.TwitterList;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.dialog.ConfirmDialog;
import org.nuclearfog.twidda.dialog.ConfirmDialog.DialogType;
import org.nuclearfog.twidda.dialog.ConfirmDialog.OnConfirmListener;

import java.util.regex.Pattern;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.ListEditor.KEY_LIST_EDITOR_DATA;
import static org.nuclearfog.twidda.backend.ListAction.Action.DELETE;
import static org.nuclearfog.twidda.backend.ListAction.Action.FOLLOW;
import static org.nuclearfog.twidda.backend.ListAction.Action.LOAD;
import static org.nuclearfog.twidda.backend.ListAction.Action.UNFOLLOW;
import static org.nuclearfog.twidda.backend.ListManager.Action.ADD_USER;
import static org.nuclearfog.twidda.fragment.UserListFragment.RESULT_REMOVED_LIST_ID;
import static org.nuclearfog.twidda.fragment.UserListFragment.RESULT_UPDATE_LIST;
import static org.nuclearfog.twidda.fragment.UserListFragment.RETURN_LIST_REMOVED;
import static org.nuclearfog.twidda.fragment.UserListFragment.RETURN_LIST_UPDATED;

/**
 * Activity to show an user list, members and tweets
 *
 * @author nuclearfog
 */
public class ListDetail extends AppCompatActivity implements OnTabSelectedListener,
        OnQueryTextListener, ListManagerCallback, OnConfirmListener {

    /**
     * Key to get user list object
     */
    public static final String KEY_LIST_DATA = "list_data";

    /**
     * return updated userlist information
     */
    public static final String RET_LIST_DATA = "list-data";

    /**
     * Request code for list editing
     */
    private static final int REQ_LIST_CHANGE = 0x7518;

    /**
     * Return code when this list was successfully changed
     */
    public static final int RET_LIST_CHANGED = 0x1A5518E1;

    /**
     * regex pattern to validate username
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("@?\\w{1,15}");

    private FragmentAdapter adapter;
    private ListAction listLoaderTask;
    private ListManager userListManager;
    private GlobalSettings settings;

    private TabLayout tablayout;
    private ViewPager pager;
    private Toolbar toolbar;
    private Dialog unfollowDialog, deleteDialog;

    @Nullable
    private TwitterList userList;
    private long listId = -1;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_listdetail);
        View root = findViewById(R.id.listdetail_root);
        toolbar = findViewById(R.id.listdetail_toolbar);
        tablayout = findViewById(R.id.listdetail_tab);
        pager = findViewById(R.id.listdetail_pager);

        settings = GlobalSettings.getInstance(this);

        adapter = new FragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(2);
        tablayout.setupWithViewPager(pager);
        tablayout.addOnTabSelectedListener(this);

        deleteDialog = new ConfirmDialog(this, DialogType.LIST_DELETE, this);
        unfollowDialog = new ConfirmDialog(this, DialogType.LIST_UNFOLLOW, this);

        Object data = getIntent().getSerializableExtra(KEY_LIST_DATA);
        if (data instanceof TwitterList) {
            userList = (TwitterList) data;
            listId = userList.getId();
            toolbar.setTitle(userList.getTitle());
            toolbar.setSubtitle(userList.getDescription());
            adapter.setupListContentPage(userList.getId(), userList.isListOwner());
        }

        setSupportActionBar(toolbar);
        AppStyles.setTheme(settings, root);
        AppStyles.setTabIcons(tablayout, settings, R.array.list_tab_icons);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (listLoaderTask == null) {
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
        AppStyles.setMenuIconColor(m, settings.getIconColor());
        AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        MenuItem editList = m.findItem(R.id.menu_list_edit);
        MenuItem deleteList = m.findItem(R.id.menu_delete_list);
        MenuItem followList = m.findItem(R.id.menu_follow_list);
        MenuItem search = m.findItem(R.id.menu_list_add_user);
        SearchView searchUser = (SearchView) search.getActionView();
        AppStyles.setTheme(settings, searchUser);
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
            // open user list editor
            if (item.getItemId() == R.id.menu_list_edit) {
                Intent editList = new Intent(this, ListEditor.class);
                editList.putExtra(KEY_LIST_EDITOR_DATA, userList);
                startActivityForResult(editList, REQ_LIST_CHANGE);
            }
            // delete user list
            else if (item.getItemId() == R.id.menu_delete_list) {
                if (!deleteDialog.isShowing()) {
                    deleteDialog.show();
                }
            }
            // follow user list
            else if (item.getItemId() == R.id.menu_follow_list) {
                if (userList.isFollowing()) {
                    if (!unfollowDialog.isShowing()) {
                        unfollowDialog.show();
                    }
                } else {
                    listLoaderTask = new ListAction(this, FOLLOW);
                    listLoaderTask.execute(userList.getId());
                }
            }
            // theme expanded search view
            else if (item.getItemId() == R.id.menu_list_add_user) {
                SearchView searchView = (SearchView) item.getActionView();
                AppStyles.setTheme(settings, searchView, Color.TRANSPARENT);
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (tablayout.getSelectedTabPosition() > 0) {
            pager.setCurrentItem(0);
        } else {
            Intent result = new Intent();
            result.putExtra(RESULT_UPDATE_LIST, userList);
            setResult(RETURN_LIST_UPDATED, result);
            super.onBackPressed();
        }
    }


    @Override
    public void onActivityResult(int reqCode, int returnCode, @Nullable Intent result) {
        if (result != null && reqCode == REQ_LIST_CHANGE) {
            if (returnCode == RET_LIST_CHANGED) {
                Object data = result.getSerializableExtra(RET_LIST_DATA);
                if (data instanceof TwitterList) {
                    userList = (TwitterList) data;
                    toolbar.setTitle(userList.getTitle());
                    toolbar.setSubtitle(userList.getDescription());
                    invalidateOptionsMenu();
                }
            }
        }
        super.onActivityResult(reqCode, returnCode, result);
    }


    @Override
    public void onConfirm(DialogType type) {
        // delete user list
        if (type == DialogType.LIST_DELETE) {
            listLoaderTask = new ListAction(this, DELETE);
            listLoaderTask.execute(listId);
        }
        // unfollow user list
        else if (type == DialogType.LIST_UNFOLLOW) {
            listLoaderTask = new ListAction(this, UNFOLLOW);
            listLoaderTask.execute(listId);
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
    public boolean onQueryTextChange(String newText) {
        return false;
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
    public void onFailure(@Nullable EngineException err) {
        ErrorHandler.handleFailure(this, err);
    }

    /**
     * called from {@link ListAction} to update userlist information
     *
     * @param userList TwitterList information
     */
    public void onSuccess(TwitterList userList, ListAction.Action action) {
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
     * @param err    error information
     * @param listId ID of the list where an error occurred
     */
    public void onFailure(@Nullable EngineException err, long listId) {
        ErrorHandler.handleFailure(this, err);
        if (err != null && err.resourceNotFound()) {
            // List does not exist
            Intent result = new Intent();
            result.putExtra(RESULT_REMOVED_LIST_ID, listId);
            setResult(RETURN_LIST_REMOVED, result);
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