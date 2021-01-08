package org.nuclearfog.twidda.backend.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import androidx.appcompat.app.AlertDialog;

import org.nuclearfog.twidda.R;

/**
 * this class creates alert dialogs with a custom listener
 */
public final class DialogBuilder {

    /**
     * types of dialogs, every dialog has its own message and title
     */
    public enum DialogType {
        DEL_MESSAGE,
        WRONG_PROXY,
        DEL_DATABASE,
        LOGOUT_APP,
        LISTPOPUP_LEAVE,
        TWEETPOPUP_LEAVE,
        TWEETPOPUP_ERROR,
        MSG_POPUP_LEAVE,
        PROFILE_EDIT_LEAVE,
        DELETE_TWEET,
        PROFILE_UNFOLLOW,
        PROFILE_BLOCK,
        PROFILE_MUTE,
        DEL_USER_LIST,
        LIST_UNFOLLOW,
        LIST_DELETE
    }

    private DialogBuilder() {
    }

    /**
     * creates an alert dialog
     *
     * @param context  activity context
     * @param type     type of error dialog
     * @param listener listener for positive button
     * @return dialog instance
     */
    public static Dialog create(Context context, final DialogType type, final OnDialogClick listener) {
        int posButton = R.string.dialog_button_yes;
        int negButton = R.string.dialog_button_no;
        int title = 0;
        int message = 0;

        switch (type) {
            case DEL_MESSAGE:
                message = R.string.confirm_delete_message;
                break;

            case WRONG_PROXY:
                title = R.string.info_error;
                message = R.string.error_wrong_connection_settings;
                posButton = R.string.dialog_button_cancel;
                negButton = R.string.confirm_back;
                break;

            case DEL_DATABASE:
                message = R.string.confirm_delete_database;
                break;

            case LOGOUT_APP:
                message = R.string.confirm_log_lout;
                break;

            case LISTPOPUP_LEAVE:
            case PROFILE_EDIT_LEAVE:
                message = R.string.confirm_discard;
                break;

            case TWEETPOPUP_LEAVE:
                message = R.string.confirm_cancel_tweet;
                break;

            case TWEETPOPUP_ERROR:
                title = R.string.info_error;
                message = R.string.error_sending_tweet;
                posButton = R.string.confirm_retry_button;
                negButton = R.string.dialog_button_cancel;
                break;

            case MSG_POPUP_LEAVE:
                message = R.string.confirm_cancel_message;
                break;

            case DELETE_TWEET:
                message = R.string.confirm_delete_tweet;
                break;

            case PROFILE_UNFOLLOW:
                message = R.string.confirm_unfollow;
                break;

            case PROFILE_BLOCK:
                message = R.string.confirm_block;
                break;

            case PROFILE_MUTE:
                message = R.string.confirm_mute;
                break;

            case DEL_USER_LIST:
                message = R.string.confirm_remove_user_from_list;
                posButton = R.string.dialog_button_ok;
                negButton = R.string.dialog_button_cancel;
                break;

            case LIST_UNFOLLOW:
                message = R.string.confirm_unfollow_list;
                break;

            case LIST_DELETE:
                message = R.string.confirm_delete_list;
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.ConfirmDialog);
        if (title != 0)
            builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(negButton, null);
        builder.setPositiveButton(posButton, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onConfirm(type);
            }
        });
        return builder.create();
    }

    /**
     * listener for dialog
     */
    public interface OnDialogClick {

        /**
         * called when the positive button was clicked
         *
         * @param type type of dialog
         */
        void onConfirm(DialogType type);
    }
}