package org.nuclearfog.twidda.ui.adapter.viewpager;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.ui.activities.TagActivity;
import org.nuclearfog.twidda.ui.fragments.TagFragment;

/**
 * Viewpager adapter of {@link TagActivity}
 *
 * @author nuclearfog
 */
public class TagAdapter extends ViewPagerAdapter {

	/**
	 *
	 */
	public TagAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);
		setPageCount(3);
	}


	@NonNull
	@Override
	public Fragment createFragment(int position) {
		TagFragment fragment = new TagFragment();
		switch (position) {
			case 0:
				Bundle paramFollowedTags = new Bundle();
				paramFollowedTags.putInt(TagFragment.KEY_MODE, TagFragment.MODE_FOLLOW);
				fragment.setArguments(paramFollowedTags);
				break;

			case 1:
				Bundle paramFeaturedTags = new Bundle();
				paramFeaturedTags.putInt(TagFragment.KEY_MODE, TagFragment.MODE_FEATURE);
				fragment.setArguments(paramFeaturedTags);
				break;

			case 2:
				Bundle paramSuggestedTags = new Bundle();
				paramSuggestedTags.putInt(TagFragment.KEY_MODE, TagFragment.MODE_SUGGESTIONS);
				fragment.setArguments(paramSuggestedTags);
				break;
		}
		return fragment;
	}
}