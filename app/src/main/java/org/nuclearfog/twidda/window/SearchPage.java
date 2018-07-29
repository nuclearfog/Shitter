package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.GlobalSettings;
import org.nuclearfog.twidda.backend.TwitterSearch;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler.OnItemClicked;
import org.nuclearfog.twidda.viewadapter.UserRecycler;


public class SearchPage extends AppCompatActivity implements UserRecycler.OnItemClicked,
        OnRefreshListener, OnTabChangeListener, OnItemClicked {

    private RecyclerView tweetSearch,userSearch;
    private SwipeRefreshLayout tweetReload;
    private TwitterSearch mSearch;
    private TabHost tabhost;
    private View lastView;
    private String search = "";
    private int tabIndex = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.searchpage);
        getExtras(getIntent().getExtras());

        GlobalSettings settings = GlobalSettings.getInstance(this);
        int background = settings.getBackgroundColor();

        Toolbar tool = findViewById(R.id.search_toolbar);
        setSupportActionBar(tool);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        tweetSearch = findViewById(R.id.tweet_result);
        tweetSearch.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        tweetSearch.setBackgroundColor(background);

        userSearch = findViewById(R.id.user_result);
        userSearch.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        userSearch.setBackgroundColor(background);

        tweetReload = findViewById(R.id.searchtweets);

        tabhost = findViewById(R.id.search_tab);
        tabhost.setup();
        setTabs(tabhost);

        tabhost.setOnTabChangedListener(this);
        tweetReload.setOnRefreshListener(this);
        getContent();
    }


    @Override
    public void onBackPressed() {
        if(tabIndex == 1) {
            tabhost.setCurrentTab(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        if(mSearch != null)
            mSearch.cancel(true);
        super.onPause();
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
        animate();
        tabIndex = tabhost.getCurrentTab();
        if(tabIndex == 1) {
            tweetReload.setRefreshing(false);
        }
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
        lastView = tabhost.getCurrentView();
    }

    private void animate() {
        final int ANIM_DUR = 300;
        final int DIMENS = Animation.RELATIVE_TO_PARENT;

        Animation leftIn = new TranslateAnimation(DIMENS,-1.0f,DIMENS,0.0f,DIMENS,0.0f,DIMENS,0.0f);
        Animation rightIn = new TranslateAnimation(DIMENS,1.0f,DIMENS,0.0f,DIMENS,0.0f,DIMENS,0.0f);
        Animation leftOut = new TranslateAnimation(DIMENS,0.0f,DIMENS, -1.0f,DIMENS, 0.0f,DIMENS,0.0f);
        Animation rightOut = new TranslateAnimation(DIMENS,0.0f,DIMENS, 1.0f,DIMENS, 0.0f,DIMENS,0.0f);
        leftIn.setDuration(ANIM_DUR);
        rightIn.setDuration(ANIM_DUR);
        leftOut.setDuration(ANIM_DUR);
        rightOut.setDuration(ANIM_DUR);

        View currentView = tabhost.getCurrentView();

        if( tabhost.getCurrentTab() > tabIndex ) {
            lastView.setAnimation(leftOut);
            currentView.setAnimation(rightIn);
        } else {
            lastView.setAnimation(rightOut);
            currentView.setAnimation(leftIn);
        }
        lastView = tabhost.getCurrentView();
    }

    private void getContent() {
        mSearch = new TwitterSearch(this);
        mSearch.execute(search);
    }

    private void getExtras(@Nullable Bundle b) {
        if(b != null) {
        search = b.getString("search");}
    }
}