package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;
import org.nuclearfog.twidda.ui.fragments.UserFragment;
import org.nuclearfog.twidda.ui.fragments.UserListFragment;

/**
 * Viewpager adapter for {@link org.nuclearfog.twidda.ui.activities.UserlistActivity}
 *
 * @author nuclearfog
 */
public class UserlistAdapter extends ViewPagerAdapter {

	/**
	 * @param userlist userlist to show content
	 */
	public UserlistAdapter(FragmentActivity fragmentActivity, UserList userlist) {
		super(fragmentActivity);
		ListFragment userlistTimeline = new StatusFragment();
		Bundle paramUserlistTl = new Bundle();
		paramUserlistTl.putLong(StatusFragment.KEY_ID, userlist.getId());
		paramUserlistTl.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_USERLIST);
		userlistTimeline.setArguments(paramUserlistTl);
		fragments.add(userlistTimeline);

		ListFragment memberList = new UserFragment();
		Bundle paramUserlistMember = new Bundle();
		paramUserlistMember.putInt(UserFragment.KEY_MODE, UserFragment.MODE_LIST_MEMBER);
		paramUserlistMember.putBoolean(UserFragment.KEY_DELETE, userlist.isEdiatable());
		paramUserlistMember.putLong(UserFragment.KEY_ID, userlist.getId());
		memberList.setArguments(paramUserlistMember);
		fragments.add(memberList);

		if (settings.getLogin().getConfiguration().isUserlistSubscriberSupported()) {
			ListFragment subscriberList = new UserListFragment();
			Bundle paramUserlistSubscriber = new Bundle();
			paramUserlistSubscriber.putLong(UserFragment.KEY_ID, userlist.getId());
			paramUserlistSubscriber.putInt(UserFragment.KEY_MODE, UserFragment.MODE_LIST_SUBSCRIBER);
			subscriberList.setArguments(paramUserlistSubscriber);
			fragments.add(subscriberList);
		}
	}
}