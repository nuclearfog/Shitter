package org.nuclearfog.twidda.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import androidx.appcompat.app.AlertDialog;

import org.nuclearfog.twidda.R;

/**
 * this dialog is to confirm for user action
 *
 * @author nuclearfog
 */
public class ConfirmDialog extends AlertDialog implements OnClickListener {

    /**
     * types of dialogs, every dialog has its own message and title
     */
    public enum DialogType {
        WRONG_PROXY,
        DEL_DATABASE,
        APP_LOG_OUT,
        REMOVE_ACCOUNT,
        TWEET_DELETE,
        TWEET_EDITOR_LEAVE,
        TWEET_EDITOR_ERROR,
        MESSAGE_DELETE,
        MESSAGE_EDITOR_LEAVE,
        MESSAGE_EDITOR_ERROR,
        PROFILE_EDITOR_LEAVE,
        PROFILE_EDITOR_ERROR,
        PROFILE_UNFOLLOW,
        PROFILE_BLOCK,
        PROFILE_MUTE,
        LIST_REMOVE_USER,
        LIST_UNFOLLOW,
        LIST_DELETE,
        LIST_EDITOR_LEAVE,
        LIST_EDITOR_ERROR
    }

    private DialogType type;
    private OnConfirmListener listener;

    /**
     * @param type     Type of the Dialog {@link DialogType}
     * @param listener listener for the confirmation button
     */
    public ConfirmDialog(Context context, DialogType type, OnConfirmListener listener) {
        super(context, R.style.ConfirmDialog);
        this.type = type;
        this.listener = listener;
        build();
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == BUTTON_POSITIVE) {
            listener.onConfirm(type);
        }
    }

    /**
     * creates an alert dialog
     */
    private void build() {
        Context c = getContext();
        String posButton = c.getString(R.string.dialog_button_yes);
        String negButton = c.getString(R.string.dialog_button_no);
        String message = "";
        String title = "";

        switch (type) {
            case MESSAGE_DELETE:
                message = c.getString(R.string.confirm_delete_message);
                break;

            case WRONG_PROXY:
                title = c.getString(R.string.info_error);
                message = c.getString(R.string.error_wrong_connection_settings);
                posButton = c.getString(R.string.dialog_button_cancel);
                negButton = c.getString(R.string.confirm_back);
                break;

            case DEL_DATABASE:
                message = c.getString(R.string.confirm_delete_database);
                break;

            case APP_LOG_OUT:
                message = c.getString(R.string.confirm_log_lout);
                break;

            case LIST_EDITOR_LEAVE:
            case PROFILE_EDITOR_LEAVE:
                message = c.getString(R.string.confirm_discard);
                break;

            case TWEET_EDITOR_LEAVE:
                message = c.getString(R.string.confirm_cancel_tweet);
                break;

            case LIST_EDITOR_ERROR:
            case MESSAGE_EDITOR_ERROR:
            case TWEET_EDITOR_ERROR:
            case PROFILE_EDITOR_ERROR:
                title = c.getString(R.string.info_error);
                posButton = c.getString(R.string.confirm_retry_button);
                negButton = c.getString(R.string.dialog_button_cancel);
                break;

            case MESSAGE_EDITOR_LEAVE:
                message = c.getString(R.string.confirm_cancel_message);
                break;

            case TWEET_DELETE:
                message = c.getString(R.string.confirm_delete_tweet);
                break;

            case PROFILE_UNFOLLOW:
                message = c.getString(R.string.confirm_unfollow);
                break;

            case PROFILE_BLOCK:
                message = c.getString(R.string.confirm_block);
                break;

            case PROFILE_MUTE:
                message = c.getString(R.string.confirm_mute);
                break;

            case LIST_REMOVE_USER:
                message = c.getString(R.string.confirm_remove_user_from_list);
                posButton = c.getString(R.string.dialog_button_ok);
                negButton = c.getString(R.string.dialog_button_cancel);
                break;

            case LIST_UNFOLLOW:
                message = c.getString(R.string.confirm_unfollow_list);
                break;

            case LIST_DELETE:
                message = c.getString(R.string.confirm_delete_list);
                break;

            case REMOVE_ACCOUNT:
                message = c.getString(R.string.confirm_remove_account);
                posButton = c.getString(R.string.dialog_button_ok);
                negButton = c.getString(R.string.dialog_button_cancel);
                break;
        }
        setTitle(title);
        setMessage(message);
        setButton(BUTTON_NEGATIVE, negButton, this);
        setButton(BUTTON_POSITIVE, posButton, this);
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
        void onConfirm(DialogType type);
    }
}