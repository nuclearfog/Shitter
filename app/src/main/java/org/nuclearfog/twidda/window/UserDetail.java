package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.backend.UserLoader;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.backend.UserLoader.Mode.FAVORIT;
import static org.nuclearfog.twidda.backend.UserLoader.Mode.FOLLOWERS;
import static org.nuclearfog.twidda.backend.UserLoader.Mode.FOLLOWING;
import static org.nuclearfog.twidda.backend.UserLoader.Mode.RETWEET;

/**
 * User List Activity
 *
 * @see UserLoader
 */
public class UserDetail extends AppCompatActivity implements OnItemClickListener, OnRefreshListener {

    private RecyclerView userList;
    private SwipeRefreshLayout userReload;
    private UserAdapter usrAdp;
    private GlobalSettings settings;
    private UserLoader userAsync;
    private int mode;
    private long id;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_userlist);

        Bundle param = getIntent().getExtras();
        if (param != null) {
            if (BuildConfig.DEBUG && param.size() != 2)
                throw new AssertionError();
            mode = param.getInt("mode");
            id = param.getLong("ID");
        }

        View root = findViewById(R.id.user_view);
        Toolbar toolbar = findViewById(R.id.user_toolbar);
        userReload = findViewById(R.id.user_refresh);
        userList = findViewById(R.id.userlist);
        userList.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(toolbar);

        settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
        userReload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());

        userReload.setRefreshing(true);
        userReload.setOnRefreshListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (userAsync == null) {
            int titleId;
            usrAdp = new UserAdapter(this);
            usrAdp.toggleImage(settings.getImageLoad());
            usrAdp.setColor(settings.getFontColor());
            userList.setAdapter(usrAdp);

            switch (mode) {
                case 0:
                    titleId = R.string.following;
                    userAsync = new UserLoader(UserDetail.this, FOLLOWING);
                    break;

                case 1:
                    titleId = R.string.follower;
                    userAsync = new UserLoader(UserDetail.this, FOLLOWERS);
                    break;

                case 2:
                    titleId = R.string.retweet;
                    userAsync = new UserLoader(UserDetail.this, RETWEET);
                    break;

                case 3:
                default:
                    titleId = R.string.favorite;
                    userAsync = new UserLoader(UserDetail.this, FAVORIT);
                    break;
            }
            userAsync.execute(id, -1L);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(titleId);
            }
        }
    }


    @Override
    protected void onStop() {
        if (userAsync != null && userAsync.getStatus() == RUNNING)
            userAsync.cancel(true);
        super.onStop();
    }


    @Override
    public void onItemClick(RecyclerView rv, int position) {
        if (!userReload.isRefreshing() && usrAdp != null) {
            TwitterUser user = usrAdp.getData(position);
            long userID = user.getId();
            String username = user.getScreenname();
            Intent intent = new Intent(this, UserProfile.class);
            intent.putExtra("userID", userID);
            intent.putExtra("username", username);
            startActivity(intent);
        }
    }


    @Override
    public void onRefresh() {
        switch (mode) {
            case 0:
                userAsync = new UserLoader(UserDetail.this, FOLLOWING);
                break;
            case 1:
                userAsync = new UserLoader(UserDetail.this, FOLLOWERS);
                break;
            case 2:
                userAsync = new UserLoader(UserDetail.this, RETWEET);
                break;
            case 3:
            default:
                userAsync = new UserLoader(UserDetail.this, FAVORIT);
                break;
        }
        userAsync.execute(id, -1L);
    }
}