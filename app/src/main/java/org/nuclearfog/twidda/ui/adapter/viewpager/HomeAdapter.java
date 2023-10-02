package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.ui.fragments.HashtagFragment;
import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.NotificationFragment;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;

/**
 * ViewPager fragment adapter for {@link org.nuclearfog.twidda.ui.activities.MainActivity}
 *
 * @author nuclearfog
 */
public class HomeAdapter extends ViewPagerAdapter {

	private static final int SIZE = 4;

	/**
	 *
	 */
	public HomeAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity, SIZE);
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
				param.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_HOME);
				fragment.setArguments(param);
				break;

			case 1:
				fragment = new HashtagFragment();
				param = new Bundle();
				param.putInt(HashtagFragment.KEY_MODE, HashtagFragment.MODE_POPULAR);
				fragment.setArguments(param);
				break;

			case 2:
				fragment = new StatusFragment();
				param = new Bundle();
				param.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_PUBLIC);
				fragment.setArguments(param);
				break;

			case 3:
				fragment = new NotificationFragment();
				break;
		}
		return fragment;
	}
}