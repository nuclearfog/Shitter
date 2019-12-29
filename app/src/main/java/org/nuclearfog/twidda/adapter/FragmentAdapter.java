package org.nuclearfog.twidda.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.nuclearfog.twidda.fragment.ListFragment;
import org.nuclearfog.twidda.fragment.MessageFragment;
import org.nuclearfog.twidda.fragment.TrendFragment;
import org.nuclearfog.twidda.fragment.TweetFragment;
import org.nuclearfog.twidda.fragment.TweetFragment.TweetType;
import org.nuclearfog.twidda.fragment.UserFragment;
import org.nuclearfog.twidda.fragment.UserFragment.UserType;

import static org.nuclearfog.twidda.fragment.ListFragment.KEY_FRAG_LIST;
import static org.nuclearfog.twidda.fragment.TweetFragment.KEY_FRAG_TWEET_FIX_LAYOUT;
import static org.nuclearfog.twidda.fragment.TweetFragment.KEY_FRAG_TWEET_ID;
import static org.nuclearfog.twidda.fragment.TweetFragment.KEY_FRAG_TWEET_MODE;
import static org.nuclearfog.twidda.fragment.TweetFragment.KEY_FRAG_TWEET_SEARCH;
import static org.nuclearfog.twidda.fragment.UserFragment.KEY_FRAG_USER_ID;
import static org.nuclearfog.twidda.fragment.UserFragment.KEY_FRAG_USER_MODE;
import static org.nuclearfog.twidda.fragment.UserFragment.KEY_FRAG_USER_SEARCH;

public class FragmentAdapter extends FragmentStatePagerAdapter {

    public enum AdapterType {
        PROFILE_TAB,
        SEARCH_TAB,
        TWEET_PAGE,
        MESSAGE_PAGE,
        FRIENDS_PAGE,
        FOLLOWER_PAGE,
        RETWEETER_PAGE,
        FAVOR_PAGE,
        LIST_PAGE,
        SUBSCRIBER_PAGE,
        LISTCONTENT_PAGE
    }

    private final Fragment[] fragments;


