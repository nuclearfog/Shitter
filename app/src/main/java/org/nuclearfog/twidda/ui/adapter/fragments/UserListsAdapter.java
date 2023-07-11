package org.nuclearfog.twidda.ui.adapter.fragments;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.UserListFragment;

/**
 * ViewPager adapter used to show userlists of an user
 * such as owned/subscribed lists and memberships
 *
 * @author nuclearfog
 */
public class UserListsAdapter extends ViewPagerAdapter {

	/**
	 *
	 */
	public UserListsAdapter(FragmentActivity fragmentActivity, long id) {
		super(fragmentActivity);

		ListFragment userlistOwnerShips = new UserListFragment();
		Bundle paramUserlistOwnership = new Bundle();
		paramUserlistOwnership.putLong(UserListFragment.KEY_ID, id);
		paramUserlistOwnership.putInt(UserListFragment.KEY_MODE, UserListFragment.MODE_OWNERSHIP);
		userlistOwnerShips.setArguments(paramUserlistOwnership);
		fragments.add(userlistOwnerShips);

		if (settings.getLogin().getConfiguration() == Configuration.TWITTER1 || settings.getLogin().getConfiguration() == Configuration.TWITTER2) {
			ListFragment userlistSubscriptions = new UserListFragment();
			Bundle paramUserlistSubscription = new Bundle();
			paramUserlistSubscription.putLong(UserListFragment.KEY_ID, id);
			paramUserlistSubscription.putInt(UserListFragment.KEY_MODE, UserListFragment.MODE_MEMBERSHIP);
			userlistSubscriptions.setArguments(paramUserlistSubscription);
			fragments.add(userlistSubscriptions);
		}
	}
}