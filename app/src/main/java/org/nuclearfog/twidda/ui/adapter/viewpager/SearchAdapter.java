package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.ui.fragments.HashtagFragment;
import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;
import org.nuclearfog.twidda.ui.fragments.UserFragment;

/**
 * ViewPager fragment adapter for {@link org.nuclearfog.twidda.ui.activities.SearchActivity}
 *
 * @author nuclearfog
 */
public class SearchAdapter extends ViewPagerAdapter {

	private String search;

	/**
	 * @param search search string
	 */
	public SearchAdapter(FragmentActivity fragmentActivity, String search) {
		super(fragmentActivity, search.startsWith("#") ? 2 : 3);
		this.search = search;
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
				param.putString(StatusFragment.KEY_SEARCH, search);
				param.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_SEARCH);
				fragment.setArguments(param);
				break;

			case 1:
				fragment = new UserFragment();
				param = new Bundle();
				param.putString(UserFragment.KEY_SEARCH, search);
				param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_SEARCH);
				fragment.setArguments(param);
				break;

			case 2:
				fragment = new HashtagFragment();
				param = new Bundle();
				param.putInt(HashtagFragment.KEY_MODE, HashtagFragment.MODE_SEARCH);
				param.putString(HashtagFragment.KEY_SEARCH, search);
				fragment.setArguments(param);
				break;
		}
		return fragment;
	}
}