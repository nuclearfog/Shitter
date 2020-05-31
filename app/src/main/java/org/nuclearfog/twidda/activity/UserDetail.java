package org.nuclearfog.twidda.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

public class UserDetail extends AppCompatActivity {

    public static final String KEY_USERDETAIL_MODE = "userlist_mode";
    public static final String KEY_USERDETAIL_ID = "userlist_owner_id";


    public static final int USERLIST_FRIENDS = 1;
    public static final int USERLIST_FOLLOWER = 2;
    public static final int USERLIST_RETWEETS = 3;
    public static final int USERLIST_SUBSCRBR = 5;

    private FragmentAdapter adapter;
    private ViewPager pager;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_userlist);
        View root = findViewById(R.id.user_view);
        Toolbar toolbar = findViewById(R.id.user_toolbar);
        pager = findViewById(R.id.user_pager);
        setSupportActionBar(toolbar);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
        FontTool.setViewFontAndColor(settings, root);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Bundle param = getIntent().getExtras();
        if (adapter == null && param != null && param.containsKey(KEY_USERDETAIL_MODE) && param.containsKey(KEY_USERDETAIL_ID)) {
            adapter = new FragmentAdapter(getSupportFragmentManager());
            long id = param.getLong(KEY_USERDETAIL_ID);
            int titleStr = 0;

            switch (param.getInt(KEY_USERDETAIL_MODE)) {
                case USERLIST_FRIENDS:
                    titleStr = R.string.userlist_following;
                    adapter.setupFriendsPage(id);
                    break;

                case USERLIST_FOLLOWER:
                    titleStr = R.string.userlist_follower;
                    adapter.setupFollowerPage(id);
                    break;

                case USERLIST_RETWEETS:
                    titleStr = R.string.userlist_retweet;
                    adapter.setupRetweeterPage(id);
                    break;

                case USERLIST_SUBSCRBR:
                    titleStr = R.string.user_list_subscr;
                    adapter.setupSubscriberPage(id);
                    break;
            }
            pager.setAdapter(adapter);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(titleStr);
        }
    }
}