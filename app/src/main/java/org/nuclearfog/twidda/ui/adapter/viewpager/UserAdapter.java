package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.ui.fragments.DomainFragment;
import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.UserFragment;

/**
 * ViewPager adapter used by {@link org.nuclearfog.twidda.ui.activities.UsersActivity}
 *
 * @author nuclearfog
 */
public class UserAdapter extends ViewPagerAdapter {

	/**
	 * used to show following pages
	 */
	public static final int FOLLOWING = 10;

	/**
	 * used to show follower pages
	 */
	public static final int FOLLOWER = 11;

	/**
	 * used to show reposter of a status
	 */
	public static final int REPOSTER = 12;

	/**
	 * used to show users favoriting a status
	 */
	public static final int FAVORITER = 13;

	/**
	 * used to show block/mute pages
	 */
	public static final int BLOCKS = 14;

	private long id;
	private int type;

	/**
	 *
	 */
	public UserAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);
	}


	@NonNull
	@Override
	public Fragment createFragment(int position) {
		ListFragment fragment;
		switch (position) {
			default:
			case 0:
				Bundle param = new Bundle();
				param.putLong(UserFragment.KEY_ID, id);
				if (type == FOLLOWING) {
					param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOWING);
				} else if (type == FOLLOWER) {
					param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOWER);
				} else if (type == REPOSTER) {
					param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_REPOSTER);
				} else if (type == FAVORITER) {
					param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FAVORITER);
				} else if (type == BLOCKS) {
					param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_MUTES);
				}
				fragment = new UserFragment();
				fragment.setArguments(param);
				break;

			case 1:
				param = new Bundle();
				if (type == FOLLOWING) {
					param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOW_INCOMING);
				} else if (type == FOLLOWER) {
					param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOW_OUTGOING);
				} else if (type == BLOCKS) {
					param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_BLOCKS);
				}
				fragment = new UserFragment();
				fragment.setArguments(param);
				break;

			case 2:
				fragment = new DomainFragment();
				break;
		}
		return fragment;
	}

	/**
	 * set content type to view
	 *
	 * @param type what type of fragments should be loaded {@link #FOLLOWER,#FOLLOWING,#REQUESTS,#REPOSTER,#FAVORITER,#BLOCKS}
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 *
	 */
	public void setId(long id) {
		this.id = id;
	}
}