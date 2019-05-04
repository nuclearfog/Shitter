package org.nuclearfog.twidda.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.nuclearfog.twidda.fragment.TweetListFragment;

import static org.nuclearfog.twidda.fragment.TweetListFragment.USER_FAVOR;
import static org.nuclearfog.twidda.fragment.TweetListFragment.USER_TWEET;

public class ProfileTabAdapter extends FragmentPagerAdapter {

    private static final int COUNT = 2;

    private final Fragment[] fragments;


    public ProfileTabAdapter(FragmentManager fm, long userId) {
        super(fm);

        Bundle usr_tweet = new Bundle();
        Bundle usr_favor = new Bundle();

        usr_tweet.putLong("id", userId);
        usr_tweet.putInt("mode", USER_TWEET);
        usr_tweet.putBoolean("fix", false);
        usr_favor.putLong("id", userId);
        usr_favor.putInt("mode", USER_FAVOR);
        usr_tweet.putBoolean("fix", false);

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