package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.LinkContentLoader;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.view.Window.FEATURE_NO_TITLE;
import static org.nuclearfog.twidda.activity.SearchPage.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_ID;

/**
 * Main Activity of the App
 */
public class MainActivity extends AppCompatActivity implements OnTabSelectedListener, OnQueryTextListener {

    /**
     * Code returned from {@link AppSettings} when user clears the database
     */
    public static final int RETURN_DB_CLEARED = 1;

    /**
     * Code returned from {@link AppSettings} when user logs out from Twitter
     */
    public static final int RETURN_APP_LOGOUT = 2;

    /**
     * Request code for {@link LoginActivity}
     */
    private static final int REQUEST_APP_LOGIN = 1;

    /**
     * Request code for {@link AppSettings}
     */
    private static final int REQUEST_APP_SETTINGS = 2;

    private FragmentAdapter adapter;
    private GlobalSettings settings;

    // Views and dialogs
    private Dialog loadingCircle;
    private TabLayout tablayout;
    private ViewPager pager;
    private View root;


    static {
        // Enable vector drawable support for API 16 to 21
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
        adapter = new FragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        tablayout.addOnTabSelectedListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!settings.getLogin()) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, REQUEST_APP_LOGIN);
        } else if (adapter.isEmpty()) {
            adapter.setupForHomePage();
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
                if (returnCode == RESULT_CANCELED) {
                    finish();
                } else {
                    root.setBackgroundColor(settings.getBackgroundColor());
                    tablayout.setSelectedTabIndicatorColor(settings.getHighlightColor());
                    adapter.notifySettingsChanged();
                }
                break;

            case REQUEST_APP_SETTINGS:
                if (returnCode == RETURN_APP_LOGOUT) {
                    adapter.clear();
                    pager.setAdapter(adapter);
                } else {
                    root.setBackgroundColor(settings.getBackgroundColor());
                    tablayout.setSelectedTabIndicatorColor(settings.getHighlightColor());
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
        searchQuery.setOnQueryTextListener(this);
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
    public boolean onQueryTextSubmit(String s) {
        Intent search = new Intent(this, SearchPage.class);
        search.putExtra(KEY_SEARCH_QUERY, s);
        startActivity(search);
        return false;
    }


    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }


    @Override
    public void onTabSelected(Tab tab) {
        invalidateOptionsMenu();
    }


    @Override
    public void onTabUnselected(Tab tab) {
        adapter.scrollToTop(tab.getPosition());
    }


    @Override
    public void onTabReselected(Tab tab) {
        adapter.scrollToTop(tab.getPosition());
    }

    /**
     * called from {@link LinkContentLoader} to set progress circle
     */
    public void setLoading(boolean enable) {
        if (enable) {
            loadingCircle.show();
        } else {
            loadingCircle.dismiss();
        }
    }

    /**
     * called from {@link LinkContentLoader} when an error occurs
     */
    public void onError() {
        Toast.makeText(this, R.string.error_open_link, Toast.LENGTH_SHORT).show();
    }

    /**
     * set current tab
     *
     * @param page page number
     */
    public void setTab(@IntRange(from = 0, to = 2) int page) {
        pager.setCurrentItem(page);
    }
}