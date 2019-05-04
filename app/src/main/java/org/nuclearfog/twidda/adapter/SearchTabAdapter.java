package org.nuclearfog.twidda.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.nuclearfog.twidda.fragment.TweetListFragment;
import org.nuclearfog.twidda.fragment.UserListFragment;

import static org.nuclearfog.twidda.fragment.TweetListFragment.SEARCH;
import static org.nuclearfog.twidda.fragment.UserListFragment.USEARCH;

public class SearchTabAdapter extends FragmentPagerAdapter {

    private static final int COUNT = 2;

    private final Fragment[] fragments;


    public SearchTabAdapter(FragmentManager fm, String search) {
        super(fm);
        fragments = new Fragment[COUNT];
        fragments[0] = new TweetListFragment();
        fragments[1] = new UserListFragment();

        Bundle tweetSearch = new Bundle();
        Bundle userSearch = new Bundle();

        tweetSearch.putInt("mode", SEARCH);
        tweetSearch.putBoolean("fix", true);
        tweetSearch.putString("search", search);
        userSearch.putInt("mode", USEARCH);
        userSearch.putString("search", search);

        fragments[0].setArguments(tweetSearch);
        fragments[1].setArguments(userSearch);
    }


    @Override
    public Fragment getItem(int i) {
        return fragments[i];
    }


    public int getCount() {
        return COUNT;
    }
}