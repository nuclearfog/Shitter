package org.nuclearfog.twidda.window;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.adapter.FragmentAdapter.AdapterType;
import org.nuclearfog.twidda.database.GlobalSettings;

public class UserDetail extends AppCompatActivity {

    public enum UserType {
        FRIENDS,
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

        FragmentAdapter adapter;
        View root = findViewById(R.id.user_view);
        ViewPager pager = findViewById(R.id.user_pager);
        Toolbar toolbar = findViewById(R.id.user_toolbar);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
        setSupportActionBar(toolbar);

        switch (mode) {
            case FRIENDS:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.following);
                adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.FRIENDS_PAGE, id, "");
                pager.setAdapter(adapter);
                break;
            case FOLLOWERS:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.follower);
                adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.FOLLOWER_PAGE, id, "");
                pager.setAdapter(adapter);
                break;
            case RETWEETS:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.retweet);
                adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.RETWEETER_PAGE, id, "");
                pager.setAdapter(adapter);
                break;
            case FAVORITS:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.favorite);
                adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.FAVOR_PAGE, id, "");
                pager.setAdapter(adapter);
                break;
            default:
                if (BuildConfig.DEBUG)
                    throw new AssertionError("mode failure");
                break;
        }
    }
}