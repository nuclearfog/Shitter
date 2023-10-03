package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.fragments.FieldFragment;
import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;

/**
 * ViewPager adapter for {@link org.nuclearfog.twidda.ui.activities.ProfileActivity}
 *
 * @author nuclearfog
 */
public class ProfileAdapter extends ViewPagerAdapter {

	private GlobalSettings settings;

	private long userId = 0L;

	/**
	 *
	 */
	public ProfileAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);
		settings = GlobalSettings.get(fragmentActivity);
	}


	@NonNull
	@Override
	public Fragment createFragment(int position) {
		ListFragment fragment;
		switch (position) {
			default:
			case 0:
				Bundle param = new Bundle();
				param.putLong(StatusFragment.KEY_ID, userId);
				param.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_USER);
				fragment = new StatusFragment();
				fragment.setArguments(param);
				break;

			case 1:
				if (getItemCount() == 2) {
					param = new Bundle();
					param.putLong(FieldFragment.KEY_ID, userId);
					fragment = new FieldFragment();
					fragment.setArguments(param);
				} else {
					param = new Bundle();
					param.putLong(StatusFragment.KEY_ID, userId);
					param.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_FAVORIT);
					fragment = new StatusFragment();
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

	/**
	 * set user ID of the profile
	 *
	 * @param userId user ID
	 */
	public void setId(long userId) {
		this.userId = userId;
		if (settings.getLogin().getId() == userId) {
			setPageCount(4);
		} else {
			setPageCount(2);
		}
	}
}