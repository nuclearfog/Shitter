package org.nuclearfog.twidda.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.nuclearfog.twidda.fragment.TrendListFragment;
import org.nuclearfog.twidda.fragment.TweetListFragment;
import org.nuclearfog.twidda.fragment.TweetListFragment.TweetType;


public class HomePagerAdapter extends FragmentPagerAdapter {

    private static final int COUNT = 3;
    private final Fragment[] fragments;

    private static final Bundle HOME_TL = new Bundle();
    private static final Bundle MENT_TL = new Bundle();

    static {
        HOME_TL.putSerializable("mode", TweetType.HOME);
        MENT_TL.putSerializable("mode", TweetType.MENT);
        HOME_TL.putBoolean("fix", true);
        MENT_TL.putBoolean("fix", true);
    }

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new Fragment[COUNT];
        fragments[0] = new TweetListFragment();
        fragments[1] = new TrendListFragment();
        fragments[2] = new TweetListFragment();
        fragments[0].setArguments(HOME_TL);
        fragments[2].setArguments(MENT_TL);
    }


    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }


    @Override
    public int getCount() {
        return COUNT;
    }


    public void notifySettingsChanged() {
        for (Fragment fragment : fragments) {
            if (fragment instanceof OnSettingsChanged)
                ((OnSettingsChanged) fragment).settingsChanged();
        }
    }


    public interface OnSettingsChanged {

        void settingsChanged();
    }
}