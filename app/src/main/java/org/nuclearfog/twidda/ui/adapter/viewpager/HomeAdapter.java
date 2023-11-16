package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.NotificationFragment;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;
import org.nuclearfog.twidda.ui.fragments.TagFragment;

/**
 * ViewPager fragment adapter for {@link org.nuclearfog.twidda.ui.activities.MainActivity}
 *
 * @author nuclearfog
 */
public class HomeAdapter extends ViewPagerAdapter {

	/**
	 *
	 */
	public HomeAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);
		setPageCount(4);
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
				fragment = new TagFragment();
				param = new Bundle();
				param.putInt(TagFragment.KEY_MODE, TagFragment.MODE_POPULAR);
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