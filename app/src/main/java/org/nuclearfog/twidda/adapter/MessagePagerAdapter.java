package org.nuclearfog.twidda.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.nuclearfog.twidda.fragment.MessageListFragment;

public class MessagePagerAdapter extends FragmentPagerAdapter {

    private final int COUNT = 1;

    private final Fragment[] fragments;

    public MessagePagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new Fragment[COUNT];
        fragments[0] = new MessageListFragment();
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