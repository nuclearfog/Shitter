package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.MessageFragment;
import org.nuclearfog.twidda.ui.fragments.NotificationFragment;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;
import org.nuclearfog.twidda.ui.fragments.TrendFragment;

/**
 * ViewPager fragment adapter for {@link org.nuclearfog.twidda.ui.activities.MainActivity}
 *
 * @author nuclearfog
 */
public class HomeAdapter extends ViewPagerAdapter {

	/**
	 */
	public HomeAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);

		ListFragment homeTimeline = new StatusFragment();
		Bundle paramHomeTimeline = new Bundle();
		paramHomeTimeline.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_HOME);
		homeTimeline.setArguments(paramHomeTimeline);

		ListFragment trendFragment = new TrendFragment();
		Bundle paramTrend = new Bundle();
		paramTrend.putInt(TrendFragment.KEY_MODE, TrendFragment.MODE_POPULAR);
		trendFragment.setArguments(paramTrend);

		ListFragment publicTimeline = new StatusFragment();
		Bundle parampublicTimeline = new Bundle();
		parampublicTimeline.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_PUBLIC);
		publicTimeline.setArguments(parampublicTimeline);

		switch (settings.getLogin().getConfiguration()) {
			case TWITTER1:
			case TWITTER2:
				fragments.add(homeTimeline);
				fragments.add(trendFragment);
				fragments.add(new NotificationFragment());
				fragments.add(new MessageFragment());
				break;

			case MASTODON:
				fragments.add(homeTimeline);
				fragments.add(trendFragment);
				fragments.add(publicTimeline);
				fragments.add(new NotificationFragment());
				break;
		}
	}
}