package org.nuclearfog.twidda.window;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.GlobalSettings;
import org.nuclearfog.twidda.backend.TwitterSearch;
import org.nuclearfog.twidda.backend.TwitterSearch.OnDismiss;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler.OnItemClicked;
import org.nuclearfog.twidda.viewadapter.UserRecycler;

public class SearchPage extends AppCompatActivity implements UserRecycler.OnItemClicked,
        OnRefreshListener, OnTabChangeListener, OnItemClicked, OnDismiss {

    private RecyclerView tweetSearch,userSearch;
    private SwipeRefreshLayout tweetReload;
    private TwitterSearch mSearch;
    private TabHost tabhost;
    private Dialog popup;
    private View lastView;
    private String search = "";
    private int tabIndex = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        b = getIntent().getExtras();
        if (b != null) {
            search = b.getString("search");
        }
        setContentView(R.layout.searchpage);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        int background = settings.getBackgroundColor();

        Toolbar tool = findViewById(R.id.search_toolbar);
        setSupportActionBar(tool);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        popup = new Dialog(this);
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
    protected void onPause() {
        if (mSearch != null && !mSearch.isCancelled()) {
            mSearch.cancel(true);
            tweetReload.setRefreshing(false);
        }
        if (popup.isShowing())
            popup.dismiss();
        super.onPause();
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
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.search, m);
        final SearchView searchQuery = (SearchView)m.findItem(R.id.new_search).getActionView();
        searchQuery.setQueryHint(search);
        searchQuery.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent intent = new Intent(getApplicationContext(), SearchPage.class);
                intent.putExtra("search", s);
                startActivity(intent);
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
                intent.putExtra("TweetID", -1);
            if(search.startsWith("#")) {
                intent.putExtra("Addition", search);
            }
            startActivity(intent);
            break;
        }
        return true;
    }

    @Override
    public void onItemClick(ViewGroup parent, int position) {
        Intent intent;
        switch(parent.getId()) {
            case R.id.tweet_result:
                if(!tweetReload.isRefreshing()) {
                    TimelineRecycler tweetAdapter = (TimelineRecycler) tweetSearch.getAdapter();
                    if (tweetAdapter != null) {
                        Tweet tweet = tweetAdapter.getData().get(position);

                        intent = new Intent(this, TweetDetail.class);
                        intent.putExtra("tweetID", tweet.tweetID);
                        intent.putExtra("userID", tweet.user.userID);
                        intent.putExtra("username", tweet.user.screenname);
                        startActivity(intent);
                    }
                }
                break;

            case R.id.user_result:
                UserRecycler userAdapter = (UserRecycler) userSearch.getAdapter();
                if (userAdapter != null) {
                    TwitterUser user = userAdapter.getData().get(position);

                    intent = new Intent(getApplicationContext(), UserProfile.class);
                    intent.putExtra("userID", user.userID);
                    intent.putExtra("username", user.screenname);
                    startActivity(intent);
                }
                break;
        }
    }

    @Override
    public void onRefresh() {
        mSearch = new TwitterSearch(this);
        mSearch.execute(search);
    }

    @Override
    public void onTabChanged(String tabId) {
        animate();
        tabIndex = tabhost.getCurrentTab();
    }

    private void setTabs(TabHost tabhost) {
        TabHost.TabSpec tab1 = tabhost.newTabSpec("search_result");
        tab1.setContent(R.id.searchtweets);
        tab1.setIndicator("", getDrawable(R.drawable.search));
        tabhost.addTab(tab1);

        TabHost.TabSpec tab2 = tabhost.newTabSpec("user_result");
        tab2.setContent(R.id.user_result);
        tab2.setIndicator("", getDrawable(R.drawable.user));
        tabhost.addTab(tab2);
        lastView = tabhost.getCurrentView();
    }

    private void animate() {
        final int ANIM_DUR = 300;
        final int DIMENS = Animation.RELATIVE_TO_PARENT;

        Animation leftIn = new TranslateAnimation(DIMENS,-1.0f,DIMENS,0.0f,DIMENS,0.0f,DIMENS,0.0f);
        Animation rightIn = new TranslateAnimation(DIMENS,1.0f,DIMENS,0.0f,DIMENS,0.0f,DIMENS,0.0f);
        Animation leftOut = new TranslateAnimation(DIMENS, 0.0f, DIMENS, -1.0f, DIMENS, 0.0f, DIMENS, 0.0f);
        Animation rightOut = new TranslateAnimation(DIMENS, 0.0f, DIMENS, 1.0f, DIMENS, 0.0f, DIMENS, 0.0f);
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

    @SuppressLint("InflateParams")
    private void getContent() {
        mSearch = new TwitterSearch(this);
        mSearch.execute(search);

        popup.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popup.setCanceledOnTouchOutside(false);
        if (popup.getWindow() != null)
            popup.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View load = getLayoutInflater().inflate(R.layout.item_load, null, false);
        View cancelButton = load.findViewById(R.id.kill_button);
        popup.setContentView(load);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearch != null && !mSearch.isCancelled())
                    mSearch.cancel(true);
                popup.dismiss();
            }
        });
        popup.show();
    }

    @Override
    public void dismiss() {
        if (popup != null)
            popup.dismiss();
    }
}