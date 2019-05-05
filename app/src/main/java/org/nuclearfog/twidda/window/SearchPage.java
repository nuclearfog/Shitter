package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.SearchPagerAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;


public class SearchPage extends AppCompatActivity implements OnTabSelectedListener {

    private static final int[] icons = {R.drawable.search, R.drawable.user};

    private ViewPager pager;
    private String search;
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
        if (param != null) {
            if (BuildConfig.DEBUG && !param.containsKey("search"))
                throw new AssertionError();
            search = param.getString("search", "");
        }

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
        tab.setSelectedTabIndicatorColor(settings.getHighlightColor());

        SearchPagerAdapter adapter = new SearchPagerAdapter(getSupportFragmentManager(), search);
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
                intent.putExtra("search", s);
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
                intent.putExtra("Addition", search);
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
    }


    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }
}