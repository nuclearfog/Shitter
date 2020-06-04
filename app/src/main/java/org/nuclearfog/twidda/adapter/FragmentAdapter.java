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
import org.nuclearfog.twidda.fragment.UserFragment;

import static org.nuclearfog.twidda.fragment.ListFragment.KEY_FRAG_LIST;
import static org.nuclearfog.twidda.fragment.TweetFragment.KEY_FRAG_TWEET_ID;
import static org.nuclearfog.twidda.fragment.TweetFragment.KEY_FRAG_TWEET_MODE;
import static org.nuclearfog.twidda.fragment.TweetFragment.KEY_FRAG_TWEET_SEARCH;
import static org.nuclearfog.twidda.fragment.TweetFragment.TWEET_FRAG_ANSWER;
import static org.nuclearfog.twidda.fragment.TweetFragment.TWEET_FRAG_FAVORS;
import static org.nuclearfog.twidda.fragment.TweetFragment.TWEET_FRAG_HOME;
import static org.nuclearfog.twidda.fragment.TweetFragment.TWEET_FRAG_LIST;
import static org.nuclearfog.twidda.fragment.TweetFragment.TWEET_FRAG_MENT;
import static org.nuclearfog.twidda.fragment.TweetFragment.TWEET_FRAG_SEARCH;
import static org.nuclearfog.twidda.fragment.TweetFragment.TWEET_FRAG_TWEETS;
import static org.nuclearfog.twidda.fragment.UserFragment.KEY_FRAG_USER_ID;
import static org.nuclearfog.twidda.fragment.UserFragment.KEY_FRAG_USER_MODE;
import static org.nuclearfog.twidda.fragment.UserFragment.KEY_FRAG_USER_SEARCH;
import static org.nuclearfog.twidda.fragment.UserFragment.USER_FRAG_FOLLOWS;
import static org.nuclearfog.twidda.fragment.UserFragment.USER_FRAG_FRIENDS;
import static org.nuclearfog.twidda.fragment.UserFragment.USER_FRAG_LISTS;
import static org.nuclearfog.twidda.fragment.UserFragment.USER_FRAG_RETWEET;
import static org.nuclearfog.twidda.fragment.UserFragment.USER_FRAG_SEARCH;
import static org.nuclearfog.twidda.fragment.UserFragment.USER_FRAG_SUBSCR;

public class FragmentAdapter extends FragmentStatePagerAdapter {

    private Fragment[] fragments;

    /**
     * Initialize Fragment Adapter
     *
     * @param fManager Activity Fragment Manager
     */
    public FragmentAdapter(FragmentManager fManager) {
        super(fManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragments = new Fragment[0];
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


    public boolean isEmpty() {
        return fragments.length == 0;
    }


    public void setupForHomePage() {
        Bundle home_tl = new Bundle();
        Bundle ment_tl = new Bundle();
        home_tl.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_HOME);
        ment_tl.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_MENT);
        fragments = new Fragment[3];
        fragments[0] = new TweetFragment();
        fragments[1] = new TrendFragment();
        fragments[2] = new TweetFragment();
        fragments[0].setArguments(home_tl);
        fragments[2].setArguments(ment_tl);
        notifyDataSetChanged();
    }


