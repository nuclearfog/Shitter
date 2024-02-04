package org.nuclearfog.twidda.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * Custom alert dialog class to show error and warning messages to user
 * and to ask to confirm actions
 *
 * @author nuclearfog
 */
public class ConfirmDialog extends DialogFragment implements OnClickListener {

	/**
	 * setup a proxy error dialog
	 */
	public static final int WRONG_PROXY = 601;

	/**
	 * show "delete app data" dialog
	 */
	public static final int DELETE_APP_DATA = 602;

	/**
	 * show "log out" dialog
	 */
	public static final int APP_LOG_OUT = 603;

	/**
	 * show "remove account" dialog
	 */
	public static final int REMOVE_ACCOUNT = 604;

	/**
	 * show dialog to delete status
	 */
	public static final int DELETE_STATUS = 607;

	/**
	 * show dialog to discard edited status
	 */
	public static final int STATUS_EDITOR_LEAVE = 608;

	/**
	 * show dialog if an error occurs while editin status
	 */
	public static final int STATUS_EDITOR_ERROR = 609;

	/**
	 * show "discard profile changes" dialog
	 */
	public static final int PROFILE_EDITOR_LEAVE = 613;

	/**
	 * show "error profile update" dialog
	 */
	public static final int PROFILE_EDITOR_ERROR = 614;

	/**
	 * show "unfollow user" dialog
	 */
	public static final int PROFILE_UNFOLLOW = 615;

	/**
	 * show "block user" dialog
	 */
	public static final int PROFILE_BLOCK = 616;

	/**
	 * show "mute user" dialog
	 */
	public static final int PROFILE_MUTE = 617;

	/**
	 * show "remove user from list" dialog
	 */
	public static final int LIST_REMOVE_USER = 618;

	/**
	 * show "unfollow userlist" dialog
	 */
	public static final int LIST_UNFOLLOW = 619;

	/**
	 * show "delete userlist" dialog
	 */
	public static final int LIST_DELETE = 620;

	/**
	 * show "discard changes" dialog
	 */
	public static final int LIST_EDITOR_LEAVE = 621;

	/**
	 * show "update userlist error" dialog
	 */
	public static final int LIST_EDITOR_ERROR = 622;

	/**
	 * show "dismiss notification" dialog
	 */
	public static final int NOTIFICATION_DISMISS = 623;

	/**
	 * show notification when adding domain hostname to blocklist
	 */
	public static final int DOMAIN_BLOCK_ADD = 624;

	/**
	 * show notification when removing domain hostname to blocklist
	 */
	public static final int DOMAIN_BLOCK_REMOVE = 625;

	/**
	 * show notification when removing a filter from filterlist
	 */
	public static final int FILTER_REMOVE = 626;

	/**
	 * show 'unfollow tag' dialog
	 */
	public static final int UNFOLLOW_TAG = 628;

	/**
	 * show 'unfeature tag' dialog
	 */
	public static final int UNFEATURE_TAG = 629;

	/**
	 * show notification when removing a scheduled status
	 */
	public static final int SCHEDULE_REMOVE = 630;

	/**
	 * show 'accept follow request' dialog
	 */
	public static final int FOLLOW_REQUEST = 631;

	/**
	 * show 'dismiss announcement' dialog
	 */
	public static final int ANNOUNCEMENT_DISMISS = 632;

	/**
	 * bundle key used to set/restore dialog type
	 * value type is integer
	 */
	private static final String KEY_TYPE = "dialog-type";

	/**
	 * bundle key used to set/restore dialog message
	 * value type is String
	 */
	private static final String KEY_MESSAGE = "dialog-message";

	private TextView title, message;
	private Button confirm, cancel;
	private CompoundButton remember;

	private int type = 0;
	private String messageStr = "";

	/**
	 *
	 */
	public ConfirmDialog() {
		setStyle(STYLE_NO_TITLE, R.style.ConfirmDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_confirm, container, false);
		confirm = view.findViewById(R.id.confirm_yes);
		cancel = view.findViewById(R.id.confirm_no);
		title = view.findViewById(R.id.confirm_title);
		message = view.findViewById(R.id.confirm_message);
		remember = view.findViewById(R.id.confirm_remember);
		GlobalSettings settings = GlobalSettings.get(requireContext());

		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			type = savedInstanceState.getInt(KEY_TYPE);
			messageStr = savedInstanceState.getString(KEY_MESSAGE, "");
			setText();
		}

		AppStyles.setTheme((ViewGroup) view, settings.getPopupColor());