    public FragmentAdapter(FragmentManager fManager) {
        super(fManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        Bundle home_tl = new Bundle();
        Bundle ment_tl = new Bundle();
        home_tl.putSerializable(KEY_FRAG_TWEET_MODE, TweetType.HOME);
        ment_tl.putSerializable(KEY_FRAG_TWEET_MODE, TweetType.MENT);
        home_tl.putBoolean(KEY_FRAG_TWEET_FIX_LAYOUT, true);
        ment_tl.putBoolean(KEY_FRAG_TWEET_FIX_LAYOUT, true);
        fragments = new Fragment[3];
        fragments[0] = new TweetFragment();
        fragments[1] = new TrendFragment();
        fragments[2] = new TweetFragment();
        fragments[0].setArguments(home_tl);
        fragments[2].setArguments(ment_tl);
    }


    public FragmentAdapter(FragmentManager fManager, AdapterType mode, long id, String search) {
        super(fManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        switch (mode) {
            case PROFILE_TAB:
                Bundle usr_tweet = new Bundle();
                Bundle usr_favor = new Bundle();
                usr_tweet.putLong(KEY_FRAG_TWEET_ID, id);
                usr_favor.putLong(KEY_FRAG_TWEET_ID, id);
                usr_tweet.putBoolean(KEY_FRAG_TWEET_FIX_LAYOUT, false);
                usr_tweet.putBoolean(KEY_FRAG_TWEET_FIX_LAYOUT, false);
                usr_tweet.putSerializable(KEY_FRAG_TWEET_MODE, TweetType.USER_TWEET);
                usr_favor.putSerializable(KEY_FRAG_TWEET_MODE, TweetType.USER_FAVOR);
                fragments = new Fragment[2];
                fragments[0] = new TweetFragment();
                fragments[1] = new TweetFragment();
                fragments[0].setArguments(usr_tweet);
                fragments[1].setArguments(usr_favor);
                break;

            case SEARCH_TAB:
                Bundle tweetSearch = new Bundle();
                Bundle userSearch = new Bundle();
                tweetSearch.putString(KEY_FRAG_TWEET_SEARCH, search);
                userSearch.putString(KEY_FRAG_USER_SEARCH, search);
                tweetSearch.putSerializable(KEY_FRAG_TWEET_MODE, TweetType.SEARCH);
                userSearch.putSerializable(KEY_FRAG_USER_MODE, UserType.USEARCH);
                tweetSearch.putBoolean(KEY_FRAG_TWEET_FIX_LAYOUT, true);
                fragments = new Fragment[2];
                fragments[0] = new TweetFragment();
                fragments[1] = new UserFragment();
                fragments[0].setArguments(tweetSearch);
                fragments[1].setArguments(userSearch);
                break;

            case TWEET_PAGE:
                Bundle param = new Bundle();
                param.putSerializable(KEY_FRAG_TWEET_MODE, TweetType.TWEET_ANSR);
                param.putString(KEY_FRAG_TWEET_SEARCH, search);
                param.putBoolean(KEY_FRAG_TWEET_FIX_LAYOUT, false);
                param.putLong(KEY_FRAG_TWEET_ID, id);
                fragments = new Fragment[1];
                fragments[0] = new TweetFragment();
                fragments[0].setArguments(param);
                break;

            case MESSAGE_PAGE:
                fragments = new Fragment[1];
                fragments[0] = new MessageFragment();
                break;

            case FRIENDS_PAGE:
                Bundle uParam = new Bundle();
                uParam.putLong(KEY_FRAG_USER_ID, id);
                uParam.putSerializable(KEY_FRAG_USER_MODE, UserType.FRIENDS);
                fragments = new Fragment[1];
                fragments[0] = new UserFragment();
                fragments[0].setArguments(uParam);
                break;

            case FOLLOWER_PAGE:
                uParam = new Bundle();
                uParam.putLong(KEY_FRAG_USER_ID, id);
                uParam.putSerializable(KEY_FRAG_USER_MODE, UserType.FOLLOWS);
                fragments = new Fragment[1];
                fragments[0] = new UserFragment();
                fragments[0].setArguments(uParam);
                break;

            case RETWEETER_PAGE:
                uParam = new Bundle();
                uParam.putLong(KEY_FRAG_USER_ID, id);
                uParam.putSerializable(KEY_FRAG_USER_MODE, UserType.RETWEET);
                fragments = new Fragment[1];
                fragments[0] = new UserFragment();
                fragments[0].setArguments(uParam);
                break;

            case FAVOR_PAGE:
                uParam = new Bundle();
                uParam.putLong(KEY_FRAG_USER_ID, id);
                uParam.putSerializable(KEY_FRAG_USER_MODE, UserType.FAVORIT);
                fragments = new Fragment[1];
                fragments[0] = new UserFragment();
                fragments[0].setArguments(uParam);
                break;

            case LIST_PAGE:
                uParam = new Bundle();
                uParam.putLong(KEY_FRAG_LIST, id);
                fragments = new Fragment[1];
                fragments[0] = new ListFragment();
                fragments[0].setArguments(uParam);
                break;

            case SUBSCRIBER_PAGE:
                uParam = new Bundle();
                uParam.putLong(KEY_FRAG_USER_ID, id);
                uParam.putSerializable(KEY_FRAG_USER_MODE, UserType.SUBSCR);
                fragments = new Fragment[1];
                fragments[0] = new UserFragment();
                fragments[0].setArguments(uParam);
                break;

            case LISTCONTENT_PAGE:
                Bundle tweetList = new Bundle();
                Bundle userList = new Bundle();
                tweetList.putLong(KEY_FRAG_TWEET_ID, id);
                userList.putLong(KEY_FRAG_USER_ID, id);
                tweetList.putSerializable(KEY_FRAG_TWEET_MODE, TweetType.LIST);
                userList.putSerializable(KEY_FRAG_USER_MODE, UserType.LIST);
                tweetList.putBoolean(KEY_FRAG_TWEET_FIX_LAYOUT, true);
                fragments = new Fragment[2];
                fragments[0] = new TweetFragment();
                fragments[1] = new UserFragment();
                fragments[0].setArguments(tweetList);
                fragments[1].setArguments(userList);
                break;

            default:
                fragments = new Fragment[0];
                break;
        }
    }


    @Override
    @NonNull
    public Fragment getItem(int index) {
        return fragments[index];
    }


    @Override
    public int getCount() {
        return fragments.length;
    }


    public void notifySettingsChanged() {
        for (Fragment fragment : fragments) {
            if (fragment instanceof FragmentChangeObserver)
                ((FragmentChangeObserver) fragment).onSettingsChange();
        }
    }


    public void scrollToTop(int index) {
        if (fragments[index] instanceof FragmentChangeObserver)
            ((FragmentChangeObserver) fragments[index]).onTabChange();
    }


    public void clearData() {
        for (Fragment fragment : fragments) {
            if (fragment instanceof FragmentChangeObserver)
                ((FragmentChangeObserver) fragment).onDataClear();
        }
    }


    public interface FragmentChangeObserver {

        void onSettingsChange();

        void onTabChange();

        void onDataClear();
    }
}