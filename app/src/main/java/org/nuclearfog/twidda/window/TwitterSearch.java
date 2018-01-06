package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.Search;
import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.database.UserDatabase;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.viewadapter.UserAdapter;

public class TwitterSearch extends AppCompatActivity implements AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, TabHost.OnTabChangeListener {

    private String search;
    private ListView tweetSearch, userSearch;
    private SwipeRefreshLayout tweetReload,userReload;
    private String currentTab = "search_result";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.search);
        search = getIntent().getExtras().getString("search");

        Toolbar tool = (Toolbar) findViewById(R.id.search_toolbar);
        tweetSearch  = (ListView) findViewById(R.id.tweet_result);
        userSearch   = (ListView) findViewById(R.id.user_result);
        tweetReload = (SwipeRefreshLayout) findViewById(R.id.searchtweets);
        userReload = (SwipeRefreshLayout) findViewById(R.id.searchusers);
        TabHost tabhost = (TabHost)findViewById(R.id.search_tab);
        tabhost.setup();
        setSupportActionBar(tool);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TabHost.TabSpec tab1 = tabhost.newTabSpec("search_result");
        tab1.setContent(R.id.searchtweets);
        tab1.setIndicator("",getResources().getDrawable(R.drawable.search_result));
        tabhost.addTab(tab1);

        TabHost.TabSpec tab2 = tabhost.newTabSpec("user_result");
        tab2.setContent(R.id.searchusers);
        tab2.setIndicator("",getResources().getDrawable(R.drawable.user_result));
        tabhost.addTab(tab2);

        tabhost.setOnTabChangedListener(this);
        tweetSearch.setOnItemClickListener(this);
        userSearch.setOnItemClickListener(this);
        tweetReload.setOnRefreshListener(this);
        userReload.setOnRefreshListener(this);

        getContent(Search.TWEETS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.search, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        int id = item.getItemId();
        switch(id) {
            case R.id.search_tweet:
            intent = new Intent(this, TweetPopup.class);
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
                    TweetDatabase twDB = tlAdp.getAdapter();
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
                if(!userReload.isRefreshing()) {
                    UserAdapter uAdp = (UserAdapter) userSearch.getAdapter();
                    UserDatabase uDb = uAdp.getAdapter();
                    Intent profile = new Intent(getApplicationContext(), UserProfile.class);
                    Bundle bundle = new Bundle();
                    long userID = uDb.getUserID(position);
                    bundle.putLong("userID",userID);
                    profile.putExtras(bundle);
                    startActivity(profile);
                }
                break;
        }
    }

    @Override
    public void onRefresh() {
        switch(currentTab){
            case "search_result":
                getContent(Search.TWEETS);
                break;
            case "user_result":
                getContent(Search.USERS);
                break;
        }
    }

    @Override
    public void onTabChanged(String tabId) {
        tweetReload.setRefreshing(false);
        userReload.setRefreshing(false);
        currentTab = tabId;
    }

    private void getContent(final String MODE) {
        Search s = new Search(TwitterSearch.this);
        s.execute(MODE,search);
    }
}