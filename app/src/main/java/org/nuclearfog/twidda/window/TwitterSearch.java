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

public class TwitterSearch extends AppCompatActivity {

    private String search;
    private ListView tweetSearch, userSearch;
    private SwipeRefreshLayout tweetReload,userReload;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.search);
        Toolbar tool = (Toolbar) findViewById(R.id.search_toolbar);
        tweetSearch  = (ListView) findViewById(R.id.tweet_result);
        userSearch   = (ListView) findViewById(R.id.user_result);
        tweetReload = (SwipeRefreshLayout) findViewById(R.id.searchtweets);
        userReload = (SwipeRefreshLayout) findViewById(R.id.searchusers);
        setSupportActionBar(tool);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        search = getIntent().getExtras().getString("search");
        setTabContent();
        setListener();
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

    private void setListener() {
        tweetSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            }
        });
        userSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            }
        });
        tweetReload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getContent(Search.TWEETS);
            }
        });
        userReload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getContent(Search.USERS);
            }
        });
    }

    private void setTabContent(){
        TabHost tabhost = (TabHost)findViewById(R.id.search_tab);
        tabhost.setup();

        TabHost.TabSpec tab1 = tabhost.newTabSpec("Tweets");
        tab1.setContent(R.id.searchtweets);
        tab1.setIndicator("",getResources().getDrawable(R.drawable.search_result));
        tabhost.addTab(tab1);

        TabHost.TabSpec tab2 = tabhost.newTabSpec("Tweets");
        tab2.setContent(R.id.searchusers);
        tab2.setIndicator("",getResources().getDrawable(R.drawable.user_result));
        tabhost.addTab(tab2);

        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                tweetReload.setRefreshing(false);
                userReload.setRefreshing(false);
            }
        });
    }

    private void getContent(final String MODE){
        Search s = new Search(TwitterSearch.this);
        s.execute(MODE,search);

    }
}