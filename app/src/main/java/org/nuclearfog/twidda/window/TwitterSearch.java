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

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.Search;
import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;

public class TwitterSearch extends AppCompatActivity {

    private String search;
    private ListView searchTL;
    private SwipeRefreshLayout search_refresh;

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.search);
        Toolbar tool = (Toolbar) findViewById(R.id.search_toolbar);
        searchTL = (ListView) findViewById(R.id.search_result);
        search_refresh = (SwipeRefreshLayout) findViewById(R.id.search_refresh);
        setSupportActionBar(tool);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        search = getIntent().getExtras().getString("search");
        getContent();
        setListener();
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
        searchTL.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!search_refresh.isRefreshing()) {
                    TimelineAdapter tlAdp = (TimelineAdapter) searchTL.getAdapter();
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
        search_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getContent();
            }
        });
    }

    private void getContent() {
        Search s = new Search(this);
        s.execute("tweet",search);
    }
}