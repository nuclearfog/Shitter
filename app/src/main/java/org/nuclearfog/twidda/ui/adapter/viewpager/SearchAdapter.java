package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;
import org.nuclearfog.twidda.ui.fragments.HashtagFragment;
import org.nuclearfog.twidda.ui.fragments.UserFragment;

/**
 * ViewPager fragment adapter for {@link org.nuclearfog.twidda.ui.activities.SearchActivity}
 *
 * @author nuclearfog
 */
public class SearchAdapter extends ViewPagerAdapter {

	/**
	 * @param search search string
	 */
	public SearchAdapter(FragmentActivity fragmentActivity, String search) {
		super(fragmentActivity);

		ListFragment statusFragment = new StatusFragment();
		Bundle paramStatuses = new Bundle();
		paramStatuses.putString(StatusFragment.KEY_SEARCH, search);
		paramStatuses.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_SEARCH);
		statusFragment.setArguments(paramStatuses);

		ListFragment userFragment = new UserFragment();
		Bundle paramUsers = new Bundle();
		paramUsers.putString(UserFragment.KEY_SEARCH, search);
		paramUsers.putInt(UserFragment.KEY_MODE, UserFragment.MODE_SEARCH);
		userFragment.setArguments(paramUsers);

		fragments.add(statusFragment);
		fragments.add(userFragment);
		if (!search.startsWith("#")) {
			ListFragment hashtagFragment = new HashtagFragment();
			Bundle paramHashtag = new Bundle();
			paramHashtag.putInt(HashtagFragment.KEY_MODE, HashtagFragment.MODE_SEARCH);
			paramHashtag.putString(HashtagFragment.KEY_SEARCH, search);
			hashtagFragment.setArguments(paramHashtag);
			fragments.add(hashtagFragment);
		}
	}
}