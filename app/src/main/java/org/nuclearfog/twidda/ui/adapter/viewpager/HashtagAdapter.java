package org.nuclearfog.twidda.ui.adapter.viewpager;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.ui.fragments.HashtagFragment;

/**
 * Viewpager adapter of {@link org.nuclearfog.twidda.ui.activities.HashtagActivity}
 *
 * @author nuclearfog
 */
public class HashtagAdapter extends ViewPagerAdapter {

	private static final int COUNT = 3;

	/**
	 *
	 */
	public HashtagAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity, COUNT);
	}


	@NonNull
	@Override
	public Fragment createFragment(int position) {
		HashtagFragment fragment = new HashtagFragment();
		switch(position) {
			case 0:
				Bundle paramFollowedTags = new Bundle();
				paramFollowedTags.putInt(HashtagFragment.KEY_MODE, HashtagFragment.MODE_FOLLOW);
				fragment.setArguments(paramFollowedTags);
				break;

			case 1:
				Bundle paramFeaturedTags = new Bundle();
				paramFeaturedTags.putInt(HashtagFragment.KEY_MODE, HashtagFragment.MODE_FEATURE);
				fragment.setArguments(paramFeaturedTags);
				break;

			case 2:
				Bundle paramSuggestedTags = new Bundle();
				paramSuggestedTags.putInt(HashtagFragment.KEY_MODE, HashtagFragment.MODE_SUGGESTIONS);
				fragment.setArguments(paramSuggestedTags);
				break;
		}
		return fragment;
	}
}