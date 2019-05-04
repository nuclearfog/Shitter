package org.nuclearfog.twidda.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import org.nuclearfog.twidda.fragment.TrendListFragment;
import org.nuclearfog.twidda.fragment.TweetListFragment;

import static org.nuclearfog.twidda.fragment.TweetListFragment.HOME;
import static org.nuclearfog.twidda.fragment.TweetListFragment.MENT;


public class HomeTabAdapter extends FragmentPagerAdapter {

    private static final int COUNT = 3;
    private static final Bundle HOME_TL, MENT_TL;

    private final Fragment[] fragments;

    static {
        HOME_TL = new Bundle();
        MENT_TL = new Bundle();
        HOME_TL.putInt("mode",HOME);
        MENT_TL.putInt("mode", MENT);
    }

    public HomeTabAdapter(FragmentManager fm) {
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
}