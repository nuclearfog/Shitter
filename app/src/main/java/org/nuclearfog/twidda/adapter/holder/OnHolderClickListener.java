package org.nuclearfog.twidda.adapter.holder;

/**
 * Click listener for adapter view holder
 *
 * @author nuclearfog
 */
public interface OnHolderClickListener {

	int NO_TYPE = -1;

	int LIST_CLICK = 1;

	int PROFILE_CLICK = 2;

	int USER_CLICK = 3;

	int USER_REMOVE = 4;

	int STATUS_CLICK = 5;

	int STATUS_LABEL = 6;

	int LIST_PROFILE = 7;

	int MESSAGE_VIEW = 8;

	int MESSAGE_ANSWER = 9;

	int MESSAGE_PROFILE = 10;

	int MESSAGE_MEDIA = 11;

	int MESSAGE_DELETE = 12;

	int ACCOUNT_SELECT = 13;

	int ACCOUNT_REMOVE = 14;

	int IMAGE_CLICK = 15;

	int IMAGE_SAVE = 16;

	/**
	 * called when an item was clicked
	 *
	 * @param position adapter position of the item
	 * @param type     type of click
	 */
	void onItemClick(int position, int type);

	/**
	 * called when a placeholder item was clicked
	 *
	 * @param position position of the item
	 * @return true to enable loading animation
	 */
	boolean onPlaceholderClick(int position);
}