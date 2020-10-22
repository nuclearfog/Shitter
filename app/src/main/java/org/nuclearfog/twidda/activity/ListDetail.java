package org.nuclearfog.twidda.activity;

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
import org.nuclearfog.twidda.backend.UserListManager;
import org.nuclearfog.twidda.backend.UserListManager.ListManagerCallback;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.ListPopup.KEY_LIST_DESCR;
import static org.nuclearfog.twidda.activity.ListPopup.KEY_LIST_ID;
import static org.nuclearfog.twidda.activity.ListPopup.KEY_LIST_TITLE;
import static org.nuclearfog.twidda.activity.ListPopup.KEY_LIST_VISIB;
import static org.nuclearfog.twidda.backend.UserListManager.Action.ADD_USER;

/**
 * Activity to show an user list, members and tweets
 */
public class ListDetail extends AppCompatActivity implements OnTabSelectedListener, OnQueryTextListener, ListManagerCallback {

    /**
     * Key for the list ID, required
     */
    public static final String KEY_LISTDETAIL_ID = "list-id";

    /**
     * Key for the list title
     */
    public static final String KEY_LISTDETAIL_TITLE = "list-title";

    /**
     * Key for the list description
     */
    public static final String KEY_LISTDETAIL_DESCR = "list-descr";

    /**
     * Key for the list description
     */
    public static final String KEY_LISTDETAIL_VISIB = "list-visibility";

    /**
     * Key to check if this list is owned by the current user
     */
    public static final String KEY_CURRENT_USER_OWNS = "list-owner";

    /**
     * Request code for list editing
     */
    public static final int REQ_LIST_CHANGE = 1;

    /**
     * Return code when this list was sucessfully changed
     */
    public static final int RET_LIST_CHANGED = 2;

    private UserListManager listAsync;
    private FragmentAdapter adapter;

    // Views
    private TabLayout tablayout;
    private ViewPager pager;
    private Toolbar toolbar;

    // list information
    private long listId = -1;
    private String title = "";
    private String description = "";
    private boolean isPublic = false;
    private boolean belongsToCurrentUser = false;

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

        Bundle param = getIntent().getExtras();
        if (param != null) {
            listId = param.getLong(KEY_LISTDETAIL_ID, -1);
            title = param.getString(KEY_LISTDETAIL_TITLE, "");
            description = param.getString(KEY_LISTDETAIL_DESCR, "");
            isPublic = param.getBoolean(KEY_LISTDETAIL_VISIB, false);
            belongsToCurrentUser = param.getBoolean(KEY_CURRENT_USER_OWNS, false);
            adapter.setupListContentPage(listId);
            Tab tweetTab = tablayout.getTabAt(0);
            Tab userTab = tablayout.getTabAt(1);
            if (tweetTab != null && userTab != null) {
                tweetTab.setIcon(R.drawable.list);
                userTab.setIcon(R.drawable.user);
            }
            toolbar.setTitle(title);
            toolbar.setSubtitle(description);
            setSupportActionBar(toolbar);
        }
        FontTool.setViewFontAndColor(settings, root);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.userlist, m);
        MenuItem search = m.findItem(R.id.menu_list_add_user);
        MenuItem editList = m.findItem(R.id.menu_list_edit);
        SearchView searchUser = (SearchView) search.getActionView();
        if (belongsToCurrentUser) {
            searchUser.setQueryHint(getString(R.string.menu_add_user));
            searchUser.setOnQueryTextListener(this);
        } else {
            editList.setVisible(false);
            search.setVisible(false);
        }
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        MenuItem search = m.findItem(R.id.menu_list_add_user);
        search.collapseActionView();
        return super.onPrepareOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_list_edit) {
            Intent editList = new Intent(this, ListPopup.class);
            editList.putExtra(KEY_LIST_ID, listId);
            editList.putExtra(KEY_LIST_TITLE, title);
            editList.putExtra(KEY_LIST_DESCR, description);
            editList.putExtra(KEY_LIST_VISIB, isPublic);
            startActivityForResult(editList, REQ_LIST_CHANGE);
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
        if (reqCode == REQ_LIST_CHANGE && returnCode == RET_LIST_CHANGED && data != null) {
            // refresh list information
            title = data.getStringExtra(KEY_LISTDETAIL_TITLE);
            description = data.getStringExtra(KEY_LISTDETAIL_DESCR);
            isPublic = data.getBooleanExtra(KEY_LISTDETAIL_VISIB, false);
            if (title == null || description == null)
                title = description = "";
            toolbar.setTitle(title);
            toolbar.setSubtitle(description);
        }
        super.onActivityResult(reqCode, returnCode, data);
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
        if (listAsync == null || listAsync.getStatus() != RUNNING) {
            Toast.makeText(this, R.string.info_adding_user_to_list, Toast.LENGTH_SHORT).show();
            listAsync = new UserListManager(listId, ADD_USER, getApplicationContext(), this);
            listAsync.execute(query);
        }
        return true;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }


    @Override
    public void onSuccess() {
        adapter.notifySettingsChanged();
        Toast.makeText(this, R.string.info_user_added_to_list, Toast.LENGTH_SHORT).show();
        invalidateOptionsMenu();
    }


    @Override
    public void onFailure(EngineException err) {
        ErrorHandler.handleFailure(this, err);
    }
}