package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.lists.Fields;
import org.nuclearfog.twidda.ui.fragments.FieldFragment;
import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;

/**
 * ViewPager adapter for {@link org.nuclearfog.twidda.ui.activities.ProfileActivity}
 *
 * @author nuclearfog
 */
public class ProfileAdapter extends ViewPagerAdapter {

	/**
	 * @param userId ID of the user (profile ID)
	 */
	public ProfileAdapter(FragmentActivity fragmentActivity, long userId) {
		super(fragmentActivity);
		// user timeline
		Bundle paramUser = new Bundle();
		paramUser.putLong(StatusFragment.KEY_ID, userId);
		paramUser.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_USER);
		ListFragment statusFragment = new StatusFragment();
		statusFragment.setArguments(paramUser);
		// user favorits
		Bundle paramFavorite = new Bundle();
		paramFavorite.putLong(StatusFragment.KEY_ID, userId);
		paramFavorite.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_FAVORIT);
		ListFragment favoriteFragment = new StatusFragment();
		favoriteFragment.setArguments(paramFavorite);
		// user bookmarks
		Bundle paramBookmark = new Bundle();
		paramBookmark.putLong(StatusFragment.KEY_ID, userId);
		paramBookmark.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_BOOKMARK);
		ListFragment bookmarkFragment = new StatusFragment();
		bookmarkFragment.setArguments(paramBookmark);
		// user fields
		ListFragment fieldFragment = new FieldFragment();

		fragments.clear();
		fragments.add(statusFragment);
		switch (settings.getLogin().getConfiguration()) {
			case MASTODON:
				if (settings.getLogin().getId() == userId) {
					fragments.add(favoriteFragment);
					fragments.add(bookmarkFragment);
				}
				fragments.add(fieldFragment);
				break;
		}
	}

	/**
	 * put user fields into FieldFragment
	 *
	 * @param fields user fields
	 */
	public void setFields(User.Field[] fields) {
		for (ListFragment fragment : fragments) {
			if (fragment instanceof FieldFragment) {
				((FieldFragment) fragment).setItems(new Fields(fields));
			}
		}
	}
}