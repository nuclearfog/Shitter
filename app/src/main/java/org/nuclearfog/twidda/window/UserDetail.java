package org.nuclearfog.twidda.window;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.UserPagerAdapter;
import org.nuclearfog.twidda.adapter.UserPagerAdapter.Mode;
import org.nuclearfog.twidda.database.GlobalSettings;

public class UserDetail extends AppCompatActivity {

    public enum UserType {
        FOLLOWING,
        FOLLOWERS,
        RETWEETS,
        FAVORITS,
    }

    private UserType mode;
    private long id;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_userlist);

        Bundle param = getIntent().getExtras();
        if (param != null && param.containsKey("mode") && param.containsKey("ID")) {
            mode = (UserType) param.getSerializable("mode");
            id = param.getLong("ID");
        } else if (BuildConfig.DEBUG) {
            throw new AssertionError();
        }

        UserPagerAdapter adapter;
        View root = findViewById(R.id.user_view);
        ViewPager pager = findViewById(R.id.user_pager);
        Toolbar toolbar = findViewById(R.id.user_toolbar);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
        setSupportActionBar(toolbar);

        switch (mode) {
            case FOLLOWING:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.following);
                adapter = new UserPagerAdapter(getSupportFragmentManager(), Mode.FOLLOWING, id);
                pager.setAdapter(adapter);
                break;
            case FOLLOWERS:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.follower);
                adapter = new UserPagerAdapter(getSupportFragmentManager(), Mode.FOLLOWERS, id);
                pager.setAdapter(adapter);
                break;
            case RETWEETS:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.retweet);
                adapter = new UserPagerAdapter(getSupportFragmentManager(), Mode.RETWEETER, id);
                pager.setAdapter(adapter);
                break;
            case FAVORITS:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.favorite);
                adapter = new UserPagerAdapter(getSupportFragmentManager(), Mode.FAVORS, id);
                pager.setAdapter(adapter);
                break;
            default:
                if (BuildConfig.DEBUG)
                    throw new AssertionError("mode failure");
                break;
        }
    }
}