package org.nuclearfog.twidda.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.UserFragment;

import static org.nuclearfog.twidda.fragment.UserFragment.KEY_FRAG_USER_ID;
import static org.nuclearfog.twidda.fragment.UserFragment.KEY_FRAG_USER_MODE;
import static org.nuclearfog.twidda.fragment.UserFragment.USER_FRAG_FOLLOWS;
import static org.nuclearfog.twidda.fragment.UserFragment.USER_FRAG_FRIENDS;
import static org.nuclearfog.twidda.fragment.UserFragment.USER_FRAG_RETWEET;

/**
 * Activity to show a list of twitter users
 *
 * @author nuclearfog
 */
public class UserDetail extends AppCompatActivity {

    /**
     * type of users to get from twitter
     * {@link #USERLIST_FRIENDS}, {@link #USERLIST_FOLLOWER}, {@link #USERLIST_RETWEETS}
     */
    public static final String KEY_USERDETAIL_MODE = "userlist_mode";

    /**
     * ID of a userlist, an user or a tweet to get the users from
     */
    public static final String KEY_USERDETAIL_ID = "userlist_id";

    /**
     * friends of an user, requires user ID
     */
    public static final int USERLIST_FRIENDS = 0xDF893242;

    /**
     * follower of an user, requires user ID
     */
    public static final int USERLIST_FOLLOWER = 0xA89F5968;

    /**
     * user retweeting a tweet, requires tweet ID
     */
    public static final int USERLIST_RETWEETS = 0x19F582E;


    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_fragment);
        View root = findViewById(R.id.fragment_root);
        Toolbar toolbar = findViewById(R.id.fragment_toolbar);

        // get parameter
        Intent data = getIntent();
        int mode = data.getIntExtra(KEY_USERDETAIL_MODE, 0);
        long id = data.getLongExtra(KEY_USERDETAIL_ID, -1);

        Bundle param = new Bundle();

        switch (mode) {
            case USERLIST_FRIENDS:
                // set fragment parameter
                param.putLong(KEY_FRAG_USER_ID, id);
                param.putInt(KEY_FRAG_USER_MODE, USER_FRAG_FRIENDS);
                // set toolbar title
                toolbar.setTitle(R.string.userlist_following);
                break;

            case USERLIST_FOLLOWER:
                // set fragment parameter
                param.putLong(KEY_FRAG_USER_ID, id);
                param.putInt(KEY_FRAG_USER_MODE, USER_FRAG_FOLLOWS);
                // set toolbar title
                toolbar.setTitle(R.string.userlist_follower);
                break;

            case USERLIST_RETWEETS:
                // set fragment parameter
                param.putLong(KEY_FRAG_USER_ID, id);
                param.putInt(KEY_FRAG_USER_MODE, USER_FRAG_RETWEET);
                // set toolbar title
                toolbar.setTitle(R.string.toolbar_userlist_retweet);
                break;
        }
        // insert fragment into view
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, UserFragment.class, param, "");
        fragmentTransaction.commit();

        // set toolbar
        setSupportActionBar(toolbar);

        // style activity
        GlobalSettings settings = GlobalSettings.getInstance(this);
        AppStyles.setTheme(settings, root);
    }
}