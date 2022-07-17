package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

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
	 * show "proxy bypass" dialog
	 */
	public static final int PROXY_CONFIRM = 605;

	/**
	 * show "video error" dialog
	 */
	public static final int VIDEO_ERROR = 606;

	/**
	 * show "delete Tweet?" dialog
	 */
	public static final int TWEET_DELETE = 607;

	/**
	 * show "discard tweet" dialog
	 */
	public static final int TWEET_EDITOR_LEAVE = 608;

	/**
	 * show "Tweet create error" dialog
	 */
	public static final int TWEET_EDITOR_ERROR = 609;

	/**
	 * show "delete directmessage" dialog
	 */
	public static final int MESSAGE_DELETE = 610;

	/**
	 * show "discard directmessage" dialog
	 */
	public static final int MESSAGE_EDITOR_LEAVE = 611;

	/**
	 * show "directmessage upload" error
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


	private TextView title, message, confirmDescr;
	private CompoundButton confirmCheck;
	private Button confirm, cancel;

	@Nullable
	private OnConfirmListener listener;

	/**
	 *
	 */
	public ConfirmDialog(Context context) {
		super(context, R.style.ConfirmDialog);
		setContentView(R.layout.dialog_confirm);
		ViewGroup root = findViewById(R.id.confirm_rootview);
		confirm = findViewById(R.id.confirm_yes);
		cancel = findViewById(R.id.confirm_no);
		title = findViewById(R.id.confirm_title);
		message = findViewById(R.id.confirm_message);
		confirmDescr = findViewById(R.id.confirm_remember_descr);
		confirmCheck = findViewById(R.id.confirm_remember);

		GlobalSettings settings = GlobalSettings.getInstance(context);
		AppStyles.setTheme(root, settings.getBackgroundColor());

		confirm.setOnClickListener(this);
		cancel.setOnClickListener(this);
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
	 * @param type Type of dialog to show
	 * @param messageTxt override default message text
	 */
	public void show(int type, @NonNull String messageTxt) {
		if (isShowing())
			return;

		// attach type to the view
		confirm.setTag(type);

		// default visibility values
		int titleVis = View.GONE;
		int confirmVis = View.INVISIBLE;
		int cancelVis = View.VISIBLE;

		// default resource values
		int titleRes = R.string.info_error;
		int messageRes = R.string.confirm_unknown_error;
		int confirmRes = android.R.string.ok;
		int confirmIconRes = R.drawable.check;
		int cancelRes = android.R.string.cancel;
		int cancelIconRes = R.drawable.cross;

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

			case VIDEO_ERROR:
				titleVis = View.VISIBLE;
				messageRes = R.string.error_cant_load_video;
				confirmIconRes = 0;
				confirmRes = R.string.confirm_open_link;
				cancelVis = View.GONE;
				break;

			case LIST_EDITOR_LEAVE:
			case PROFILE_EDITOR_LEAVE:
				messageRes = R.string.confirm_discard;
				break;

			case TWEET_EDITOR_LEAVE:
				messageRes = R.string.confirm_cancel_tweet;
				break;

			case MESSAGE_EDITOR_LEAVE:
				messageRes = R.string.confirm_cancel_message;
				break;

			case LIST_EDITOR_ERROR:
			case MESSAGE_EDITOR_ERROR:
			case TWEET_EDITOR_ERROR:
			case PROFILE_EDITOR_ERROR:
				titleVis = View.VISIBLE;
				messageRes = R.string.error_connection_failed;
				confirmRes = R.string.confirm_retry_button;
				break;

			case TWEET_DELETE:
				messageRes = R.string.confirm_delete_tweet;
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

			case PROXY_CONFIRM:
				confirmVis = View.VISIBLE;
				titleVis = View.VISIBLE;
				titleRes = R.string.dialog_confirm_warning;
				messageRes = R.string.dialog_warning_videoview;
				break;
		}
		title.setVisibility(titleVis);
		title.setText(titleRes);

		cancel.setVisibility(cancelVis);
		cancel.setText(cancelRes);
		cancel.setCompoundDrawablesWithIntrinsicBounds(cancelIconRes, 0, 0, 0);

		confirmCheck.setVisibility(confirmVis);
		confirmDescr.setVisibility(confirmVis);

		confirm.setText(confirmRes);
		confirm.setCompoundDrawablesWithIntrinsicBounds(confirmIconRes, 0, 0, 0);

		if (messageTxt.isEmpty())
			message.setText(messageRes);
		else
			message.setText(messageTxt);

		super.show();
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.confirm_yes) {
			Object tag = v.getTag();
			if (listener != null && tag instanceof Integer) {
				int type = (int) tag;
				boolean remember = confirmCheck.getVisibility() == View.VISIBLE && confirmCheck.isChecked();
				listener.onConfirm(type, remember);
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
		 *  @param type           type of dialog
		 * @param rememberChoice true if choice should be remembered
		 */
		void onConfirm(int type, boolean rememberChoice);
	}
}