package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.*;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.backend.TwitterSearch;
import org.nuclearfog.twidda.viewadapter.UserRecycler;

/**
 * SearchPage Tweets and Users
 * @see TwitterSearch
 */
public class SearchPage extends AppCompatActivity implements UserRecycler.OnItemClicked,
        SwipeRefreshLayout.OnRefreshListener, TabHost.OnTabChangeListener, TimelineRecycler.OnItemClicked {

    private RecyclerView tweetSearch,userSearch;
    private SwipeRefreshLayout tweetReload;
    private TwitterSearch mSearch;
    private String search = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.search);
        getExtras(getIntent().getExtras());
        int background = ColorPreferences.getInstance(this).getColor(ColorPreferences.BACKGROUND);

        Toolbar tool = (Toolbar) findViewById(R.id.search_toolbar);
        tweetSearch  = (RecyclerView) findViewById(R.id.tweet_result);
        userSearch   = (RecyclerView) findViewById(R.id.user_result);
        tweetReload = (SwipeRefreshLayout) findViewById(R.id.searchtweets);
        tweetSearch.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        userSearch.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        tweetSearch.setBackgroundColor(background);
        userSearch.setBackgroundColor(background);

        setSupportActionBar(tool);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        TabHost tabhost = (TabHost)findViewById(R.id.search_tab);
        tabhost.setup();
        setTabs(tabhost);

        tabhost.setOnTabChangedListener(this);
        tweetReload.setOnRefreshListener(this);
        getContent();
    }

    @Override
    protected void onDestroy() {
        mSearch.cancel(true);
        super.onDestroy();
    }

    /**
     * Home Button
     */
    @Override
    protected void onUserLeaveHint(){
        super.onUserLeaveHint();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.search, m);
        final SearchView searchQuery = (SearchView)m.findItem(R.id.new_search).getActionView();
        searchQuery.setQueryHint(search);
        searchQuery.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search = s;
                searchQuery.setQueryHint(search);
                findViewById(R.id.search_progress).setVisibility(View.VISIBLE);
                tweetSearch.setAdapter(null);
                userSearch.setAdapter(null);
                getContent();
                return true;
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
    public void onItemClick(View view, ViewGroup parent, int position) {
        switch(parent.getId()) {
            case R.id.tweet_result:
                if(!tweetReload.isRefreshing()) {
                    TimelineRecycler tlAdp = (TimelineRecycler) tweetSearch.getAdapter();
                    Tweet tweet = tlAdp.getData().get(position);
                    long tweetID = tweet.tweetID;
                    long userID = tweet.user.userID;
                    String username = tweet.user.screenname;
                    Intent intent = new Intent(getApplicationContext(), TweetDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("tweetID",tweetID);
                    bundle.putLong("userID",userID);
                    bundle.putString("username", username);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.user_result:
                UserRecycler uAdp = (UserRecycler) userSearch.getAdapter();
                TwitterUser user = uAdp.getData().get(position);
                Intent profile = new Intent(getApplicationContext(), UserProfile.class);
                Bundle bundle = new Bundle();
                long userID = user.userID;
                String username = user.screenname;
                bundle.putLong("userID",userID);
                bundle.putString("username", username);
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
        tab1.setIndicator("", ContextCompat.getDrawable(getApplicationContext(),R.drawable.search));
        tabhost.addTab(tab1);
        TabHost.TabSpec tab2 = tabhost.newTabSpec("user_result");
        tab2.setContent(R.id.user_result);
        tab2.setIndicator("",ContextCompat.getDrawable(getApplicationContext(),R.drawable.user));
        tabhost.addTab(tab2);
    }

    private void getContent() {
        mSearch = new TwitterSearch(this);
        mSearch.execute(search);
    }


    @SuppressWarnings("ConstantConditions")
    private void getExtras(Bundle b) {
        search = b.getString("search");
    }
}