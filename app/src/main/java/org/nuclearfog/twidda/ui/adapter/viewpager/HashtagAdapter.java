package org.nuclearfog.twidda.ui.adapter.viewpager;


import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.ui.fragments.HashtagFragment;

/**
 * Viewpager adapter of {@link org.nuclearfog.twidda.ui.activities.HashtagActivity}
 *
 * @author nuclearfog
 */
public class HashtagAdapter extends ViewPagerAdapter {

	/**
	 */
	public HashtagAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);

		HashtagFragment followedTags = new HashtagFragment();
		Bundle paramFollowedTags = new Bundle();
		paramFollowedTags.putInt(HashtagFragment.KEY_MODE, HashtagFragment.MODE_FOLLOW);
		followedTags.setArguments(paramFollowedTags);

		HashtagFragment featuredTags = new HashtagFragment();
		Bundle paramFeaturedTags = new Bundle();
		paramFeaturedTags.putInt(HashtagFragment.KEY_MODE, HashtagFragment.MODE_FEATURE);
		featuredTags.setArguments(paramFeaturedTags);

		fragments.add(followedTags);
		fragments.add(featuredTags);
	}
}