package org.nuclearfog.twidda.window;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.MessageAdapter;
import org.nuclearfog.twidda.adapter.MessageAdapter.OnItemSelected;
import org.nuclearfog.twidda.backend.MessageLoader;
import org.nuclearfog.twidda.backend.MessageLoader.Mode;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.database.GlobalSettings;


public class DirectMessage extends AppCompatActivity implements OnRefreshListener, OnItemSelected {

    private MessageLoader messageAsync;
    private MessageAdapter mAdapter;
    private SwipeRefreshLayout messageRefresh;
    private GlobalSettings settings;
    private RecyclerView dmList;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_dm);

        Toolbar tool = findViewById(R.id.dm_toolbar);
        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.directmessage);
        messageRefresh = findViewById(R.id.dm_reload);
        dmList = findViewById(R.id.messagelist);
        View root = findViewById(R.id.dm_layout);

        settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
        messageRefresh.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());

        dmList.setLayoutManager(new LinearLayoutManager(this));
        dmList.setHasFixedSize(true);
        messageRefresh.setOnRefreshListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (messageAsync == null) {
            mAdapter = new MessageAdapter(this);
            mAdapter.setColor(settings.getFontColor(), settings.getHighlightColor());
            mAdapter.setImageLoad(settings.getImageLoad());
            dmList.setAdapter(mAdapter);
            messageAsync = new MessageLoader(this, Mode.LDR);
            messageAsync.execute();
        }
    }


    @Override
    protected void onStop() {
        if (messageAsync != null && messageAsync.getStatus() == Status.RUNNING) {
            messageAsync.cancel(true);
        }
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.message, m);
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (messageAsync != null && messageAsync.getStatus() != Status.RUNNING) {
            if (item.getItemId() == R.id.message) {
                Intent sendDm = new Intent(this, MessagePopup.class);
                startActivity(sendDm);
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onAnswer(int position) {
        if (!messageRefresh.isRefreshing()) {
            Message message = mAdapter.getData(position);
            Intent sendDm = new Intent(this, MessagePopup.class);
            sendDm.putExtra("username", message.getSender().getScreenname());
            startActivity(sendDm);
        }
    }


    @Override
    public void onDelete(int position) {
        if (!messageRefresh.isRefreshing()) {
            Message message = mAdapter.getData(position);
            final long messageId = message.getId();
            new Builder(this).setMessage(R.string.confirm_delete_dm)
                    .setNegativeButton(R.string.no_confirm, null)
                    .setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            messageAsync = new MessageLoader(DirectMessage.this, Mode.DEL);
                            messageAsync.execute(messageId);
                        }
                    }).show();
        }
    }


    @Override
    public void onProfileClick(int index) {
        if (!messageRefresh.isRefreshing()) {
            Message message = mAdapter.getData(index);
            long userId = message.getSender().getId();
            String username = message.getSender().getScreenname();
            Intent user = new Intent(this, UserProfile.class);
            user.putExtra("userID", userId);
            user.putExtra("username", username);
            startActivity(user);
        }
    }


    @Override
    public void onClick(String tag) {
        if (!messageRefresh.isRefreshing()) {
            Intent intent = new Intent(this, SearchPage.class);
            intent.putExtra("search", tag);
            startActivity(intent);
        }
    }


    @Override
    public void onRefresh() {
        messageAsync = new MessageLoader(this, Mode.GET);
        messageAsync.execute();
    }
}