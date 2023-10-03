package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;
import org.nuclearfog.twidda.ui.fragments.UserFragment;
import org.nuclearfog.twidda.ui.fragments.UserListFragment;

/**
 * Viewpager adapter for {@link org.nuclearfog.twidda.ui.activities.UserlistActivity}
 *
 * @author nuclearfog
 */
public class UserlistAdapter extends ViewPagerAdapter {

	private long id;

	/**
	 *
	 */
	public UserlistAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);
		GlobalSettings settings = GlobalSettings.get(fragmentActivity);
		if (settings.getLogin().getConfiguration().isUserlistSubscriberSupported()) {
			setPageCount(3);
		} else {
			setPageCount(2);
		}
	}


	@NonNull
	@Override
	public Fragment createFragment(int position) {
		ListFragment fragment;
		switch (position) {
			default:
			case 0:
				fragment = new StatusFragment();
				Bundle param = new Bundle();
				param.putLong(StatusFragment.KEY_ID, id);
				param.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_USERLIST);
				fragment.setArguments(param);
				break;

			case 1:
				fragment = new UserFragment();
				param = new Bundle();
				param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_LIST_MEMBER);
				param.putBoolean(UserFragment.KEY_DELETE, true);
				param.putLong(UserFragment.KEY_ID, id);
				fragment.setArguments(param);
				break;

			case 2:
				fragment = new UserListFragment();
				param = new Bundle();
				param.putLong(UserFragment.KEY_ID, id);
				param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_LIST_SUBSCRIBER);
				fragment.setArguments(param);
				break;
		}
		return fragment;
	}

	/**
	 *
	 */
	public void setId(long id) {
		this.id = id;
	}
}