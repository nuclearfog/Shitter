package org.nuclearfog.twidda.ui.adapter.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.ui.fragments.DomainFragment;
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
		settings = GlobalSettings.get(fragmentActivity.getApplicationContext());
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
		Bundle paramTrend = new Bundle();
		Bundle paramHomeTimeline = new Bundle();
		Bundle parampublicTimeline = new Bundle();
		paramHomeTimeline.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_HOME);
		parampublicTimeline.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_PUBLIC);
		paramTrend.putInt(TrendFragment.KEY_MODE, TrendFragment.MODE_POPULAR);

		switch (settings.getLogin().getConfiguration()) {
			case TWITTER1:
			case TWITTER2:
				fragments = new ListFragment[4];
				fragments[0] = new StatusFragment();
				fragments[1] = new TrendFragment();
				fragments[2] = new NotificationFragment();
				fragments[3] = new MessageFragment();
				fragments[0].setArguments(paramHomeTimeline);
				fragments[1].setArguments(paramTrend);
				break;

			case MASTODON:
				fragments = new ListFragment[4];
				fragments[0] = new StatusFragment();
				fragments[1] = new TrendFragment();
				fragments[2] = new StatusFragment();
				fragments[3] = new NotificationFragment();
				fragments[0].setArguments(paramHomeTimeline);
				fragments[1].setArguments(paramTrend);
				fragments[2].setArguments(parampublicTimeline);
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
		paramTimeline.putLong(StatusFragment.KEY_ID, userId);
		paramFavorite.putLong(StatusFragment.KEY_ID, userId);
		paramBookmark.putLong(StatusFragment.KEY_ID, userId);
		paramTimeline.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_USER);
		paramFavorite.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_FAVORIT);
		paramBookmark.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_BOOKMARK);

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
		paramStatuses.putString(StatusFragment.KEY_SEARCH, search);
		paramUsers.putString(UserFragment.KEY_SEARCH, search);
		paramStatuses.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_SEARCH);
		paramUsers.putInt(UserFragment.KEY_MODE, UserFragment.MODE_SEARCH);
		paramTrend.putInt(TrendFragment.KEY_MODE, TrendFragment.MODE_SEARCH);
		paramTrend.putString(TrendFragment.KEY_SEARCH, search);
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
		paramUserlistOwnership.putLong(UserListFragment.KEY_ID, userId);
		paramUserlistSubscription.putLong(UserListFragment.KEY_ID, userId);
		paramUserlistOwnership.putInt(UserListFragment.KEY_MODE, UserListFragment.MODE_OWNERSHIP);
		paramUserlistSubscription.putInt(UserListFragment.KEY_MODE, UserListFragment.MODE_MEMBERSHIP);

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
		paramUserlistTl.putLong(StatusFragment.KEY_ID, listId);
		paramUserlistTl.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_USERLIST);
		paramUserlistMember.putInt(UserFragment.KEY_MODE, UserFragment.MODE_LIST_MEMBER);
		paramUserlistMember.putBoolean(UserFragment.KEY_DELETE, ownerOfList);
		paramUserlistMember.putLong(UserFragment.KEY_ID, listId);
		paramUserlistSubscriber.putLong(UserFragment.KEY_ID, listId);
		paramUserlistSubscriber.putInt(UserFragment.KEY_MODE, UserFragment.MODE_LIST_SUBSCRIBER);

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
	public void setupBlockPage(boolean enableDomainBlock) {
		Bundle paramMuteList = new Bundle();
		Bundle paramBlockList = new Bundle();
		paramMuteList.putInt(UserFragment.KEY_MODE, UserFragment.MODE_MUTES);
		paramBlockList.putInt(UserFragment.KEY_MODE, UserFragment.MODE_BLOCKS);
		if (enableDomainBlock) {
			fragments = new ListFragment[3];
			fragments[2] = new DomainFragment();
		} else {
			fragments = new ListFragment[2];
		}
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
		paramFollowing.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOW_INCOMING);
		paramFollower.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOW_OUTGOING);
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
	public void setupFollowingPage(long userId, boolean addTagPage) {
		if (addTagPage) {
			Bundle paramTrend = new Bundle();
			paramTrend.putInt(TrendFragment.KEY_MODE, TrendFragment.MODE_FOLLOW);
			fragments = new ListFragment[2];
			fragments[1] = new TrendFragment();
			fragments[1].setArguments(paramTrend);
		} else {
			fragments = new ListFragment[1];
		}
		Bundle paramFollowing = new Bundle();
		paramFollowing.putLong(UserFragment.KEY_ID, userId);
		paramFollowing.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOWING);
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
		paramFollower.putLong(UserFragment.KEY_ID, userId);
		paramFollower.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOWER);
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
		paramReposter.putLong(UserFragment.KEY_ID, id);
		paramReposter.putInt(UserFragment.KEY_MODE, UserFragment.MODE_REPOSTER);
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
		paramFavoriter.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FAVORITER);
		paramFavoriter.putLong(UserFragment.KEY_ID, id);
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