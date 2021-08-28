package org.nuclearfog.twidda.activity;

import static org.nuclearfog.twidda.backend.UserExcludeLoader.Mode.BLOCK_USER;
import static org.nuclearfog.twidda.backend.UserExcludeLoader.Mode.MUTE_USER;
import static org.nuclearfog.twidda.backend.UserExcludeLoader.Mode.REFRESH;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import org.nuclearfog.twidda.backend.UserExcludeLoader;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * Activity to show block and mute list of the current user
 *
 * @author nuclearfog
 */
public class UserExclude extends AppCompatActivity implements OnTabSelectedListener, OnQueryTextListener {

    private GlobalSettings settings;
    private UserExcludeLoader userExclTask;

    private Toolbar toolbar;
    private TabLayout tablayout;


    @Override
    protected void onCreate(Bundle savedInst) {
        super.onCreate(savedInst);
        setContentView(R.layout.page_user_exclude);
        View root = findViewById(R.id.userexclude_root);
        toolbar = findViewById(R.id.userexclude_toolbar);
        tablayout = findViewById(R.id.userexclude_tab);
        ViewPager pager = findViewById(R.id.userexclude_pager);

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(2);
        adapter.setupMuteBlockPage();
        tablayout.setupWithViewPager(pager);
        tablayout.addOnTabSelectedListener(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        settings = GlobalSettings.getInstance(this);
        AppStyles.setTheme(settings, root);
        AppStyles.setTabIcons(tablayout, settings, R.array.user_exclude_icons);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.excludelist, m);
        MenuItem search = m.findItem(R.id.menu_exclude_user);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setOnQueryTextListener(this);
        AppStyles.setMenuIconColor(m, settings.getIconColor());
        AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        SearchView searchView = (SearchView) m.findItem(R.id.menu_exclude_user).getActionView();
        if (tablayout.getSelectedTabPosition() == 0) {
            String hint = getString(R.string.menu_hint_mute_user);
            searchView.setQueryHint(hint);
        } else if (tablayout.getSelectedTabPosition() == 1) {
            String hint = getString(R.string.menu_hint_block_user);
            searchView.setQueryHint(hint);
        }
        return super.onPrepareOptionsMenu(m);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (userExclTask == null) {
            userExclTask = new UserExcludeLoader(this, "");
            userExclTask.execute(REFRESH);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onTabSelected(Tab tab) {
        invalidateOptionsMenu();
    }


    @Override
    public void onTabUnselected(Tab tab) {
    }


    @Override
    public void onTabReselected(Tab tab) {
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        if (userExclTask == null || userExclTask.getStatus() != AsyncTask.Status.RUNNING) {
            userExclTask = new UserExcludeLoader(this, query);
            if (tablayout.getSelectedTabPosition() == 0) {
                userExclTask.execute(MUTE_USER);
                return true;
            }
            if (tablayout.getSelectedTabPosition() == 1) {
                userExclTask.execute(BLOCK_USER);
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    /**
     * called from {@link UserExcludeLoader} if task finished successfully
     */
    public void onSuccess(UserExcludeLoader.Mode mode) {
        switch (mode) {
            case MUTE_USER:
                Toast.makeText(this, R.string.info_user_muted, Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                break;

            case BLOCK_USER:
                Toast.makeText(this, R.string.info_user_blocked, Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                break;
        }
    }

    /**
     * called from {@link UserExcludeLoader} if an error occurs
     */
    public void onError(EngineException err) {
        ErrorHandler.handleFailure(this, err);
    }
}