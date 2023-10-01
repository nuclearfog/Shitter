package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

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

	/**
	 *
	 */
	public HomeAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);

		ListFragment homeTimeline = new StatusFragment();
		Bundle paramHomeTimeline = new Bundle();
		paramHomeTimeline.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_HOME);
		homeTimeline.setArguments(paramHomeTimeline);

		ListFragment trendFragment = new HashtagFragment();
		Bundle paramTrend = new Bundle();
		paramTrend.putInt(HashtagFragment.KEY_MODE, HashtagFragment.MODE_POPULAR);
		trendFragment.setArguments(paramTrend);

		ListFragment publicTimeline = new StatusFragment();
		Bundle parampublicTimeline = new Bundle();
		parampublicTimeline.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_PUBLIC);
		publicTimeline.setArguments(parampublicTimeline);

		switch (settings.getLogin().getConfiguration()) {
			case MASTODON:
				fragments.add(homeTimeline);
				fragments.add(trendFragment);
				fragments.add(publicTimeline);
				fragments.add(new NotificationFragment());
				break;
		}
	}
}