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

import org.nuclearfog.twidda.backend.FollowStatus;
import org.nuclearfog.twidda.database.UserDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.UserAdapter;

/**
 * Get Follow Connections from an User
 * @see FollowStatus
 */
public class Follower extends AppCompatActivity implements AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private long userID;
    private long mode;
    private ListView follow;
    private SwipeRefreshLayout reload;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.follow);
        userID = getIntent().getExtras().getLong("userID");
        mode = getIntent().getExtras().getLong("mode");

        follow = (ListView) findViewById(R.id.followList);
        reload = (SwipeRefreshLayout) findViewById(R.id.follow_swipe);
        toolbar = (Toolbar) findViewById(R.id.follow_toolbar);
        setSupportActionBar(toolbar);
        setActionbarTitle(mode);

        follow.setOnItemClickListener(this);
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
            UserAdapter uAdp = (UserAdapter) follow.getAdapter();
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
        FollowStatus follow = new FollowStatus(Follower.this);
        follow.execute(mode, userID);
    }

    private void setActionbarTitle(long mode) {
        if(mode==1){
            getSupportActionBar().setTitle(R.string.follower);
        } else{
            getSupportActionBar().setTitle(R.string.following);
        }
    }
}