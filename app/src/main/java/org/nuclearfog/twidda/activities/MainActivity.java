package org.nuclearfog.twidda.activities;

import static org.nuclearfog.twidda.activities.AccountActivity.RET_ACCOUNT_CHANGE;
import static org.nuclearfog.twidda.activities.SearchPage.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.activities.UserProfile.KEY_PROFILE_ID;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
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
import org.nuclearfog.twidda.backend.LinkLoader;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.dialog.ProgressDialog;

/**
 * Main Activity of the App
 *
 * @author nuclearfog
 */
public class MainActivity extends AppCompatActivity implements OnTabSelectedListener, OnQueryTextListener {

    /**
     * bundle key used to set the page
     */
    public static final String KEY_TAB_PAGE = "tab_pos";

    /**
     * Code returned from {@link AppSettings} when user clears the database
     */
    public static final int RETURN_DB_CLEARED = 0x95BEA792;

    /**
     * Code returned from {@link AppSettings} when user logs out from Twitter
     */
    public static final int RETURN_APP_LOGOUT = 0x4029DFEE;

    /**
     * Request code for {@link LoginActivity}
     */
    private static final int REQUEST_APP_LOGIN = 0x6A89;

    /**
     * Request code for {@link AppSettings}
     */
    private static final int REQUEST_APP_SETTINGS = 0x54AD;

    private FragmentAdapter adapter;
    private GlobalSettings settings;

    // Views and dialogs
    private Dialog loadingCircle;
    private TabLayout tabLayout;
    private ViewPager pager;
    private ViewGroup root;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppStyles.setFontScale(newBase));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_main);
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        pager = findViewById(R.id.home_pager);
        tabLayout = findViewById(R.id.home_tab);
        root = findViewById(R.id.main_layout);
        loadingCircle = new ProgressDialog(this, null);

        settings = GlobalSettings.getInstance(this);
        tabLayout.setupWithViewPager(pager);
        pager.setOffscreenPageLimit(3);
        adapter = new FragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        AppStyles.setTheme(root, settings.getBackgroundColor());

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        tabLayout.addOnTabSelectedListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!settings.isLoggedIn()) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, REQUEST_APP_LOGIN);
        } else if (adapter.isEmpty()) {
            adapter.setupForHomePage();
            AppStyles.setTabIcons(tabLayout, settings, R.array.home_tab_icons);
            if (getIntent().getData() != null) {
                LinkLoader linkLoader = new LinkLoader(this);
                linkLoader.execute(getIntent().getData());
                loadingCircle.show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        loadingCircle.dismiss();
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, @Nullable Intent intent) {
        switch (reqCode) {
            case REQUEST_APP_LOGIN:
                AppStyles.setTheme(root, settings.getBackgroundColor());
                if (returnCode == RESULT_CANCELED) {
                    finish();
                } else if (returnCode == RET_ACCOUNT_CHANGE) {
                    adapter.notifySettingsChanged();
                } else {
                    adapter.notifySettingsChanged();
                }
                break;

            case REQUEST_APP_SETTINGS:
                AppStyles.setTheme(root, settings.getBackgroundColor());
                AppStyles.setTabIcons(tabLayout, settings, R.array.home_tab_icons);
                if (returnCode == RETURN_APP_LOGOUT) {
                    adapter.clear();
                    pager.setAdapter(adapter);
                } else {
                    adapter.notifySettingsChanged();
                }
                break;
        }
        super.onActivityResult(reqCode, returnCode, intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.home, m);
        AppStyles.setMenuIconColor(m, settings.getIconColor());
        MenuItem search = m.findItem(R.id.action_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        MenuItem profile = m.findItem(R.id.action_profile);
        MenuItem tweet = m.findItem(R.id.action_tweet);
        MenuItem search = m.findItem(R.id.action_search);
        MenuItem setting = m.findItem(R.id.action_settings);
        MenuItem account = m.findItem(R.id.action_account);

        switch (tabLayout.getSelectedTabPosition()) {
            case 0:
                profile.setVisible(true);
                search.setVisible(false);
                tweet.setVisible(true);
                setting.setVisible(false);
                account.setVisible(false);
                search.collapseActionView();
                break;

            case 1:
                profile.setVisible(false);
                search.setVisible(true);
                tweet.setVisible(false);
                setting.setVisible(true);
                account.setVisible(false);
                break;

            case 2:
                profile.setVisible(false);
                search.setVisible(false);
                tweet.setVisible(false);
                setting.setVisible(true);
                account.setVisible(true);
                search.collapseActionView();
                break;
        }
        return super.onPrepareOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // open home profile
        if (item.getItemId() == R.id.action_profile) {
            Intent user = new Intent(this, UserProfile.class);
            user.putExtra(KEY_PROFILE_ID, settings.getCurrentUserId());
            startActivity(user);
        }
        // open tweet editor
        else if (item.getItemId() == R.id.action_tweet) {
            Intent tweet = new Intent(this, TweetEditor.class);
            startActivity(tweet);
        }
        // open app settings
        else if (item.getItemId() == R.id.action_settings) {
            Intent settings = new Intent(this, AppSettings.class);
            startActivityForResult(settings, REQUEST_APP_SETTINGS);
        }
        // theme expanded search view
        else if (item.getItemId() == R.id.action_search) {
            SearchView searchView = (SearchView) item.getActionView();
            AppStyles.setTheme(searchView, Color.TRANSPARENT);
        }
        // open account manager
        else if (item.getItemId() == R.id.action_account) {
            Intent accountManager = new Intent(this, AccountActivity.class);
            startActivityForResult(accountManager, REQUEST_APP_LOGIN);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (tabLayout.getSelectedTabPosition() > 0) {
            pager.setCurrentItem(0);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onQueryTextSubmit(String s) {
        if (s.length() <= SearchPage.SEARCH_STR_MAX_LEN && !s.contains(":") && !s.contains("$")) {
            Intent search = new Intent(this, SearchPage.class);
            search.putExtra(KEY_SEARCH_QUERY, s);
            startActivity(search);
        } else {
            Toast.makeText(this, R.string.error_twitter_search, Toast.LENGTH_SHORT).show();
        }
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
     * called from {@link LinkLoader} when link information were successfully loaded
     *
     * @param holder holder with activity information and extras
     */
    public void onSuccess(@Nullable LinkLoader.DataHolder holder) {
        loadingCircle.dismiss();
        if (holder != null) {
            if (holder.activity == MainActivity.class) {
                int page = holder.data.getInt(KEY_TAB_PAGE, 0);
                pager.setCurrentItem(page);
            } else {
                Intent intent = new Intent(this, holder.activity);
                intent.putExtras(holder.data);
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, R.string.info_link_open_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * called from {@link LinkLoader} when an error occurs
     */
    public void onError(ErrorHandler.TwitterError error) {
        ErrorHandler.handleFailure(this, error);
        loadingCircle.dismiss();
    }
}