package org.nuclearfog.twidda.ui.adapter.viewpager;

import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.ui.fragments.ScheduleFragment;

/**
 * @author nuclearfog
 */
public class ScheduleAdapter extends ViewPagerAdapter {

	/**
	 *
	 */
	public ScheduleAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);
		fragments.add(new ScheduleFragment());
	}
}