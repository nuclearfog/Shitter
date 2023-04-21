package org.nuclearfog.twidda.ui.adapter;

import static org.nuclearfog.twidda.ui.fragments.StatusFragment.KEY_STATUS_FRAGMENT_ID;
import static org.nuclearfog.twidda.ui.fragments.StatusFragment.KEY_STATUS_FRAGMENT_MODE;
import static org.nuclearfog.twidda.ui.fragments.StatusFragment.KEY_STATUS_FRAGMENT_SEARCH;
import static org.nuclearfog.twidda.ui.fragments.StatusFragment.STATUS_FRAGMENT_BOOKMARK;
import static org.nuclearfog.twidda.ui.fragments.StatusFragment.STATUS_FRAGMENT_FAVORIT;
import static org.nuclearfog.twidda.ui.fragments.StatusFragment.STATUS_FRAGMENT_HOME;
import static org.nuclearfog.twidda.ui.fragments.StatusFragment.STATUS_FRAGMENT_PUBLIC;
import static org.nuclearfog.twidda.ui.fragments.StatusFragment.STATUS_FRAGMENT_SEARCH;
import static org.nuclearfog.twidda.ui.fragments.StatusFragment.STATUS_FRAGMENT_USER;
import static org.nuclearfog.twidda.ui.fragments.StatusFragment.STATUS_FRAGMENT_USERLIST;
import static org.nuclearfog.twidda.ui.fragments.TrendFragment.KEY_FRAGMENT_TREND_SEARCH;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.KEY_FRAG_DEL_USER;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.KEY_FRAG_USER_ID;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.KEY_FRAG_USER_MODE;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.KEY_FRAG_USER_SEARCH;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.USER_FRAG_BLOCKED_USERS;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.USER_FRAG_FAVORIT;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.USER_FRAG_FOLLOWER;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.USER_FRAG_FOLLOWING;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.USER_FRAG_FOLLOW_INCOMING;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.USER_FRAG_FOLLOW_OUTGOING;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.USER_FRAG_LIST_MEMBERS;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.USER_FRAG_LIST_SUBSCRIBER;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.USER_FRAG_MUTED_USERS;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.USER_FRAG_REPOST;
import static org.nuclearfog.twidda.ui.fragments.UserFragment.USER_FRAG_SEARCH;
import static org.nuclearfog.twidda.ui.fragments.UserListFragment.KEY_FRAGMENT_USERLIST_OWNER_ID;
import static org.nuclearfog.twidda.ui.fragments.UserListFragment.KEY_FRAGMENT_USERLIST_TYPE;
import static org.nuclearfog.twidda.ui.fragments.UserListFragment.LIST_USER_OWNS;
import static org.nuclearfog.twidda.ui.fragments.UserListFragment.LIST_USER_SUBSCR_TO;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.MessageFragment;
import org.nuclearfog.twidda.ui.fragments.NotificationFragment;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;
import org.nuclearfog.twidda.ui.fragments.TrendFragment;
import org.nuclearfog.twidda.ui.fragments.UserFragment;
import org.nuclearfog.twidda.ui.fragments.UserListFragment;

/**
 * custom adapter used for {@link androidx.viewpager2.widget.ViewPager2}
 *
 * @author nuclearfog
 */
public class FragmentAdapter extends FragmentStateAdapter {

	private ListFragment[] fragments = {};
	private GlobalSettings settings;

