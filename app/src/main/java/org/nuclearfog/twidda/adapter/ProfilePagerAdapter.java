package org.nuclearfog.twidda.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.nuclearfog.twidda.fragment.TweetListFragment;
import org.nuclearfog.twidda.fragment.TweetListFragment.TweetType;


public class ProfilePagerAdapter extends FragmentPagerAdapter {

    private static final int COUNT = 2;

    private final Fragment[] fragments;


    public ProfilePagerAdapter(FragmentManager fm, long userId) {
        super(fm);

        Bundle usr_tweet = new Bundle();
        Bundle usr_favor = new Bundle();
        usr_tweet.putLong("id", userId);
        usr_favor.putLong("id", userId);
        usr_tweet.putBoolean("fix", false);
        usr_tweet.putBoolean("fix", false);
        usr_tweet.putSerializable("mode", TweetType.USER_TWEET);
        usr_favor.putSerializable("mode", TweetType.USER_FAVOR);

        fragments = new Fragment[COUNT];
        fragments[0] = new TweetListFragment();
        fragments[1] = new TweetListFragment();
        fragments[0].setArguments(usr_tweet);
        fragments[1].setArguments(usr_favor);
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