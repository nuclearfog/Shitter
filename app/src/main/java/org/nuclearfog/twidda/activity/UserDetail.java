package org.nuclearfog.twidda.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.adapter.FragmentAdapter.AdapterType;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

public class UserDetail extends AppCompatActivity {

    public static final String KEY_USERDETAIL_MODE = "userlist_mode";
    public static final String KEY_USERDETAIL_ID = "userlist_owner_id";

    public static final int USERLIST_FRIENDS = 0;
    public static final int USERLIST_FOLLOWER = 1;
    public static final int USERLIST_RETWEETS = 2;
    public static final int USERLSIT_FAVORITS = 3;
    public static final int USERLIST_SUBSCRIBER = 4;

    private long id;
    private int mode;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_userlist);

        Bundle param = getIntent().getExtras();
        if (param != null && param.containsKey(KEY_USERDETAIL_MODE) && param.containsKey(KEY_USERDETAIL_ID)) {
            mode = param.getInt(KEY_USERDETAIL_MODE);
            id = param.getLong(KEY_USERDETAIL_ID);
        }

        FragmentAdapter adapter;
        View root = findViewById(R.id.user_view);
        ViewPager pager = findViewById(R.id.user_pager);
        Toolbar toolbar = findViewById(R.id.user_toolbar);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
        setSupportActionBar(toolbar);

        switch (mode) {
            case USERLIST_FRIENDS:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.userlist_following);
                adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.FRIENDS_PAGE, id, "");
                pager.setAdapter(adapter);
                break;
            case USERLIST_FOLLOWER:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.userlist_follower);
                adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.FOLLOWER_PAGE, id, "");
                pager.setAdapter(adapter);
                break;
            case USERLIST_RETWEETS:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.userlist_retweet);
                adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.RETWEETER_PAGE, id, "");
                pager.setAdapter(adapter);
                break;
            case USERLSIT_FAVORITS:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.userlist_favorite);
                adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.FAVOR_PAGE, id, "");
                pager.setAdapter(adapter);
                break;
            case USERLIST_SUBSCRIBER:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.user_list_subscr);
                adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.SUBSCRIBER_PAGE, id, "");
                pager.setAdapter(adapter);
        }
        FontTool.setViewFont(settings, root);
    }
}