	/**
	 * @param fragmentActivity fragment activity
	 */
	public FragmentAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);
		settings = GlobalSettings.getInstance(fragmentActivity.getApplicationContext());
	}


	@Override
	public long getItemId(int position) {
		return fragments[position].getSessionId();
	}


	@Override
	public boolean containsItem(long itemId) {
		for (ListFragment fragment : fragments) {
			if (fragment.getSessionId() == itemId)
				return true;
		}
		return false;
	}


	@NonNull
	@Override
	public Fragment createFragment(int position) {
		return fragments[position];
	}


	@Override
	public int getItemCount() {
		return fragments.length;
	}

	/**
	 * get fragment at index
	 *
	 * @param index index of the fragment
	 * @return fragment
	 */
	public ListFragment getItem(int index) {
		if (index >= 0 && index < fragments.length) {
			return fragments[index];
		}
		return null;
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
		Bundle paramHomeTl = new Bundle();
		Bundle parampublicTl = new Bundle();
		paramHomeTl.putInt(KEY_STATUS_FRAGMENT_MODE, STATUS_FRAGMENT_HOME);
		parampublicTl.putInt(KEY_STATUS_FRAGMENT_MODE, STATUS_FRAGMENT_PUBLIC);

		switch (settings.getLogin().getConfiguration()) {
			case TWITTER1:
			case TWITTER2:
				fragments = new ListFragment[4];
				fragments[0] = new StatusFragment();
				fragments[1] = new TrendFragment();
				fragments[2] = new NotificationFragment();
				fragments[3] = new MessageFragment();
				fragments[0].setArguments(paramHomeTl);
				break;

			case MASTODON:
				fragments = new ListFragment[4];
				fragments[0] = new StatusFragment();
				fragments[1] = new TrendFragment();
				fragments[2] = new StatusFragment();
				fragments[3] = new NotificationFragment();
				fragments[0].setArguments(paramHomeTl);
				fragments[2].setArguments(parampublicTl);
				break;

			default:
				fragments = new ListFragment[0];
				break;
		}
		notifyDataSetChanged();
	}

	/**
	 * setup adapter for viewing user timeline and favorites
	 *
	 * @param userId ID of the user
	 */
	public void setupProfilePage(long userId) {
		Bundle paramTimeline = new Bundle();
		Bundle paramFavorite = new Bundle();
		Bundle paramBookmark = new Bundle();
		paramTimeline.putLong(KEY_STATUS_FRAGMENT_ID, userId);
		paramFavorite.putLong(KEY_STATUS_FRAGMENT_ID, userId);
		paramBookmark.putLong(KEY_STATUS_FRAGMENT_ID, userId);
		paramTimeline.putInt(KEY_STATUS_FRAGMENT_MODE, STATUS_FRAGMENT_USER);
		paramFavorite.putInt(KEY_STATUS_FRAGMENT_MODE, STATUS_FRAGMENT_FAVORIT);
		paramBookmark.putInt(KEY_STATUS_FRAGMENT_MODE, STATUS_FRAGMENT_BOOKMARK);

		Account login = settings.getLogin();
		switch (login.getConfiguration()) {
			case MASTODON:
				if (login.getId() == userId) {
					fragments = new ListFragment[3];
					fragments[1] = new StatusFragment();
					fragments[2] = new StatusFragment();
					fragments[1].setArguments(paramFavorite);
					fragments[2].setArguments(paramBookmark);
				} else {
					fragments = new ListFragment[1];
				}
				fragments[0] = new StatusFragment();
				fragments[0].setArguments(paramTimeline);
				break;

			case TWITTER1:
			case TWITTER2:
				fragments = new ListFragment[2];
				fragments[0] = new StatusFragment();
				fragments[0].setArguments(paramTimeline);
				fragments[1] = new StatusFragment();
				fragments[1].setArguments(paramFavorite);
				break;

			default:
				fragments = new ListFragment[0];
				break;
		}
		notifyDataSetChanged();
	}

	/**
	 * setup adapter for search for status and user search
	 *
	 * @param search         Search string
	 * @param includeHashtag add hashtag search fragment
	 */
	public void setupSearchPage(String search, boolean includeHashtag) {
		Bundle paramStatuses = new Bundle();
		Bundle paramUsers = new Bundle();
		Bundle paramTrend = new Bundle();
		paramStatuses.putString(KEY_STATUS_FRAGMENT_SEARCH, search);
		paramUsers.putString(KEY_FRAG_USER_SEARCH, search);
		paramStatuses.putInt(KEY_STATUS_FRAGMENT_MODE, STATUS_FRAGMENT_SEARCH);
		paramUsers.putInt(KEY_FRAG_USER_MODE, USER_FRAG_SEARCH);
		paramTrend.putString(KEY_FRAGMENT_TREND_SEARCH, search);
		if (includeHashtag) {
			fragments = new ListFragment[3];
			fragments[2] = new TrendFragment();
			fragments[2].setArguments(paramTrend);
		} else {
			fragments = new ListFragment[2];
		}
		fragments[0] = new StatusFragment();
		fragments[1] = new UserFragment();
		fragments[0].setArguments(paramStatuses);
		fragments[1].setArguments(paramUsers);
		notifyDataSetChanged();
	}

	/**
	 * setup adapter for a list of user lists created by an user
	 *
	 * @param userId ID of the user
	 */
	public void setupListPage(long userId) {
		Bundle paramUserlistOwnership = new Bundle();
		Bundle paramUserlistSubscription = new Bundle();
		paramUserlistOwnership.putLong(KEY_FRAGMENT_USERLIST_OWNER_ID, userId);
		paramUserlistSubscription.putLong(KEY_FRAGMENT_USERLIST_OWNER_ID, userId);
		paramUserlistOwnership.putInt(KEY_FRAGMENT_USERLIST_TYPE, LIST_USER_OWNS);
		paramUserlistSubscription.putInt(KEY_FRAGMENT_USERLIST_TYPE, LIST_USER_SUBSCR_TO);

		switch (settings.getLogin().getConfiguration()) {
			case TWITTER1:
			case TWITTER2:
				fragments = new ListFragment[2];
				fragments[0] = new UserListFragment();
				fragments[1] = new UserListFragment();
				fragments[0].setArguments(paramUserlistOwnership);
				fragments[1].setArguments(paramUserlistSubscription);
				break;

			case MASTODON:
				fragments = new ListFragment[1];
				fragments[0] = new UserListFragment();
				fragments[0].setArguments(paramUserlistOwnership);
				break;

			default:
				fragments = new ListFragment[0];
				break;
		}
		notifyDataSetChanged();
	}

	/**
	 * setup adapter for a page of statuses and users in an user list
	 *
	 * @param listId      ID of an user list
	 * @param ownerOfList true if current user owns this list
	 */
	public void setupListContentPage(long listId, boolean ownerOfList) {
		Bundle paramUserlistTl = new Bundle();
		Bundle paramUserlistMember = new Bundle();
		Bundle paramUserlistSubscriber = new Bundle();
		paramUserlistTl.putLong(KEY_STATUS_FRAGMENT_ID, listId);
		paramUserlistTl.putInt(KEY_STATUS_FRAGMENT_MODE, STATUS_FRAGMENT_USERLIST);
		paramUserlistMember.putInt(KEY_FRAG_USER_MODE, USER_FRAG_LIST_MEMBERS);
		paramUserlistMember.putBoolean(KEY_FRAG_DEL_USER, ownerOfList);
		paramUserlistMember.putLong(KEY_FRAG_USER_ID, listId);
		paramUserlistSubscriber.putLong(KEY_FRAG_USER_ID, listId);
		paramUserlistSubscriber.putInt(KEY_FRAG_USER_MODE, USER_FRAG_LIST_SUBSCRIBER);

		switch (settings.getLogin().getConfiguration()) {
			case TWITTER1:
			case TWITTER2:
				fragments = new ListFragment[3];
				fragments[0] = new StatusFragment();
				fragments[1] = new UserFragment();
				fragments[2] = new UserFragment();
				fragments[0].setArguments(paramUserlistTl);
				fragments[1].setArguments(paramUserlistMember);
				fragments[2].setArguments(paramUserlistSubscriber);
				break;

			case MASTODON:
				fragments = new ListFragment[2];
				fragments[0] = new StatusFragment();
				fragments[1] = new UserFragment();
				fragments[0].setArguments(paramUserlistTl);
				fragments[1].setArguments(paramUserlistMember);
				break;

			default:
				fragments = new ListFragment[0];
				break;
		}
		notifyDataSetChanged();
	}

	/**
	 * setup adapter for a page of muted / blocked users
	 */
	public void setupMuteBlockPage() {
		Bundle paramMuteList = new Bundle();
		Bundle paramBlockList = new Bundle();
		paramMuteList.putInt(KEY_FRAG_USER_MODE, USER_FRAG_MUTED_USERS);
		paramBlockList.putInt(KEY_FRAG_USER_MODE, USER_FRAG_BLOCKED_USERS);
		fragments = new ListFragment[2];
		fragments[0] = new UserFragment();
		fragments[1] = new UserFragment();
		fragments[0].setArguments(paramMuteList);
		fragments[1].setArguments(paramBlockList);
		notifyDataSetChanged();
	}

	/**
	 * setup adapter to show follow requesting users
	 */
	public void setupFollowRequestPage() {
		Bundle paramFollowing = new Bundle();
		Bundle paramFollower = new Bundle();
		paramFollowing.putInt(KEY_FRAG_USER_MODE, USER_FRAG_FOLLOW_INCOMING);
		paramFollower.putInt(KEY_FRAG_USER_MODE, USER_FRAG_FOLLOW_OUTGOING);
		fragments = new ListFragment[2];
		fragments[0] = new UserFragment();
		fragments[1] = new UserFragment();
		fragments[0].setArguments(paramFollower);
		fragments[1].setArguments(paramFollowing);
		notifyDataSetChanged();
	}

	/**
	 * setup adapter to show "following" of an user
	 *
	 * @param userId ID of the user
	 */
	public void setupFollowingPage(long userId) {
		Bundle paramFollowing = new Bundle();
		paramFollowing.putLong(KEY_FRAG_USER_ID, userId);
		paramFollowing.putInt(KEY_FRAG_USER_MODE, USER_FRAG_FOLLOWING);
		fragments = new ListFragment[1];
		fragments[0] = new UserFragment();
		fragments[0].setArguments(paramFollowing);
		notifyDataSetChanged();
	}

	/**
	 * setup adapter to show "follower" of an user
	 *
	 * @param userId ID of the user
	 */
	public void setupFollowerPage(long userId) {
		Bundle paramFollower = new Bundle();
		paramFollower.putLong(KEY_FRAG_USER_ID, userId);
		paramFollower.putInt(KEY_FRAG_USER_MODE, USER_FRAG_FOLLOWER);
		fragments = new ListFragment[1];
		fragments[0] = new UserFragment();
		fragments[0].setArguments(paramFollower);
		notifyDataSetChanged();
	}

	/**
	 * setup adapter to show users reposting a status
	 *
	 * @param id ID of the status
	 */
	public void setupReposterPage(long id) {
		Bundle paramReposter = new Bundle();
		paramReposter.putLong(KEY_FRAG_USER_ID, id);
		paramReposter.putInt(KEY_FRAG_USER_MODE, USER_FRAG_REPOST);
		fragments = new ListFragment[1];
		fragments[0] = new UserFragment();
		fragments[0].setArguments(paramReposter);
		notifyDataSetChanged();
	}

	/**
	 * setup adapter to show users liking a status
	 *
	 * @param id ID of the status
	 */
	public void setFavoriterPage(long id) {
		Bundle paramFavoriter = new Bundle();
		paramFavoriter.putInt(KEY_FRAG_USER_MODE, USER_FRAG_FAVORIT);
		paramFavoriter.putLong(KEY_FRAG_USER_ID, id);
		fragments = new ListFragment[1];
		fragments[0] = new UserFragment();
		fragments[0].setArguments(paramFavoriter);
		notifyDataSetChanged();
	}

	/**
	 * called when app settings change
	 */
	public void notifySettingsChanged() {
		for (ListFragment fragment : fragments) {
			if (!fragment.isDetached()) {
				fragment.reset();
			}
		}
	}

	/**
	 * called to scroll page to top
	 *
	 * @param index tab position of page
	 */
	public void scrollToTop(int index) {
		if (!fragments[index].isDetached()) {
			fragments[index].onTabChange();
		}
	}
}