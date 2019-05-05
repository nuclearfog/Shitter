package org.nuclearfog.twidda.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.fragment.UserListFragment;
import org.nuclearfog.twidda.fragment.UserListFragment.UserType;


public class UserPagerAdapter extends FragmentPagerAdapter {

    public enum Mode {
        FOLLOWERS,
        FOLLOWING,
        RETWEETER,
        FAVORS
    }

    private static final int COUNT = 1;
    private final Fragment[] fragments;

    public UserPagerAdapter(FragmentManager fm, Mode mode, long id) {
        super(fm);
        Bundle param = new Bundle();
        fragments = new Fragment[COUNT];
        fragments[0] = new UserListFragment();

        switch (mode) {
            case FOLLOWERS:
                param.putSerializable("mode", UserType.FOLLOWS);
                break;
            case FOLLOWING:
                param.putSerializable("mode", UserType.FRIENDS);
                break;
            case RETWEETER:
                param.putSerializable("mode", UserType.RETWEET);
                break;
            case FAVORS:
                param.putSerializable("mode", UserType.FAVORIT);
                break;
            default:
                if (BuildConfig.DEBUG)
                    throw new AssertionError("mode failure");
                break;
        }
        param.putLong("id", id);
        fragments[0].setArguments(param);
    }


    @Override
    public Fragment getItem(int pos) {
        return fragments[pos];
    }


    @Override
    public int getCount() {
        return COUNT;
    }
}