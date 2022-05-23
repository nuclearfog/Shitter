package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
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
        PROXY_CONFIRM,
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
    public void show(DialogType type) {
        if (isShowing())
            return;

        // attach type to the view
        confirm.setTag(type);

        // default values
        int titleVis = View.GONE;
        int titleTxt = R.string.info_error;
        int messageTxt = R.string.confirm_unknown_error;
        int confirmTxt = android.R.string.ok;
        int confirmIcon = R.drawable.check;
        int confirmVis = View.INVISIBLE;
        int cancelTxt = android.R.string.cancel;
        int cancelIcon = R.drawable.cross;
        int cancelVis = View.VISIBLE;

        switch (type) {
            case MESSAGE_DELETE:
                messageTxt = R.string.confirm_delete_message;
                break;

            case WRONG_PROXY:
                titleVis = View.VISIBLE;
                messageTxt = R.string.error_wrong_connection_settings;
                break;

            case DELETE_APP_DATA:
                messageTxt = R.string.confirm_delete_database;
                break;

            case APP_LOG_OUT:
                messageTxt = R.string.confirm_log_lout;
                break;

            case VIDEO_ERROR:
                titleVis = View.VISIBLE;
                messageTxt = R.string.error_cant_load_video;
                confirmIcon = 0;
                confirmTxt = R.string.confirm_open_link;
                cancelVis = View.GONE;
                break;

            case LIST_EDITOR_LEAVE:
            case PROFILE_EDITOR_LEAVE:
                messageTxt = R.string.confirm_discard;
                break;

            case TWEET_EDITOR_LEAVE:
                messageTxt = R.string.confirm_cancel_tweet;
                break;

            case MESSAGE_EDITOR_LEAVE:
                messageTxt = R.string.confirm_cancel_message;
                break;

            case LIST_EDITOR_ERROR:
            case MESSAGE_EDITOR_ERROR:
            case TWEET_EDITOR_ERROR:
            case PROFILE_EDITOR_ERROR:
                titleVis = View.VISIBLE;
                messageTxt = R.string.error_connection_failed;
                confirmTxt = R.string.confirm_retry_button;
                break;

            case TWEET_DELETE:
                messageTxt = R.string.confirm_delete_tweet;
                break;

            case PROFILE_UNFOLLOW:
                messageTxt = R.string.confirm_unfollow;
                break;

            case PROFILE_BLOCK:
                messageTxt = R.string.confirm_block;
                break;

            case PROFILE_MUTE:
                messageTxt = R.string.confirm_mute;
                break;

            case LIST_REMOVE_USER:
                messageTxt = R.string.confirm_remove_user_from_list;
                break;

            case LIST_UNFOLLOW:
                messageTxt = R.string.confirm_unfollow_list;
                break;

            case LIST_DELETE:
                messageTxt = R.string.confirm_delete_list;
                break;

            case REMOVE_ACCOUNT:
                messageTxt = R.string.confirm_remove_account;
                break;

            case PROXY_CONFIRM:
                confirmVis = View.VISIBLE;
                titleVis = View.VISIBLE;
                titleTxt = R.string.dialog_confirm_warning;
                messageTxt = R.string.dialog_warning_videoview;
                break;
        }
        title.setVisibility(titleVis);
        title.setText(titleTxt);
        message.setText(messageTxt);

        cancel.setVisibility(cancelVis);
        cancel.setText(cancelTxt);
        cancel.setCompoundDrawablesWithIntrinsicBounds(cancelIcon, 0, 0, 0);

        confirmCheck.setVisibility(confirmVis);
        confirmDescr.setVisibility(confirmVis);

        confirm.setText(confirmTxt);
        confirm.setCompoundDrawablesWithIntrinsicBounds(confirmIcon, 0, 0, 0);

        super.show();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.confirm_yes) {
            Object tag = v.getTag();
            if (listener != null && tag instanceof DialogType) {
                DialogType type = (DialogType) tag;
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
     * set message text
     *
     * @param message message text
     */
    public void setMessage(String message) {
        this.message.setText(message);
    }

    /**
     * Alert dialog listener
     */
    public interface OnConfirmListener {

        /**
         * called when the positive button was clicked
         *
         * @param type           type of dialog
         * @param rememberChoice true if choice should be remembered
         */
        void onConfirm(DialogType type, boolean rememberChoice);
    }
}