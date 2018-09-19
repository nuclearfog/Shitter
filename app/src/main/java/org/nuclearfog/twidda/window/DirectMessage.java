package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.MessageLoader;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.viewadapter.MessageAdapter;
import org.nuclearfog.twidda.viewadapter.MessageAdapter.OnItemSelected;

import static android.os.AsyncTask.Status.RUNNING;

/**
 * Direct Message page
 *
 * @see MessageLoader
 */
public class DirectMessage extends AppCompatActivity implements OnItemSelected, OnRefreshListener {

    private MessageLoader mLoader;
    private SwipeRefreshLayout refresh;
    private RecyclerView dmList;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_dm);

        Toolbar tool = findViewById(R.id.dm_toolbar);
        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.directmessage);
        refresh = findViewById(R.id.dm_reload);
        dmList = findViewById(R.id.messagelist);
        View root = findViewById(R.id.dm_layout);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());

        dmList.setLayoutManager(new LinearLayoutManager(this));
        dmList.setHasFixedSize(true);
        refresh.setOnRefreshListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mLoader == null) {
            refresh.setRefreshing(true);
            mLoader = new MessageLoader(this);
            mLoader.execute();
        }
    }


    @Override
    protected void onStop() {
        if (mLoader != null && mLoader.getStatus() == RUNNING)
            mLoader.cancel(true);
        refresh.setRefreshing(false);
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.message, m);
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mLoader != null && mLoader.getStatus() != RUNNING) {
            switch (item.getItemId()) {
                case R.id.message:
                    Intent sendDm = new Intent(this, MessagePopup.class);
                    startActivity(sendDm);
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSelected(int index) {
        MessageAdapter mAdapter = (MessageAdapter) dmList.getAdapter();
        if (mAdapter != null && !refresh.isRefreshing()) {
            TwitterUser sender = mAdapter.getData().get(index).sender;
            Intent sendDm = new Intent(this, MessagePopup.class);
            sendDm.putExtra("username", sender.screenname);
            startActivity(sendDm);
        }
    }


    @Override
    public void onRefresh() {
        mLoader = new MessageLoader(this);
        mLoader.execute();
    }
}