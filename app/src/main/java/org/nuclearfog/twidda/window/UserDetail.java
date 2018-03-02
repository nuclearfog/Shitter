package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
public class UserDetail extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private long userID;
    private long mode;
    private ListView userListview;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.user);
        getExtras(getIntent().getExtras());

        userListview = (ListView) findViewById(R.id.userlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.user_toolbar);
        setSupportActionBar(toolbar);
        userListview.setOnItemClickListener(this);
        getUsers();
    }

    /**
     * Home Button
     */
    @Override
    protected void onUserLeaveHint(){
        super.onUserLeaveHint();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onCreateOptionsMenu( Menu m ) {
        getMenuInflater().inflate(R.menu.user, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.user_back) {
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserAdapter uAdp = (UserAdapter) userListview.getAdapter();
        UserDatabase uDB = uAdp.getData();
        long userID = uDB.getUserID(position);
        String username = uDB.getScreenname(position);
        Intent intent = new Intent(getApplicationContext(), UserProfile.class);
        Bundle bundle = new Bundle();
        bundle.putLong("userID",userID);
        bundle.putString("username", username);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @SuppressWarnings("ConstantConditions")
    private void getUsers() {
        UserLists uList = new UserLists(UserDetail.this);
        if(mode == 0L){
            getSupportActionBar().setTitle(R.string.following);
            uList.execute(userID, UserLists.FOLLOWING, -1L);
        } else if(mode == 1L){
            getSupportActionBar().setTitle(R.string.follower);
            uList.execute(userID, UserLists.FOLLOWERS, -1L);
        } else if(mode == 2L){
            getSupportActionBar().setTitle(R.string.retweet);
            uList.execute(userID, UserLists.RETWEETER, -1L);
        } else if(mode == 3L){
            getSupportActionBar().setTitle(R.string.favorite);
            uList.execute(userID, UserLists.FAVORISER, -1L);
        }
    }

    @SuppressWarnings("ConstantCondidions")
    private void getExtras(Bundle b) {
        userID = b.getLong("userID");
        mode = b.getLong("mode");
    }
}