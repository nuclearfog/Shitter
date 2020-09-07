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

/**
 * Activity to show a list of twitter users
 */
public class UserDetail extends AppCompatActivity {

    public static final String KEY_USERDETAIL_MODE = "userlist_mode";
    public static final String KEY_USERDETAIL_ID = "userlist_owner_id";

    public static final int USERLIST_FRIENDS = 1;
    public static final int USERLIST_FOLLOWER = 2;
    public static final int USERLIST_RETWEETS = 3;
    public static final int USERLIST_SUBSCRBR = 5;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_userlist);
        View root = findViewById(R.id.user_view);
        Toolbar toolbar = findViewById(R.id.user_toolbar);
        ViewPager pager = findViewById(R.id.user_pager);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        Bundle param = getIntent().getExtras();
        if (param != null && param.containsKey(KEY_USERDETAIL_ID) && param.containsKey(KEY_USERDETAIL_MODE)) {
            long id = param.getLong(KEY_USERDETAIL_ID);
            switch (param.getInt(KEY_USERDETAIL_MODE)) {
                case USERLIST_FRIENDS:
                    toolbar.setTitle(R.string.userlist_following);
                    adapter.setupFriendsPage(id);
                    break;

                case USERLIST_FOLLOWER:
                    toolbar.setTitle(R.string.userlist_follower);
                    adapter.setupFollowerPage(id);
                    break;

                case USERLIST_RETWEETS:
                    toolbar.setTitle(R.string.userlist_retweet);
                    adapter.setupRetweeterPage(id);
                    break;

                case USERLIST_SUBSCRBR:
                    toolbar.setTitle(R.string.user_list_subscr);
                    adapter.setupSubscriberPage(id);
                    break;
            }
        }
        setSupportActionBar(toolbar);
        FontTool.setViewFontAndColor(settings, root);
    }
}