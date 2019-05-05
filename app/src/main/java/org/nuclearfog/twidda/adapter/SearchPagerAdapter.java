package org.nuclearfog.twidda.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.nuclearfog.twidda.fragment.TweetListFragment;
import org.nuclearfog.twidda.fragment.TweetListFragment.TweetType;
import org.nuclearfog.twidda.fragment.UserListFragment;
import org.nuclearfog.twidda.fragment.UserListFragment.UserType;


public class SearchPagerAdapter extends FragmentPagerAdapter {

    private static final int COUNT = 2;
    private final Fragment[] fragments;

    public SearchPagerAdapter(FragmentManager fm, String search) {
        super(fm);
        fragments = new Fragment[COUNT];
        fragments[0] = new TweetListFragment();
        fragments[1] = new UserListFragment();

        Bundle tweetSearch = new Bundle();
        Bundle userSearch = new Bundle();
        tweetSearch.putString("search", search);
        userSearch.putString("search", search);
        tweetSearch.putSerializable("mode", TweetType.SEARCH);
        userSearch.putSerializable("mode", UserType.USEARCH);
        tweetSearch.putBoolean("fix", true);
        userSearch.putBoolean("fix", true);

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