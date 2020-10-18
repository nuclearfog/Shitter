package org.nuclearfog.twidda.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import org.nuclearfog.twidda.backend.utils.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.backend.UserListManager.Action.ADD_USER;

/**
 * Activity to show an user list, members and tweets
 */
public class ListDetail extends AppCompatActivity implements OnTabSelectedListener, OnQueryTextListener, ListManagerCallback {

    public static final String KEY_LISTDETAIL_ID = "list-id";
    public static final String KEY_LISTDETAIL_TITLE = "list-title";
    public static final String KEY_LISTDETAIL_DESCR = "list-descr";

    private UserListManager listAsync;

    private FragmentAdapter adapter;
    private TabLayout tablayout;
    private ViewPager pager;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_listdetail);
        View root = findViewById(R.id.listdetail_root);
        Toolbar toolbar = findViewById(R.id.listdetail_toolbar);
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
        if (param != null && param.containsKey(KEY_LISTDETAIL_ID)) {
            long id = param.getLong(KEY_LISTDETAIL_ID);
            String title = param.getString(KEY_LISTDETAIL_TITLE, "");
            String subTitle = param.getString(KEY_LISTDETAIL_DESCR, "");
            adapter.setupListContentPage(id);
            Tab tlTab = tablayout.getTabAt(0);
            Tab trTab = tablayout.getTabAt(1);
            if (tlTab != null && trTab != null) {
                tlTab.setIcon(R.drawable.list);
                trTab.setIcon(R.drawable.user);
            }
            toolbar.setTitle(title);
            toolbar.setSubtitle(subTitle);
            setSupportActionBar(toolbar);
        }
        FontTool.setViewFontAndColor(settings, root);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.userlist, m);
        MenuItem search = m.findItem(R.id.add_user);
        SearchView addUser = (SearchView) search.getActionView();
        addUser.setQueryHint(getString(R.string.list_add_user));
        addUser.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(m);
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
            Bundle param = getIntent().getExtras();
            if (param != null && param.containsKey(KEY_LISTDETAIL_ID)) {
                long id = param.getLong(KEY_LISTDETAIL_ID);
                listAsync = new UserListManager(id, ADD_USER, getApplicationContext(), this);
                listAsync.execute(query);
            }
        }
        return true;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }


    @Override
    public void onSuccess() {
    }


    @Override
    public void onFailure(EngineException err) {

    }
}