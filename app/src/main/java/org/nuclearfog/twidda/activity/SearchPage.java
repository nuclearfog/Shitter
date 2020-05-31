package org.nuclearfog.twidda.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;

import static org.nuclearfog.twidda.activity.TweetPopup.KEY_TWEETPOPUP_PREFIX;

/**
 * Twitter search Activity
 */
public class SearchPage extends AppCompatActivity implements OnTabSelectedListener {

    public static final String KEY_SEARCH_QUERY = "search_query";

    private FragmentAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager pager;

    private String search = "";
    private int tabIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_search);
        View root = findViewById(R.id.search_layout);
        Toolbar tool = findViewById(R.id.search_toolbar);
        tabLayout = findViewById(R.id.search_tab);
        pager = findViewById(R.id.search_pager);

        tool.setTitle("");
        setSupportActionBar(tool);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
        tabLayout.setSelectedTabIndicatorColor(settings.getHighlightColor());
        tabLayout.setupWithViewPager(pager);
        tabLayout.addOnTabSelectedListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Bundle param = getIntent().getExtras();
        if (adapter == null && param != null && param.containsKey(KEY_SEARCH_QUERY)) {
            search = param.getString(KEY_SEARCH_QUERY);
            adapter = new FragmentAdapter(getSupportFragmentManager());
            adapter.setupSearchPage(search);
            pager.setAdapter(adapter);

            Tab twtSearch = tabLayout.getTabAt(0);
            Tab usrSearch = tabLayout.getTabAt(1);
            if (twtSearch != null && usrSearch != null) {
                twtSearch.setIcon(R.drawable.search);
                usrSearch.setIcon(R.drawable.user);
            }
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
        if (adapter != null)
            adapter.scrollToTop(tab.getPosition());
    }


    @Override
    public void onTabReselected(Tab tab) {
        if (adapter != null)
            adapter.scrollToTop(tab.getPosition());
    }
}