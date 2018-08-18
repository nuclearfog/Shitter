package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.GlobalSettings;
import org.nuclearfog.twidda.backend.UserLists;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;
import org.nuclearfog.twidda.viewadapter.UserAdapter;
import org.nuclearfog.twidda.viewadapter.UserAdapter.OnItemClicked;

public class UserDetail extends AppCompatActivity implements OnItemClicked {

    private RecyclerView userList;
    private UserLists uList;
    private int mode = -1;
    private long userID = 0;
    private long tweetID = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        b = getIntent().getExtras();
        if (b != null) {
            userID = b.getLong("userID");
            mode = b.getInt("mode");
            if (b.containsKey("tweetID"))
                tweetID = b.getLong("tweetID");
        }
        setContentView(R.layout.userpage);

        userList = findViewById(R.id.userlist);
        userList.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar = findViewById(R.id.user_toolbar);
        setSupportActionBar(toolbar);
        GlobalSettings settings = GlobalSettings.getInstance(this);
        int background = settings.getBackgroundColor();

        userList.setBackgroundColor(background);
        getUsers();
    }

    @Override
    protected void onPause() {
        if (uList != null && !uList.isCancelled())
            uList.cancel(true);
        super.onPause();
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
    public void onItemClick(ViewGroup parent, int position) {
        UserAdapter userListAdapter = (UserAdapter) userList.getAdapter();
        if (userListAdapter != null) {
            TwitterUser user = userListAdapter.getData().get(position);
            long userID = user.userID;
            String username = user.screenname;

            Intent intent = new Intent(this, UserProfile.class);
            intent.putExtra("userID", userID);
            intent.putExtra("username", username);
            startActivity(intent);
        }
    }

    private void getUsers() {
        uList = new UserLists(UserDetail.this);
        int titleId = 0;

        switch (mode) {
            case 0:
                titleId = R.string.following;
                uList.execute(userID, UserLists.FOLLOWING, -1L);
                break;

            case 1:
                titleId = R.string.follower;
                uList.execute(userID, UserLists.FOLLOWERS, -1L);
                break;

            case 2:
                titleId = R.string.retweet;
                uList.execute(tweetID, UserLists.RETWEETER, -1L);
                break;

            case 3:
                titleId = R.string.favorite;
                uList.execute(tweetID, UserLists.FAVORISER, -1L);
                break;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleId);
        }
    }
}