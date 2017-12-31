package org.nuclearfog.twidda.Window;

import android.content.Context;
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

import org.nuclearfog.twidda.Backend.Search;
import org.nuclearfog.twidda.R;


public class TwitterSearch extends AppCompatActivity {

    private String search;
    private Context c;
    private ListView searchTL;
    private SwipeRefreshLayout search_refresh;

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.search);
        Toolbar tool = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(tool);
        c = getApplicationContext();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        search = getIntent().getExtras().getString("search");
        searchTL = (ListView) findViewById(R.id.search_result);
        search_refresh = (SwipeRefreshLayout) findViewById(R.id.search_refresh);
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
            case R.id.action_tweet:
            intent = new Intent(this, TweetPopup.class);
            startActivity(intent);
            break;
        }
        return true;
    }

    private void setListener(){
        searchTL.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO
            }
        });
    }

    private void getContent() {
        Search s = new Search(this);
        s.execute("tweet",search);
    }
}