package org.nuclearfog.twidda.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.nuclearfog.twidda.fragment.ListFragment;
import org.nuclearfog.twidda.fragment.TrendFragment;
import org.nuclearfog.twidda.fragment.TweetFragment;
import org.nuclearfog.twidda.fragment.UserFragment;
import org.nuclearfog.twidda.fragment.UserListFragment;

import static org.nuclearfog.twidda.fragment.TweetFragment.KEY_FRAG_TWEET_ID;
import static org.nuclearfog.twidda.fragment.TweetFragment.KEY_FRAG_TWEET_MODE;
import static org.nuclearfog.twidda.fragment.TweetFragment.KEY_FRAG_TWEET_SEARCH;
import static org.nuclearfog.twidda.fragment.TweetFragment.TWEET_FRAG_FAVORS;
import static org.nuclearfog.twidda.fragment.TweetFragment.TWEET_FRAG_HOME;
import static org.nuclearfog.twidda.fragment.TweetFragment.TWEET_FRAG_LIST;
import static org.nuclearfog.twidda.fragment.TweetFragment.TWEET_FRAG_MENT;
import static org.nuclearfog.twidda.fragment.TweetFragment.TWEET_FRAG_SEARCH;
import static org.nuclearfog.twidda.fragment.TweetFragment.TWEET_FRAG_TWEETS;
import static org.nuclearfog.twidda.fragment.UserFragment.KEY_FRAG_DEL_USER;
import static org.nuclearfog.twidda.fragment.UserFragment.KEY_FRAG_USER_ID;
import static org.nuclearfog.twidda.fragment.UserFragment.KEY_FRAG_USER_MODE;
import static org.nuclearfog.twidda.fragment.UserFragment.KEY_FRAG_USER_SEARCH;
import static org.nuclearfog.twidda.fragment.UserFragment.USER_FRAG_LISTS;
import static org.nuclearfog.twidda.fragment.UserFragment.USER_FRAG_SEARCH;
import static org.nuclearfog.twidda.fragment.UserFragment.USER_FRAG_SUBSCR;
import static org.nuclearfog.twidda.fragment.UserListFragment.KEY_FRAG_LIST_LIST_TYPE;
import static org.nuclearfog.twidda.fragment.UserListFragment.KEY_FRAG_LIST_OWNER_ID;
import static org.nuclearfog.twidda.fragment.UserListFragment.KEY_FRAG_LIST_OWNER_NAME;
import static org.nuclearfog.twidda.fragment.UserListFragment.LIST_USER_OWNS;
import static org.nuclearfog.twidda.fragment.UserListFragment.LIST_USER_SUBSCR_TO;

/**
 * Fragment adapter for ViewPager
 *
 * @author nuclearfog
 */
public class FragmentAdapter extends FragmentStatePagerAdapter {

    private ListFragment[] fragments;

    /**
     * Initialize Fragment Adapter
     *
     * @param fManager Activity Fragment Manager
     */
    public FragmentAdapter(FragmentManager fManager) {
        super(fManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragments = new ListFragment[0];
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

    /**
     * Check if adapter is empty
     *
     * @return true if adapter does not contain fragments
     */
    public boolean isEmpty() {
        return fragments.length == 0;
    }

    /**
     * Clear all fragments
     */
    public void clear() {
        fragments = new ListFragment[0];
        notifyDataSetChanged();
    }

    /**
     * setup adapter for the home activity
     */
    public void setupForHomePage() {
        Bundle home_tl = new Bundle();
        Bundle ment_tl = new Bundle();
        home_tl.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_HOME);
        ment_tl.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_MENT);
        fragments = new ListFragment[3];
        fragments[0] = new TweetFragment();
        fragments[1] = new TrendFragment();
        fragments[2] = new TweetFragment();
        fragments[0].setArguments(home_tl);
        fragments[2].setArguments(ment_tl);
        notifyDataSetChanged();
    }