		confirm.setOnClickListener(this);
		cancel.setOnClickListener(this);
		return view;
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putInt(KEY_TYPE, type);
		outState.putString(KEY_MESSAGE, messageStr);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.confirm_yes) {
			Object tag = v.getTag();
			if (tag instanceof Integer) {
				int type = (int) tag;
				// get parent activity or fragment inplementing OnConfirmListener and return result
				if (getParentFragment() instanceof OnConfirmListener) {
					((OnConfirmListener) getParentFragment()).onConfirm(type, remember.isChecked());
				} else if (getActivity() instanceof OnConfirmListener) {
					((OnConfirmListener) getActivity()).onConfirm(type, remember.isChecked());
				}
			}
			dismiss();
		} else if (v.getId() == R.id.confirm_no) {
			dismiss();
		}
	}

	/**
	 * show dialog
	 *
	 * @param fragment fragment from which to show the dialog
	 * @param type     type of dialog
	 * @param message  additional message
	 * @return true if dialog was created successfully, false if a dialog already exists
	 */
	public static boolean show(Fragment fragment, int type, @Nullable String message) {
		if (fragment.isAdded())
			return show(fragment.getChildFragmentManager(), type, message);
		return false;
	}

	/**
	 * show dialog
	 *
	 * @param activity activity from which to show the dialog
	 * @param type     type of dialog
	 * @param message  additional message
	 * @return true if dialog was created successfully, false if a dialog already exists
	 */
	public static boolean show(FragmentActivity activity, int type, @Nullable String message) {
		return show(activity.getSupportFragmentManager(), type, message);
	}

	/**
	 *
	 */
	private static boolean show(FragmentManager fm, int type, @Nullable String message) {
		String tag = type + ":" + message;
		Fragment dialogFragment = fm.findFragmentByTag(tag);
		if (dialogFragment == null) {
			ConfirmDialog dialog = new ConfirmDialog();
			Bundle args = new Bundle();
			args.putInt(KEY_TYPE, type);
			if (message != null)
				args.putString(KEY_MESSAGE, message);
			dialog.setArguments(args);
			dialog.show(fm, tag);
			return true;
		}
		return false;
	}

	/**
	 *
	 */
	private void setText() {
		confirm.setTag(type);
		// default visibility values
		int titleVis = View.GONE;
		int cancelVis = View.VISIBLE;
		// default resource values
		int titleRes = R.string.info_error;
		int messageRes = R.string.confirm_unknown_error;
		int confirmRes = android.R.string.ok;
		int confirmIconRes = R.drawable.check;
		int cancelRes = android.R.string.cancel;
		int cancelIconRes = R.drawable.cross;
		// override values depending on type
		switch (type) {
			case WRONG_PROXY:
				titleVis = View.VISIBLE;
				messageRes = R.string.error_wrong_connection_settings;
				break;

			case DELETE_APP_DATA:
				messageRes = R.string.confirm_delete_database;
				break;

			case APP_LOG_OUT:
				messageRes = R.string.confirm_log_lout;
				break;

			case LIST_EDITOR_LEAVE:
			case PROFILE_EDITOR_LEAVE:
				messageRes = R.string.confirm_discard;
				break;

			case STATUS_EDITOR_LEAVE:
				messageRes = R.string.confirm_cancel_status;
				break;

			case LIST_EDITOR_ERROR:
			case STATUS_EDITOR_ERROR:
			case PROFILE_EDITOR_ERROR:
				titleVis = View.VISIBLE;
				messageRes = R.string.error_connection_failed;
				confirmRes = R.string.confirm_retry_button;
				break;

			case DELETE_STATUS:
				messageRes = R.string.confirm_delete_status;
				break;

			case NOTIFICATION_DISMISS:
				messageRes = R.string.confirm_dismiss_notification;
				break;

			case DOMAIN_BLOCK_ADD:
				messageRes = R.string.confirm_add_domain_block;
				break;

			case DOMAIN_BLOCK_REMOVE:
				messageRes = R.string.confirm_remove_domain_block;
				break;

			case PROFILE_UNFOLLOW:
				messageRes = R.string.confirm_unfollow;
				break;

			case PROFILE_BLOCK:
				messageRes = R.string.confirm_block;
				break;

			case PROFILE_MUTE:
				messageRes = R.string.confirm_mute;
				break;

			case LIST_REMOVE_USER:
				messageRes = R.string.confirm_remove_user_from_list;
				break;

			case LIST_UNFOLLOW:
				messageRes = R.string.confirm_unfollow_list;
				break;

			case LIST_DELETE:
				messageRes = R.string.confirm_delete_list;
				break;

			case REMOVE_ACCOUNT:
				messageRes = R.string.confirm_remove_account;
				break;

			case FILTER_REMOVE:
				messageRes = R.string.confirm_remove_filter;
				break;

			case UNFOLLOW_TAG:
				messageRes = R.string.confirm_tag_unfollow;
				break;

			case UNFEATURE_TAG:
				messageRes = R.string.confirm_tag_unfeature;
				break;

			case SCHEDULE_REMOVE:
				messageRes = R.string.confirm_schedule_remove;
				break;

			case FOLLOW_REQUEST:
				messageRes = R.string.confirm_accept_follow_request;
				break;

			case ANNOUNCEMENT_DISMISS:
				messageRes = R.string.confirm_dismiss_announcement;
				break;
		}
		// setup title
		title.setVisibility(titleVis);
		title.setText(titleRes);
		// setup cancel button
		cancel.setVisibility(cancelVis);
		cancel.setText(cancelRes);
		cancel.setCompoundDrawablesWithIntrinsicBounds(cancelIconRes, 0, 0, 0);
		// setup confirm button
		confirm.setText(confirmRes);
		confirm.setCompoundDrawablesWithIntrinsicBounds(confirmIconRes, 0, 0, 0);
		// setup message
		if (messageStr != null && !messageStr.isEmpty()) {
			message.setText(messageStr);
		} else {
			message.setText(messageRes);
		}
	}

	/**
	 * Alert dialog listener
	 */
	public interface OnConfirmListener {

		/**
		 * called when the positive button was clicked
		 *
		 * @param type     type of dialog
		 * @param remember true if "remember choice" is checked
		 */
		void onConfirm(int type, boolean remember);
	}
}