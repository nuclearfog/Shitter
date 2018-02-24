package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.database.UserDatabase;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.viewadapter.UserAdapter;
import org.nuclearfog.twidda.backend.TwitterSearch;

/**
 * SearchWindow Tweets and Users
 * @see TwitterSearch
 */
public class SearchWindow extends AppCompatActivity implements AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, TabHost.OnTabChangeListener {

    private ListView tweetSearch, userSearch;
    private SwipeRefreshLayout tweetReload;
    private TwitterSearch mSearch;
    private String search = "";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.search);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        search = getIntent().getExtras().getString("search");

        Toolbar tool = (Toolbar) findViewById(R.id.search_toolbar);
        tweetSearch  = (ListView) findViewById(R.id.tweet_result);
        userSearch   = (ListView) findViewById(R.id.user_result);
        tweetReload = (SwipeRefreshLayout) findViewById(R.id.searchtweets);
        setSupportActionBar(tool);
        TabHost tabhost = (TabHost)findViewById(R.id.search_tab);
        tabhost.setup();
        setTabs(tabhost);

        tabhost.setOnTabChangedListener(this);
        tweetSearch.setOnItemClickListener(this);
        userSearch.setOnItemClickListener(this);
        tweetReload.setOnRefreshListener(this);

        getContent();
    }

    @Override
    protected void onDestroy() {
        mSearch.cancel(true);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.search, m);
        SearchView searchQuery = (SearchView)m.findItem(R.id.new_search).getActionView();
        searchQuery.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search = s;
                ProgressBar mCircle = (ProgressBar)findViewById(R.id.search_progress);
                mCircle.setVisibility(View.VISIBLE);
                tweetSearch.setAdapter(null);
                getContent();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        int id = item.getItemId();
        switch(id) {
            case R.id.search_tweet:
            intent = new Intent(this, TweetPopup.class);
            Bundle b = new Bundle();
            b.putLong("TweetID", -1);
            if(search.startsWith("#")) {
                b.putString("Addition", search);
            }
            intent.putExtras(b);
            startActivity(intent);
            break;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch(parent.getId()) {
            case R.id.tweet_result:
                if(!tweetReload.isRefreshing()) {
                    TimelineAdapter tlAdp = (TimelineAdapter) tweetSearch.getAdapter();
                    TweetDatabase twDB = tlAdp.getData();
                    long tweetID = twDB.getTweetId(position);
                    long userID = twDB.getUserID(position);
                    Intent intent = new Intent(getApplicationContext(), TweetDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("tweetID",tweetID);
                    bundle.putLong("userID",userID);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.user_result:
                UserAdapter uAdp = (UserAdapter) userSearch.getAdapter();
                UserDatabase uDb = uAdp.getData();
                Intent profile = new Intent(getApplicationContext(), UserProfile.class);
                Bundle bundle = new Bundle();
                long userID = uDb.getUserID(position);
                bundle.putLong("userID",userID);
                profile.putExtras(bundle);
                startActivity(profile);
                break;
        }
    }

    @Override
    public void onRefresh() {
        getContent();
    }

    @Override
    public void onTabChanged(String tabId) {
        if(tabId.equals("user_result"))
            tweetReload.setRefreshing(false);
    }

    private void setTabs(TabHost tabhost) {
        TabHost.TabSpec tab1 = tabhost.newTabSpec("search_result");
        tab1.setContent(R.id.searchtweets);
        tab1.setIndicator("",getResources().getDrawable(R.drawable.search));
        tabhost.addTab(tab1);

        TabHost.TabSpec tab2 = tabhost.newTabSpec("user_result");
        tab2.setContent(R.id.user_result);
        tab2.setIndicator("",getResources().getDrawable(R.drawable.user));
        tabhost.addTab(tab2);
    }

    private void getContent() {
        mSearch = new TwitterSearch(this);
        mSearch.execute(search);
    }
}