package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.ui.fragments.FieldFragment;
import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;

/**
 * ViewPager adapter for {@link org.nuclearfog.twidda.ui.activities.ProfileActivity}
 *
 * @author nuclearfog
 */
public class ProfileAdapter extends ViewPagerAdapter {

	private long userId;
	private boolean isCurrentUser;

	/**
	 * @param userId ID of the user (profile ID)
	 */
	public ProfileAdapter(FragmentActivity fragmentActivity, long userId, boolean isCurrentUser) {
		super(fragmentActivity, isCurrentUser ? 4 : 2);
		this.isCurrentUser = isCurrentUser;
		this.userId = userId;
	}


	@NonNull
	@Override
	public Fragment createFragment(int position) {
		ListFragment fragment;
		switch(position) {
			default:
			case 0:
				Bundle param = new Bundle();
				param.putLong(StatusFragment.KEY_ID, userId);
				param.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_USER);
				fragment = new StatusFragment();
				fragment.setArguments(param);
				break;

			case 1:
				if (isCurrentUser) {
					param = new Bundle();
					param.putLong(StatusFragment.KEY_ID, userId);
					param.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_FAVORIT);
					fragment = new StatusFragment();
					fragment.setArguments(param);
				} else {
					param = new Bundle();
					param.putLong(FieldFragment.KEY_ID, userId);
					fragment = new FieldFragment();
					fragment.setArguments(param);
				}
				break;

			case 2:
				param = new Bundle();
				param.putLong(StatusFragment.KEY_ID, userId);
				param.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_BOOKMARK);
				fragment = new StatusFragment();
				fragment.setArguments(param);
				break;

			case 3:
				param = new Bundle();
				param.putLong(FieldFragment.KEY_ID, userId);
				fragment = new FieldFragment();
				fragment.setArguments(param);
				break;
		}
		return fragment;
	}
}