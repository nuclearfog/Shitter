package org.nuclearfog.twidda.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.nuclearfog.twidda.fragment.TweetListFragment;
import org.nuclearfog.twidda.fragment.TweetListFragment.TweetType;

public class TweetPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments;

    public TweetPagerAdapter(FragmentManager fm, long id, String name) {
        super(fm);

        Bundle param = new Bundle();
        param.putSerializable("mode", TweetType.TWEET_ANSR);
        param.putString("search", name);
        param.putBoolean("fix", false);
        param.putLong("id", id);
        fragments = new Fragment[1];
        fragments[0] = new TweetListFragment();
        fragments[0].setArguments(param);
    }


    @Override
    public Fragment getItem(int pos) {
        return fragments[pos];
    }


    @Override
    public int getCount() {
        return fragments.length;
    }
}