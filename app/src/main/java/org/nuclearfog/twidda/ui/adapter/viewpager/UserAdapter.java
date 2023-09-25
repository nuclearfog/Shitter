package org.nuclearfog.twidda.ui.adapter.viewpager;

import android.os.Bundle;

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

	/**
	 * @param id   Status ID, List ID or User ID, depending on mode
	 * @param mode what type of fragments should be loaded {@link #FOLLOWER,#FOLLOWING,#REQUESTS,#REPOSTER,#FAVORITER,#BLOCKS}
	 */
	public UserAdapter(FragmentActivity fragmentActivity, long id, int mode) {
		super(fragmentActivity);
		switch (mode) {
			case FOLLOWING:
				Bundle paramFollowing = new Bundle();
				paramFollowing.putLong(UserFragment.KEY_ID, id);
				paramFollowing.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOWING);
				ListFragment following = new UserFragment();
				following.setArguments(paramFollowing);
				fragments.add(following);
				if (settings.getLogin().getId() == id) {
					Bundle paramFollowingRequest = new Bundle();
					paramFollowingRequest.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOW_INCOMING);
					ListFragment pendingRequests = new UserFragment();
					pendingRequests.setArguments(paramFollowingRequest);
					fragments.add(pendingRequests);
				}
				break;

			case FOLLOWER:
				Bundle paramFollower = new Bundle();
				paramFollower.putLong(UserFragment.KEY_ID, id);
				paramFollower.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOWER);
				ListFragment followerList = new UserFragment();
				followerList.setArguments(paramFollower);
				fragments.add(followerList);

				if (settings.getLogin().getConfiguration().isOutgoingFollowRequestSupported() && settings.getLogin().getId() == id) {
					Bundle paramFollowRequest = new Bundle();
					paramFollowRequest.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOW_OUTGOING);
					ListFragment followRequest = new UserFragment();
					followRequest.setArguments(paramFollowRequest);
					fragments.add(followRequest);
				}
				break;

			case REPOSTER:
				Bundle paramReposter = new Bundle();
				paramReposter.putLong(UserFragment.KEY_ID, id);
				paramReposter.putInt(UserFragment.KEY_MODE, UserFragment.MODE_REPOSTER);
				ListFragment reposter = new UserFragment();
				reposter.setArguments(paramReposter);
				fragments.add(reposter);
				break;

			case FAVORITER:
				Bundle paramFavoriter = new Bundle();
				paramFavoriter.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FAVORITER);
				paramFavoriter.putLong(UserFragment.KEY_ID, id);
				ListFragment favoriter = new UserFragment();
				favoriter.setArguments(paramFavoriter);
				fragments.add(favoriter);
				break;

			case BLOCKS:
				Bundle paramMuteList = new Bundle();
				paramMuteList.putInt(UserFragment.KEY_MODE, UserFragment.MODE_MUTES);
				ListFragment muteList = new UserFragment();
				muteList.setArguments(paramMuteList);
				fragments.add(muteList);

				Bundle paramBlockList = new Bundle();
				paramBlockList.putInt(UserFragment.KEY_MODE, UserFragment.MODE_BLOCKS);
				ListFragment blockList = new UserFragment();
				blockList.setArguments(paramBlockList);
				fragments.add(blockList);

				ListFragment domainFragment = new DomainFragment();
				fragments.add(domainFragment);
				break;
		}
	}
}