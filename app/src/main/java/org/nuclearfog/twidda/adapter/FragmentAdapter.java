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

import static org.nuclearfog.twidda.fragment.ListFragment.KEY_FRAG_LIST_OWNER_ID;
import static org.nuclearfog.twidda.fragment.ListFragment.KEY_FRAG_LIST_OWNER_NAME;
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

/**
 * Fragment adapter for ViewPager
 */
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
        fragments = new Fragment[0];
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
        fragments = new Fragment[3];
        fragments[0] = new TweetFragment();
        fragments[1] = new TrendFragment();
        fragments[2] = new TweetFragment();
        fragments[0].setArguments(home_tl);
        fragments[2].setArguments(ment_tl);
        notifyDataSetChanged();
    }

    /**
     * setup adapter for viewing user tweets and favs
     *
     * @param userId ID of the user
     */
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

    /**
     * setup adapter for viewing user tweets and favs
     *
     * @param username screen name of the user
     */
    public void setupProfilePage(String username) {
        Bundle usr_tweet = new Bundle();
        Bundle usr_favor = new Bundle();
        usr_tweet.putString(KEY_FRAG_TWEET_SEARCH, username);
        usr_favor.putString(KEY_FRAG_TWEET_SEARCH, username);
        usr_tweet.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_TWEETS);
        usr_favor.putInt(KEY_FRAG_TWEET_MODE, TWEET_FRAG_FAVORS);
        fragments = new Fragment[2];
        fragments[0] = new TweetFragment();
        fragments[1] = new TweetFragment();
        fragments[0].setArguments(usr_tweet);
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
        fragments = new Fragment[2];
        fragments[0] = new TweetFragment();
        fragments[1] = new UserFragment();
        fragments[0].setArguments(tweetSearch);
        fragments[1].setArguments(userSearch);
        notifyDataSetChanged();
    }

    /**
     * setup adapter for tweet page to show replies of a tweet
     *
     * @param tweetId   ID of the tweet
     * @param replyName screen name of the author
     */
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

    /**
     * setup adapter for a list of friends
     *
     * @param userId ID of the user
     */
    public void setupFriendsPage(long userId) {
        Bundle param = new Bundle();
        param.putLong(KEY_FRAG_USER_ID, userId);
        param.putInt(KEY_FRAG_USER_MODE, USER_FRAG_FRIENDS);
        fragments = new Fragment[1];
        fragments[0] = new UserFragment();
        fragments[0].setArguments(param);
        notifyDataSetChanged();
    }

    /**
     * setup adapter for a list of follower
     *
     * @param userId ID of the user
     */
    public void setupFollowerPage(long userId) {
        Bundle param = new Bundle();
        param.putLong(KEY_FRAG_USER_ID, userId);
        param.putInt(KEY_FRAG_USER_MODE, USER_FRAG_FOLLOWS);
        fragments = new Fragment[1];
        fragments[0] = new UserFragment();
        fragments[0].setArguments(param);
        notifyDataSetChanged();
    }

    /**
     * setup adapter for a list of direct messages
     */
    public void setupMessagePage() {
        fragments = new Fragment[1];
        fragments[0] = new MessageFragment();
        notifyDataSetChanged();
    }

    /**
     * setup adapter for a list of users which retweets a tweet
     *
     * @param tweetId ID if the tweet
     */
    public void setupRetweeterPage(long tweetId) {
        Bundle param = new Bundle();
        param.putLong(KEY_FRAG_USER_ID, tweetId);
        param.putInt(KEY_FRAG_USER_MODE, USER_FRAG_RETWEET);
        fragments = new Fragment[1];
        fragments[0] = new UserFragment();
        fragments[0].setArguments(param);
        notifyDataSetChanged();
    }

    /**
     * setup adapter for a list of user lists created by an user
     *
     * @param userId ID of the user
     */
    public void setupListPage(long userId) {
        Bundle param = new Bundle();
        param.putLong(KEY_FRAG_LIST_OWNER_ID, userId);
        fragments = new Fragment[1];
        fragments[0] = new ListFragment();
        fragments[0].setArguments(param);
        notifyDataSetChanged();
    }

    /**
     * setup adapter for a list of user lists created by an user
     *
     * @param ownerName screen name of the owner
     */
    public void setupListPage(String ownerName) {
        Bundle param = new Bundle();
        param.putString(KEY_FRAG_LIST_OWNER_NAME, ownerName);
        fragments = new Fragment[1];
        fragments[0] = new ListFragment();
        fragments[0].setArguments(param);
        notifyDataSetChanged();
    }

    /**
     * setup adapter for a list of users subscribing an user list
     *
     * @param listId ID of an user list
     */
    public void setupSubscriberPage(long listId) {
        Bundle param = new Bundle();
        param.putLong(KEY_FRAG_USER_ID, listId);
        param.putInt(KEY_FRAG_USER_MODE, USER_FRAG_SUBSCR);
        fragments = new Fragment[1];
        fragments[0] = new UserFragment();
        fragments[0].setArguments(param);
        notifyDataSetChanged();
    }

    /**
     * setup adapter for a page of tweets and users in an user list
     *
     * @param listId ID of an user list
     */
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
            if (fragment instanceof FragmentChangeObserver) {
                ((FragmentChangeObserver) fragment).onReset();
            }
        }
    }

    /**
     * called to scroll page to top
     * @param index tab position of page
     */
    public void scrollToTop(int index) {
        if (fragments[index] instanceof FragmentChangeObserver) {
            ((FragmentChangeObserver) fragments[index]).onTabChange();
        }
    }


    public interface FragmentChangeObserver {

        /**
         * called if settings changed to refresh fragments
         */
        void onReset();

        /**
         * called when the current tab changes
         */
        void onTabChange();
    }
}