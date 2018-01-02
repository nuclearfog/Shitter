package org.nuclearfog.twidda.Window;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.nuclearfog.twidda.Backend.Following;
import org.nuclearfog.twidda.DataBase.UserDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.ViewAdapter.UserAdapter;

public class Follower extends AppCompatActivity {

    private long userID;
    private long mode;
    private ListView follow;
    private Context context;
    private SwipeRefreshLayout reload;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.follow);
        userID = getIntent().getExtras().getLong("userID");
        mode = getIntent().getExtras().getLong("mode");
        toolbar = (Toolbar) findViewById(R.id.follow_toolbar);
        setSupportActionBar(toolbar);
        follow = (ListView) findViewById(R.id.followList);
        reload = (SwipeRefreshLayout) findViewById(R.id.follow_swipe);
        context = getApplicationContext();
        setActionbarTitle(mode);
        setListener();
    }

    /**
     * Create Actionbar
     */
    @Override
    public boolean onCreateOptionsMenu( Menu m ) {
        toolbar.inflateMenu(R.menu.setting); //TODO
        return true;
    }

    private void setListener() {
        reload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Following follow = new Following(Follower.this);
                follow.execute(mode, userID);
            }
        });

        follow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!reload.isRefreshing()) {
                    UserAdapter uAdp = (UserAdapter) follow.getAdapter();
                    UserDatabase uDB = uAdp.getAdapter();
                    long userID = uDB.getUserID(position);
                    Intent intent = new Intent(context, UserProfile.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("userID",userID);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
    }

    private void setActionbarTitle(long mode) {
        if(mode==1){
            getSupportActionBar().setTitle(R.string.follower);
        } else{
            getSupportActionBar().setTitle(R.string.following);
        }
    }
}