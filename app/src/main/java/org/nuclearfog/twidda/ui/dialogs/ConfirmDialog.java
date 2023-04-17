package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;

/**
 * Custom alert dialog class to show error and warning messages to user
 * and to ask to confirm actions
 *
 * @author nuclearfog
 */
public class ConfirmDialog extends Dialog implements OnClickListener {

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
	 * show dialog to delete message
	 */
	public static final int MESSAGE_DELETE = 610;

	/**
	 * show dialog to discard message
	 */
	public static final int MESSAGE_EDITOR_LEAVE = 611;

	/**
	 * show dialog if an error occurs while uploading a message
	 */
	public static final int MESSAGE_EDITOR_ERROR = 612;

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


	private TextView title, message;
	private Button confirm, cancel;
	private ViewGroup root;

	@Nullable
	private OnConfirmListener listener;

	/**
	 *
	 */
	public ConfirmDialog(Context context) {
		super(context, R.style.ConfirmDialog);
		setContentView(R.layout.dialog_confirm);
		root = findViewById(R.id.confirm_rootview);
		confirm = findViewById(R.id.confirm_yes);
		cancel = findViewById(R.id.confirm_no);
		title = findViewById(R.id.confirm_title);
		message = findViewById(R.id.confirm_message);

		confirm.setOnClickListener(this);
		cancel.setOnClickListener(this);
	}


	@Override
	public void show() {
	}

	/**
	 * creates an alert dialog
	 *
	 * @param type Type of dialog to show
	 */
	public void show(int type) {
		show(type, "");
	}

	/**
	 * creates an alert dialog
	 *
	 * @param type       Type of dialog to show
	 * @param messageTxt override default message text
	 */
	public void show(int type, @NonNull String messageTxt) {
		if (isShowing()) {
			return;
		}
		// attach type to the view
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
			case MESSAGE_DELETE:
				messageRes = R.string.confirm_delete_message;
				break;

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

			case MESSAGE_EDITOR_LEAVE:
				messageRes = R.string.confirm_cancel_message;
				break;

			case LIST_EDITOR_ERROR:
			case MESSAGE_EDITOR_ERROR:
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
		if (messageTxt.isEmpty()) {
			message.setText(messageRes);
		} else {
			message.setText(messageTxt);
		}
		AppStyles.setTheme(root);
		super.show();
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.confirm_yes) {
			Object tag = v.getTag();
			if (listener != null && tag instanceof Integer) {
				int type = (int) tag;
				listener.onConfirm(type);
			}
			dismiss();
		} else if (v.getId() == R.id.confirm_no) {
			dismiss();
		}
	}

	/**
	 * add confirm listener
	 */
	public void setConfirmListener(OnConfirmListener listener) {
		this.listener = listener;
	}

	/**
	 * Alert dialog listener
	 */
	public interface OnConfirmListener {

		/**
		 * called when the positive button was clicked
		 *
		 * @param type type of dialog
		 */
		void onConfirm(int type);
	}
}