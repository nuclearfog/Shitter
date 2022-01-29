package org.nuclearfog.twidda.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

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
        DELETE_APP_DATA,
        APP_LOG_OUT,
        REMOVE_ACCOUNT,
        VIDEO_ERROR,
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
        txtTitle = findViewById(R.id.confirm_title);
        txtMessage = findViewById(R.id.confirm_message);

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
    public void show(DialogType type) {
        if (isShowing())
            return;

        // attach type to the view
        confirm.setTag(type);

        // set default values
        confirm.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check, 0, 0, 0);
        cancel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cross, 0, 0, 0);
        confirm.setText(android.R.string.ok);
        cancel.setText(android.R.string.cancel);
        cancel.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.GONE);

        switch (type) {
            case MESSAGE_DELETE:
                txtMessage.setText(R.string.confirm_delete_message);
                break;

            case WRONG_PROXY:
                txtTitle.setVisibility(View.VISIBLE);
                txtTitle.setText(R.string.info_error);
                txtMessage.setText(R.string.error_wrong_connection_settings);
                break;

            case DELETE_APP_DATA:
                txtMessage.setText(R.string.confirm_delete_database);
                break;

            case APP_LOG_OUT:
                txtMessage.setText(R.string.confirm_log_lout);
                break;

            case VIDEO_ERROR:
                txtTitle.setVisibility(View.VISIBLE);
                txtTitle.setText(R.string.info_error);
                txtMessage.setText(R.string.error_cant_load_video);
                confirm.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                confirm.setText(R.string.confirm_open_link);
                cancel.setVisibility(View.GONE);
                break;

            case LIST_EDITOR_LEAVE:
            case PROFILE_EDITOR_LEAVE:
                txtMessage.setText(R.string.confirm_discard);
                break;

            case TWEET_EDITOR_LEAVE:
                txtMessage.setText(R.string.confirm_cancel_tweet);
                break;

            case MESSAGE_EDITOR_LEAVE:
                txtMessage.setText(R.string.confirm_cancel_message);
                break;

            case LIST_EDITOR_ERROR:
            case MESSAGE_EDITOR_ERROR:
            case TWEET_EDITOR_ERROR:
            case PROFILE_EDITOR_ERROR:
                txtTitle.setVisibility(View.VISIBLE);
                txtTitle.setText(R.string.info_error);
                txtMessage.setText(R.string.error_connection_failed);
                confirm.setText(R.string.confirm_retry_button);
                break;

            case TWEET_DELETE:
                txtMessage.setText(R.string.confirm_delete_tweet);
                break;

            case PROFILE_UNFOLLOW:
                txtMessage.setText(R.string.confirm_unfollow);
                break;

            case PROFILE_BLOCK:
                txtMessage.setText(R.string.confirm_block);
                break;

            case PROFILE_MUTE:
                txtMessage.setText(R.string.confirm_mute);
                break;

            case LIST_REMOVE_USER:
                txtMessage.setText(R.string.confirm_remove_user_from_list);
                break;

            case LIST_UNFOLLOW:
                txtMessage.setText(R.string.confirm_unfollow_list);
                break;

            case LIST_DELETE:
                txtMessage.setText(R.string.confirm_delete_list);
                break;

            case REMOVE_ACCOUNT:
                txtMessage.setText(R.string.confirm_remove_account);
                break;
        }
        super.show();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.confirm_yes) {
            Object tag = v.getTag();
            if (listener != null && tag instanceof DialogType) {
                DialogType type = (DialogType) tag;
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
     * set message text
     *
     * @param message message text
     */
    public void setMessage(String message) {
        txtMessage.setText(message);
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