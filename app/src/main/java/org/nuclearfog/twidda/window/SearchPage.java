package org.nuclearfog.twidda.window;

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

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.adapter.FragmentAdapter.AdapterType;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.window.TweetPopup.KEY_TWEETPOPUP_ADDITION;


public class SearchPage extends AppCompatActivity implements OnTabSelectedListener {

    public static final String KEY_SEARCH = "search";
    private static final int[] icons = {R.drawable.search, R.drawable.user};

    private FragmentAdapter adapter;
    private ViewPager pager;
    private GlobalSettings settings;
    private String search = "";
    private int tabIndex = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_search);

        Toolbar tool = findViewById(R.id.search_toolbar);
        TabLayout tab = findViewById(R.id.search_tab);
        View root = findViewById(R.id.search_layout);
        pager = findViewById(R.id.search_pager);

        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bundle param = getIntent().getExtras();
        Uri link = getIntent().getData();
        settings = GlobalSettings.getInstance(this);

        if (param != null && param.containsKey(KEY_SEARCH)) {
            search = param.getString(KEY_SEARCH);
        } else if (link != null) {
            getSearchString(link);
        } else if (BuildConfig.DEBUG)
            throw new AssertionError();

        root.setBackgroundColor(settings.getBackgroundColor());
        tab.setSelectedTabIndicatorColor(settings.getHighlightColor());

        adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.SEARCH_TAB, 0, search);
        tab.setupWithViewPager(pager);
        tab.addOnTabSelectedListener(this);
        pager.setAdapter(adapter);

        for (int i = 0; i < icons.length; i++) {
            TabLayout.Tab t = tab.getTabAt(i);
            if (t != null)
                t.setIcon(icons[i]);
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
                intent.putExtra(KEY_SEARCH, s);
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
                intent.putExtra(KEY_TWEETPOPUP_ADDITION, search);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
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


    private void getSearchString(@NonNull Uri link) {
        String path = link.getPath();
        String query = link.getQuery();

        if (path != null) {
            if (path.startsWith("/hashtag/")) {
                search = '#' + path.substring(9);
            } else if (path.startsWith("/search")) {
                if (query != null && query.length() > 2) {
                    search = query.substring(2).replace('+', ' ');
                }
            }
        }
        if (search.isEmpty() || !settings.getLogin()) {
            Toast.makeText(this, R.string.failed_open_link, LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}