    /**
     * setup adapter for viewing user tweets and favorites
     *
     * @param userId ID of the user
     */
    public void setupProfilePage(long userId) {
        fragments = new ListFragment[2];

        fragments[0] = new TweetFragment();
        Bundle usr_tweet = new Bundle();
        usr_tweet.putLong(KEY_FRAG_TWEET_ID, userId);
        usr_tweet.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_TWEETS);
        fragments[0].setArguments(usr_tweet);

        fragments[1] = new TweetFragment();
        Bundle usr_favor = new Bundle();
        usr_favor.putLong(KEY_FRAG_TWEET_ID, userId);
        usr_favor.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_FAVORS);
        fragments[1].setArguments(usr_favor);

        notifyDataSetChanged();
    }

    /**
     * setup adapter for search for tweet and user search
     *
     * @param search Search string
     */
    public void setupSearchPage(String search) {
        Bundle tweetSearch = new Bundle();
        Bundle userSearch = new Bundle();
        tweetSearch.putString(KEY_FRAG_TWEET_SEARCH, search);
        userSearch.putString(KEY_FRAG_USER_SEARCH, search);
        tweetSearch.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_SEARCH);
        userSearch.putInt(KEY_FRAG_USER_MODE, USER_FRAG_SEARCH);
        fragments = new ListFragment[2];
        fragments[0] = new TweetFragment();
        fragments[1] = new UserFragment();
        fragments[0].setArguments(tweetSearch);
        fragments[1].setArguments(userSearch);
        notifyDataSetChanged();
    }

    /**
     * setup adapter for a list of user lists created by an user
     *
     * @param userId   ID of the user
     * @param username screen name of the owner
     */
    public void setupListPage(long userId, String username) {
        Bundle userListParam = new Bundle();
        Bundle subscriberParam = new Bundle();
        if (userId > 0) {
            userListParam.putLong(KEY_FRAG_LIST_OWNER_ID, userId);
            subscriberParam.putLong(KEY_FRAG_LIST_OWNER_ID, userId);
        } else {
            userListParam.putString(KEY_FRAG_LIST_OWNER_NAME, username);
            subscriberParam.putString(KEY_FRAG_LIST_OWNER_NAME, username);
        }
        userListParam.putInt(KEY_FRAG_LIST_LIST_TYPE, LIST_USER_OWNS);
        subscriberParam.putInt(KEY_FRAG_LIST_LIST_TYPE, LIST_USER_SUBSCR_TO);
        fragments = new ListFragment[2];
        fragments[0] = new UserListFragment();
        fragments[1] = new UserListFragment();
        fragments[0].setArguments(userListParam);
        fragments[1].setArguments(subscriberParam);
        notifyDataSetChanged();
    }

    /**
     * setup adapter for a page of tweets and users in an user list
     *
     * @param listId      ID of an user list
     * @param ownerOfList true if current user owns this list
     */
    public void setupListContentPage(long listId, boolean ownerOfList) {
        Bundle tweetParam = new Bundle();
        Bundle userParam = new Bundle();
        Bundle subscrParam = new Bundle();
        tweetParam.putLong(KEY_FRAG_TWEET_ID, listId);
        tweetParam.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_LIST);
        userParam.putInt(KEY_FRAG_USER_MODE, USER_FRAG_LISTS);
        userParam.putBoolean(KEY_FRAG_DEL_USER, ownerOfList);
        userParam.putLong(KEY_FRAG_USER_ID, listId);
        subscrParam.putLong(KEY_FRAG_USER_ID, listId);
        subscrParam.putInt(KEY_FRAG_USER_MODE, USER_FRAG_SUBSCR);
        fragments = new ListFragment[3];
        fragments[0] = new TweetFragment();
        fragments[1] = new UserFragment();
        fragments[2] = new UserFragment();
        fragments[0].setArguments(tweetParam);
        fragments[1].setArguments(userParam);
        fragments[2].setArguments(subscrParam);
        notifyDataSetChanged();
    }

    /**
     * called when app settings change
     */
    public void notifySettingsChanged() {
        for (ListFragment fragment : fragments) {
            fragment.reset();
        }
    }

    /**
     * called to scroll page to top
     *
     * @param index tab position of page
     */
    public void scrollToTop(int index) {
        fragments[index].onTabChange();
    }
}