package org.nuclearfog.twidda.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.adapter.FragmentAdapter.AdapterType;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.TweetPopup.KEY_TWEETPOPUP_PREFIX;

/**
 * Twitter search Activity
 */
public class SearchPage extends AppCompatActivity implements OnTabSelectedListener {

    public static final String KEY_SEARCH_QUERY = "search_query";

    private FragmentAdapter adapter;
    private ViewPager pager;
    private String search = "";
    private int tabIndex = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_search);
        Toolbar tool = findViewById(R.id.search_toolbar);
        TabLayout tablayout = findViewById(R.id.search_tab);
        View root = findViewById(R.id.search_layout);
        pager = findViewById(R.id.search_pager);

        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bundle param = getIntent().getExtras();
        Uri link = getIntent().getData();
        if (param != null && param.containsKey(KEY_SEARCH_QUERY)) {
            search = param.getString(KEY_SEARCH_QUERY);
        } else if (link != null) {
            getSearchString(link);
        }

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
        tablayout.setSelectedTabIndicatorColor(settings.getHighlightColor());
        tablayout.setupWithViewPager(pager);
        tablayout.addOnTabSelectedListener(this);

        adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.SEARCH_TAB, 0, search);
        pager.setAdapter(adapter);

        Tab twtSearch = tablayout.getTabAt(0);
        Tab usrSearch = tablayout.getTabAt(1);
        if (twtSearch != null && usrSearch != null) {
            twtSearch.setIcon(R.drawable.search);
            usrSearch.setIcon(R.drawable.user);
        }
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
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.search, m);
        MenuItem mSearch = m.findItem(R.id.new_search);
        SearchView searchQuery = (SearchView) mSearch.getActionView();
        searchQuery.setQueryHint(search);
        searchQuery.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent intent = new Intent(SearchPage.this, SearchPage.class);
                intent.putExtra(KEY_SEARCH_QUERY, s);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search_tweet) {
            Intent intent = new Intent(this, TweetPopup.class);
            if (search.startsWith("#"))
                intent.putExtra(KEY_TWEETPOPUP_PREFIX, search);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onTabSelected(Tab tab) {
        tabIndex = tab.getPosition();
        invalidateOptionsMenu();
    }


    @Override
    public void onTabUnselected(Tab tab) {
        adapter.scrollToTop(tab.getPosition());
    }


    @Override
    public void onTabReselected(Tab tab) {
    }


    /**
     * get search string from twitter link
     *
     * @param link twitter link
     */
    private void getSearchString(@NonNull Uri link) {
        String path = link.getPath();
        String query = link.getQuery();
        GlobalSettings settings = GlobalSettings.getInstance(this);

        if (path != null) {
            if (path.startsWith("/hashtag/")) {
                int end = path.indexOf('&');
                if (end > 9)
                    search = '#' + path.substring(9, end);
                else
                    search = '#' + path.substring(9);
            } else if (path.startsWith("/search")) {
                if (query != null && query.length() > 2) {
                    int end = query.indexOf('&');
                    if (end > 2)
                        search = query.substring(2, end).replace('+', ' ');
                    else
                        search = query.substring(2).replace('+', ' ');
                }
            }
        }
        if (search.isEmpty() || !settings.getLogin()) {
            Toast.makeText(this, R.string.error_open_link, LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}