package org.nuclearfog.twidda.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * this dialog is to confirm for user action
 *
 * @author nuclearfog
 */
public class ConfirmDialog extends Dialog implements OnClickListener {

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

    private TextView txtTitle, txtMessage;
    private Button confirm, cancel;

    private DialogType type;
    private OnConfirmListener listener;

    /**
     * @param type     Type of the Dialog {@link DialogType}
     * @param listener listener for the confirmation button
     */
    public ConfirmDialog(Context context, DialogType type, OnConfirmListener listener) {
        super(context, R.style.ConfirmDialog);
        setContentView(R.layout.dialog_confirm);
        View root = findViewById(R.id.confirm_rootview);
        confirm = findViewById(R.id.confirm_yes);
        cancel = findViewById(R.id.confirm_no);
        txtTitle = findViewById(R.id.confirm_title);
        txtMessage = findViewById(R.id.confirm_message);

        confirm.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check, 0, 0, 0);
        cancel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cross, 0, 0, 0);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        AppStyles.setTheme(settings, root);

        confirm.setOnClickListener(this);
        cancel.setOnClickListener(this);

        this.type = type;
        this.listener = listener;
        build();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.confirm_yes) {
            listener.onConfirm(type);
            dismiss();
        } else if (v.getId() == R.id.confirm_no) {
            dismiss();
        }
    }

    /**
     * set message text
     *
     * @param message message text
     */
    public void setMessage(String message) {
        txtMessage.setText(message);
    }

    /**
     * creates an alert dialog
     */
    private void build() {
        switch (type) {
            case MESSAGE_DELETE:
                txtTitle.setVisibility(View.GONE);
                txtMessage.setText(R.string.confirm_delete_message);
                break;

            case WRONG_PROXY:
                txtTitle.setText(R.string.info_error);
                txtMessage.setText(R.string.error_wrong_connection_settings);
                confirm.setText(R.string.dialog_button_cancel);
                cancel.setText(R.string.confirm_back);
                break;

            case DEL_DATABASE:
                txtTitle.setVisibility(View.GONE);
                txtMessage.setText(R.string.confirm_delete_database);
                break;

            case APP_LOG_OUT:
                txtTitle.setVisibility(View.GONE);
                txtMessage.setText(R.string.confirm_log_lout);
                break;

            case LIST_EDITOR_LEAVE:
            case PROFILE_EDITOR_LEAVE:
                txtTitle.setVisibility(View.GONE);
                txtMessage.setText(R.string.confirm_discard);
                break;

            case TWEET_EDITOR_LEAVE:
                txtTitle.setVisibility(View.GONE);
                txtMessage.setText(R.string.confirm_cancel_tweet);
                break;

            case LIST_EDITOR_ERROR:
            case MESSAGE_EDITOR_ERROR:
            case TWEET_EDITOR_ERROR:
            case PROFILE_EDITOR_ERROR:
                txtTitle.setText(R.string.info_error);
                confirm.setText(R.string.confirm_retry_button);
                cancel.setText(R.string.dialog_button_cancel);
                break;

            case MESSAGE_EDITOR_LEAVE:
                txtTitle.setVisibility(View.GONE);
                txtMessage.setText(R.string.confirm_cancel_message);
                break;

            case TWEET_DELETE:
                txtTitle.setVisibility(View.GONE);
                txtMessage.setText(R.string.confirm_delete_tweet);
                break;

            case PROFILE_UNFOLLOW:
                txtTitle.setVisibility(View.GONE);
                txtMessage.setText(R.string.confirm_unfollow);
                break;

            case PROFILE_BLOCK:
                txtTitle.setVisibility(View.GONE);
                txtMessage.setText(R.string.confirm_block);
                break;

            case PROFILE_MUTE:
                txtTitle.setVisibility(View.GONE);
                txtMessage.setText(R.string.confirm_mute);
                break;

            case LIST_REMOVE_USER:
                txtTitle.setVisibility(View.GONE);
                txtMessage.setText(R.string.confirm_remove_user_from_list);
                confirm.setText(R.string.dialog_button_ok);
                cancel.setText(R.string.dialog_button_cancel);
                break;

            case LIST_UNFOLLOW:
                txtMessage.setText(R.string.confirm_unfollow_list);
                txtTitle.setVisibility(View.GONE);
                break;

            case LIST_DELETE:
                txtMessage.setText(R.string.confirm_delete_list);
                txtTitle.setVisibility(View.GONE);
                break;

            case REMOVE_ACCOUNT:
                txtMessage.setText(R.string.confirm_remove_account);
                confirm.setText(R.string.dialog_button_ok);
                cancel.setText(R.string.dialog_button_cancel);
                txtTitle.setVisibility(View.GONE);
                break;
        }
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