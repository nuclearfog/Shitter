package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.UserListFragment;

/**
 * ViewPager adapter for {@link org.nuclearfog.twidda.ui.activities.UserlistsActivity}
 *
 * @author nuclearfog
 */
public class UserListsAdapter extends ViewPagerAdapter {

	private long userId;

	/**
	 *
	 */
	public UserListsAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);
	}


	@NonNull
	@Override
	public Fragment createFragment(int position) {
		ListFragment fragment;
		switch (position) {
			default:
			case 0:
				fragment = new UserListFragment();
				Bundle param = new Bundle();
				param.putLong(UserListFragment.KEY_ID, userId);
				param.putInt(UserListFragment.KEY_MODE, UserListFragment.MODE_OWNERSHIP);
				fragment.setArguments(param);
				break;

			case 1:
				fragment = new UserListFragment();
				param = new Bundle();
				param.putLong(UserListFragment.KEY_ID, userId);
				param.putInt(UserListFragment.KEY_MODE, UserListFragment.MODE_MEMBERSHIP);
				fragment.setArguments(param);
				break;
		}
		return fragment;
	}

	/**
	 *
	 */
	public void setId(long userId) {
		this.userId = userId;
	}
}