    public void setupProfilePage(long userId) {
        Bundle usr_tweet = new Bundle();
        Bundle usr_favor = new Bundle();
        usr_tweet.putLong(KEY_FRAG_TWEET_ID, userId);
        usr_favor.putLong(KEY_FRAG_TWEET_ID, userId);
        usr_tweet.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_TWEETS);
        usr_favor.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_FAVORS);
        fragments = new Fragment[2];
        fragments[0] = new TweetFragment();
        fragments[1] = new TweetFragment();
        fragments[0].setArguments(usr_tweet);
        fragments[1].setArguments(usr_favor);
        notifyDataSetChanged();
    }


    public void setupSearchPage(String search) {
        Bundle tweetSearch = new Bundle();
        Bundle userSearch = new Bundle();
        tweetSearch.putString(KEY_FRAG_TWEET_SEARCH, search);
        userSearch.putString(KEY_FRAG_USER_SEARCH, search);
        tweetSearch.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_SEARCH);
        userSearch.putInt(KEY_FRAG_USER_MODE, USER_FRAG_SEARCH);
        fragments = new Fragment[2];
        fragments[0] = new TweetFragment();
        fragments[1] = new UserFragment();
        fragments[0].setArguments(tweetSearch);
        fragments[1].setArguments(userSearch);
        notifyDataSetChanged();
    }


    public void setupTweetPage(long tweetId, String replyName) {
        Bundle param = new Bundle();
        param.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_ANSWER);
        param.putString(KEY_FRAG_TWEET_SEARCH, replyName);
        param.putLong(KEY_FRAG_TWEET_ID, tweetId);
        fragments = new Fragment[1];
        fragments[0] = new TweetFragment();
        fragments[0].setArguments(param);
        notifyDataSetChanged();
    }


    public void setupFriendsPage(long userId) {
        Bundle param = new Bundle();
        param.putLong(KEY_FRAG_USER_ID, userId);
        param.putInt(KEY_FRAG_USER_MODE, USER_FRAG_FRIENDS);
        fragments = new Fragment[1];
        fragments[0] = new UserFragment();
        fragments[0].setArguments(param);
        notifyDataSetChanged();
    }


    public void setupFollowerPage(long userId) {
        Bundle param = new Bundle();
        param.putLong(KEY_FRAG_USER_ID, userId);
        param.putInt(KEY_FRAG_USER_MODE, USER_FRAG_FOLLOWS);
        fragments = new Fragment[1];
        fragments[0] = new UserFragment();
        fragments[0].setArguments(param);
        notifyDataSetChanged();
    }


    public void setupMessagePage() {
        fragments = new Fragment[1];
        fragments[0] = new MessageFragment();
        notifyDataSetChanged();
    }


    public void setupRetweeterPage(long tweetId) {
        Bundle param = new Bundle();
        param.putLong(KEY_FRAG_USER_ID, tweetId);
        param.putInt(KEY_FRAG_USER_MODE, USER_FRAG_RETWEET);
        fragments = new Fragment[1];
        fragments[0] = new UserFragment();
        fragments[0].setArguments(param);
        notifyDataSetChanged();
    }


    public void setupListPage(long listId) {
        Bundle param = new Bundle();
        param.putLong(KEY_FRAG_LIST, listId);
        fragments = new Fragment[1];
        fragments[0] = new ListFragment();
        fragments[0].setArguments(param);
        notifyDataSetChanged();
    }


    public void setupSubscriberPage(long listId) {
        Bundle param = new Bundle();
        param.putLong(KEY_FRAG_USER_ID, listId);
        param.putInt(KEY_FRAG_USER_MODE, USER_FRAG_SUBSCR);
        fragments = new Fragment[1];
        fragments[0] = new UserFragment();
        fragments[0].setArguments(param);
        notifyDataSetChanged();
    }


    public void setupListContentPage(long listId) {
        Bundle tweetParam = new Bundle();
        Bundle userParam = new Bundle();
        tweetParam.putLong(KEY_FRAG_TWEET_ID, listId);
        userParam.putLong(KEY_FRAG_USER_ID, listId);
        tweetParam.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_LIST);
        userParam.putInt(KEY_FRAG_USER_MODE, USER_FRAG_LISTS);
        fragments = new Fragment[2];
        fragments[0] = new TweetFragment();
        fragments[1] = new UserFragment();
        fragments[0].setArguments(tweetParam);
        fragments[1].setArguments(userParam);
        notifyDataSetChanged();
    }

    /**
     * called when app settings change
     */
    public void notifySettingsChanged() {
        for (Fragment fragment : fragments) {
            if (fragment instanceof FragmentChangeObserver)
                ((FragmentChangeObserver) fragment).onSettingsChange();
        }
    }

    /**
     * called to scroll page to top
     * @param index tab position of page
     */
    public void scrollToTop(int index) {
        if (fragments[index] instanceof FragmentChangeObserver)
            ((FragmentChangeObserver) fragments[index]).onTabChange();
    }


    public interface FragmentChangeObserver {

        /**
         * called if settings changed to refresh fragments
         */
        void onSettingsChange();

        /**
         * called when the current tab changes
         */
        void onTabChange();
    }
}