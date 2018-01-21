package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.nuclearfog.twidda.backend.UserLists;
import org.nuclearfog.twidda.database.UserDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.UserAdapter;

/**
 * Get Follow Connections from an User
 * @see UserLists
 */
public class UserDetail extends AppCompatActivity implements AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private long userID, tweetID;
    private long mode;
    private ListView userListview;
    private SwipeRefreshLayout reload;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.follow);
        Intent i = getIntent();
        userID = i.getExtras().getLong("userID");
        mode = i.getExtras().getLong("mode");
        if(i.hasExtra("tweetID")){
            tweetID = i.getExtras().getLong("tweetID");
        }

        userListview = (ListView) findViewById(R.id.followList);
        reload = (SwipeRefreshLayout) findViewById(R.id.follow_swipe);
        toolbar = (Toolbar) findViewById(R.id.follow_toolbar);
        setSupportActionBar(toolbar);
        setActionbarTitle(mode);

        userListview.setOnItemClickListener(this);
        reload.setOnRefreshListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu( Menu m ) {
        toolbar.inflateMenu(R.menu.setting); //TODO
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(!reload.isRefreshing()) {
            UserAdapter uAdp = (UserAdapter) userListview.getAdapter();
            UserDatabase uDB = uAdp.getAdapter();
            long userID = uDB.getUserID(position);
            Intent intent = new Intent(getApplicationContext(), UserProfile.class);
            Bundle bundle = new Bundle();
            bundle.putLong("userID",userID);
            bundle.putBoolean("home", false);//todo
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    public void onRefresh() {
        getUsers();
    }

    private void getUsers(){
        UserLists uList = new UserLists(UserDetail.this);
        if(mode == 0L || mode == 1L) {
            uList.execute(mode, userID);
        } else if(mode == 2L || mode == 3L) {
            uList.execute(mode, tweetID);
        }
    }

    private void setActionbarTitle(long mode) {
        if(getSupportActionBar() == null)
            return;
        if(mode == 0) {
            getSupportActionBar().setTitle(R.string.following);
        } else if(mode == 1) {
            getSupportActionBar().setTitle(R.string.follower);
        } else if(mode == 2) {
            getSupportActionBar().setTitle(R.string.retweet);
        } else if(mode == 3) {
            getSupportActionBar().setTitle(R.string.favorite);
        }
    }
}