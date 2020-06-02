package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.LinkContentLoader;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.view.Window.FEATURE_NO_TITLE;
import static org.nuclearfog.twidda.activity.SearchPage.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_ID;

/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity implements OnTabSelectedListener {

    public static final int RETURN_DB_CLEARED = 1;
    public static final int RETURN_APP_LOGOUT = 2;
    private static final int REQUEST_APP_LOGIN = 1;
    private static final int REQUEST_APP_SETTINGS = 2;

    @Nullable
    private FragmentAdapter adapter;
    private TabLayout tablayout;
    private GlobalSettings settings;
    private Dialog loadingCircle;
    private ViewPager pager;
    private View root;


    static {
        // Enable vector drawable support
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_main);
        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        pager = findViewById(R.id.home_pager);
        tablayout = findViewById(R.id.home_tab);
        root = findViewById(R.id.main_layout);
        loadingCircle = new Dialog(this, R.style.LoadingDialog);
        View load = View.inflate(this, R.layout.item_load, null);

        settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
        tablayout.setSelectedTabIndicatorColor(settings.getHighlightColor());
        tablayout.setupWithViewPager(pager);
        pager.setOffscreenPageLimit(3);
        loadingCircle.requestWindowFeature(FEATURE_NO_TITLE);
        loadingCircle.setContentView(load);
        loadingCircle.setCanceledOnTouchOutside(false);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        tablayout.addOnTabSelectedListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!settings.getLogin()) {
            Intent loginIntent = new Intent(this, LoginPage.class);
            startActivityForResult(loginIntent, REQUEST_APP_LOGIN);
        } else if (adapter == null) {
            adapter = new FragmentAdapter(getSupportFragmentManager());
            adapter.setupForHomePage();
            pager.setAdapter(adapter);

            Tab tlTab = tablayout.getTabAt(0);
            Tab trTab = tablayout.getTabAt(1);
            Tab mnTab = tablayout.getTabAt(2);
            if (tlTab != null && trTab != null && mnTab != null) {
                tlTab.setIcon(R.drawable.home);
                trTab.setIcon(R.drawable.hash);
                mnTab.setIcon(R.drawable.mention);
            }
            if (getIntent().getData() != null) {
                LinkContentLoader linkLoader = new LinkContentLoader(this);
                linkLoader.execute(getIntent().getData());
            }
        }
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, @Nullable Intent intent) {
        switch (reqCode) {
            case REQUEST_APP_LOGIN:
                if (returnCode == RESULT_CANCELED)
                    finish();
                break;

            case REQUEST_APP_SETTINGS:
                root.setBackgroundColor(settings.getBackgroundColor());
                tablayout.setSelectedTabIndicatorColor(settings.getHighlightColor());
                if (adapter != null) {
                    if (returnCode == RETURN_DB_CLEARED)
                        adapter.clearData();
                    else if (returnCode == RETURN_APP_LOGOUT)
                        adapter = null;
                    else
                        adapter.notifySettingsChanged();
                }
                break;
        }
        super.onActivityResult(reqCode, returnCode, intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.home, m);
        MenuItem search = m.findItem(R.id.action_search);
        SearchView searchQuery = (SearchView) search.getActionView();
        searchQuery.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent search = new Intent(MainActivity.this, SearchPage.class);
                search.putExtra(KEY_SEARCH_QUERY, s);
                startActivity(search);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        MenuItem profile = m.findItem(R.id.action_profile);
        MenuItem tweet = m.findItem(R.id.action_tweet);
        MenuItem search = m.findItem(R.id.action_search);
        MenuItem setting = m.findItem(R.id.action_settings);

        switch (tablayout.getSelectedTabPosition()) {
            case 0:
                profile.setVisible(true);
                search.setVisible(false);
                tweet.setVisible(true);
                setting.setVisible(false);
                search.collapseActionView();
                break;

            case 1:
                profile.setVisible(false);
                search.setVisible(true);
                tweet.setVisible(false);
                setting.setVisible(true);
                break;

            case 2:
                profile.setVisible(false);
                search.setVisible(false);
                tweet.setVisible(false);
                setting.setVisible(true);
                search.collapseActionView();
                break;
        }
        return super.onPrepareOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                Intent user = new Intent(this, UserProfile.class);
                user.putExtra(KEY_PROFILE_ID, settings.getUserId());
                startActivity(user);
                break;

            case R.id.action_tweet:
                Intent tweet = new Intent(this, TweetPopup.class);
                startActivity(tweet);
                break;

            case R.id.action_settings:
                Intent settings = new Intent(this, AppSettings.class);
                startActivityForResult(settings, REQUEST_APP_SETTINGS);
                break;
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
    public void onTabSelected(Tab tab) {
        invalidateOptionsMenu();
    }


    @Override
    public void onTabUnselected(Tab tab) {
        if (adapter != null)
            adapter.scrollToTop(tab.getPosition());
    }


    @Override
    public void onTabReselected(Tab tab) {
        if (adapter != null)
            adapter.scrollToTop(tab.getPosition());
    }


    public void setLoading(boolean enable) {
        if (enable)
            loadingCircle.show();
        else
            loadingCircle.dismiss();
    }


    public void onError(EngineException error) {
        ErrorHandler.handleFailure(this, error);
    }
}