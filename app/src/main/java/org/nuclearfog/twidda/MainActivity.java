package org.nuclearfog.twidda;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;

import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.window.AppSettings;
import org.nuclearfog.twidda.window.LoginPage;
import org.nuclearfog.twidda.window.SearchPage;
import org.nuclearfog.twidda.window.TweetPopup;
import org.nuclearfog.twidda.window.UserProfile;

import static org.nuclearfog.twidda.window.SearchPage.KEY_SEARCH;
import static org.nuclearfog.twidda.window.UserProfile.KEY_PROFILE_ID;
import static org.nuclearfog.twidda.window.UserProfile.KEY_PROFILE_NAME;

/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    public static final int DB_CLEARED = 3;
    public static final int APP_LOGOUT = 4;
    private static final int LOGIN = 1;
    private static final int SETTING = 2;
    private static final int[] ICONS = {R.drawable.home, R.drawable.hash, R.drawable.mention};

    private GlobalSettings settings;
    private FragmentAdapter adapter;
    private TabLayout tab;
    private ViewPager pager;
    private int tabIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_main);
        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        pager = findViewById(R.id.home_pager);
        tab = findViewById(R.id.home_tab);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        settings = GlobalSettings.getInstance(this);
        adapter = new FragmentAdapter(getSupportFragmentManager());
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!settings.getLogin()) {
            Intent loginIntent = new Intent(this, LoginPage.class);
            startActivityForResult(loginIntent, LOGIN);
        } else if (pager.getAdapter() == null) {
            pager.setAdapter(adapter);
            pager.setOffscreenPageLimit(3);
            tab.setupWithViewPager(pager);
            tab.addOnTabSelectedListener(this);
            for (int i = 0; i < ICONS.length; i++) {
                Tab t = tab.getTabAt(i);
                if (t != null)
                    t.setIcon(ICONS[i]);
            }
            initView();
        }
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent intent) {
        switch (reqCode) {
            case LOGIN:
                if (returnCode == RESULT_CANCELED)
                    finish();
                break;

            case SETTING:
                if (returnCode == DB_CLEARED)
                    adapter.clearData();
                else
                    adapter.notifySettingsChanged();
                initView();
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
                search.putExtra(KEY_SEARCH, s);
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

        switch (tabIndex) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                long homeId = settings.getUserId();
                Intent user = new Intent(this, UserProfile.class);
                user.putExtra(KEY_PROFILE_ID, homeId);
                user.putExtra(KEY_PROFILE_NAME, "");
                startActivity(user);
                break;

            case R.id.action_tweet:
                Intent tweet = new Intent(this, TweetPopup.class);
                startActivity(tweet);
                break;

            case R.id.action_settings:
                Intent settings = new Intent(this, AppSettings.class);
                startActivityForResult(settings, SETTING);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (tabIndex == 0) {
            super.onBackPressed();
        } else {
            pager.setCurrentItem(0);
        }
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        tabIndex = tab.getPosition();
        invalidateOptionsMenu();
    }


    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        adapter.scrollToTop(tab.getPosition());
    }


    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }


    private void initView() {
        View root = findViewById(R.id.main_layout);
        root.setBackgroundColor(settings.getBackgroundColor());
        tab.setSelectedTabIndicatorColor(settings.getHighlightColor());
    }
}