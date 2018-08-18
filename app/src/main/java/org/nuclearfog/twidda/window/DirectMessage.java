package org.nuclearfog.twidda.window;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.MessageLoader;
import org.nuclearfog.twidda.viewadapter.MessageAdapter.OnItemSelected;

public class DirectMessage extends AppCompatActivity implements OnItemSelected, OnRefreshListener {

    private RecyclerView dmList;
    private MessageLoader mLoader;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        setContentView(R.layout.messagepage);

        dmList = findViewById(R.id.messagelist);
        Toolbar tool = findViewById(R.id.dm_toolbar);
        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.directmessage);
        SwipeRefreshLayout refresh = findViewById(R.id.dm_reload);

        dmList = findViewById(R.id.messagelist);
        dmList.setLayoutManager(new LinearLayoutManager(this));

        refresh.setRefreshing(true);
        refresh.setOnRefreshListener(this);
        loadContent();
    }

    @Override
    protected void onPause() {
        if (mLoader != null && !mLoader.isCancelled())
            mLoader.cancel(true);
        super.onPause();
    }


    private void loadContent() {
        mLoader = new MessageLoader(this);
        mLoader.execute();
    }

    @Override
    public void onSeleted(int index) {
    }

    @Override
    public void onRefresh() {
        loadContent();
    }
}