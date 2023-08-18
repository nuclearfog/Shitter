package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.UserListFragment;

/**
 * ViewPager adapter for {@link org.nuclearfog.twidda.ui.activities.UserlistsActivity}
 *
 * @author nuclearfog
 */
public class UserListsAdapter extends ViewPagerAdapter {

	/**
	 * @param userId ID of the user related to the userlists
	 */
	public UserListsAdapter(FragmentActivity fragmentActivity, long userId) {
		super(fragmentActivity);

		ListFragment userlistOwnerShips = new UserListFragment();
		Bundle paramUserlistOwnership = new Bundle();
		paramUserlistOwnership.putLong(UserListFragment.KEY_ID, userId);
		paramUserlistOwnership.putInt(UserListFragment.KEY_MODE, UserListFragment.MODE_OWNERSHIP);
		userlistOwnerShips.setArguments(paramUserlistOwnership);
		fragments.add(userlistOwnerShips);

		if (settings.getLogin().getConfiguration().isUserlistMembershipSupported()) {
			ListFragment userlistSubscriptions = new UserListFragment();
			Bundle paramUserlistSubscription = new Bundle();
			paramUserlistSubscription.putLong(UserListFragment.KEY_ID, userId);
			paramUserlistSubscription.putInt(UserListFragment.KEY_MODE, UserListFragment.MODE_MEMBERSHIP);
			userlistSubscriptions.setArguments(paramUserlistSubscription);
			fragments.add(userlistSubscriptions);
		}
	}
}