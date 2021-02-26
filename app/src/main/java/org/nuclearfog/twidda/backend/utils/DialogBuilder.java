package org.nuclearfog.twidda.backend.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.view.View.VISIBLE;
import static android.view.Window.FEATURE_NO_TITLE;

/**
 * this class creates alert dialogs with a custom listener
 *
 * @author nuclearfog
 */
public final class DialogBuilder {

    /**
     * types of dialogs, every dialog has its own message and title
     */
    public enum DialogType {
        WRONG_PROXY,
        DEL_DATABASE,
        APP_LOG_OUT,
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
    public static AlertDialog create(Context context, final DialogType type, final OnDialogConfirmListener listener) {
        int posButton = R.string.dialog_button_yes;
        int negButton = R.string.dialog_button_no;
        int title = 0;
        int message = 0;

        switch (type) {
            case MESSAGE_DELETE:
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

            case APP_LOG_OUT:
                message = R.string.confirm_log_lout;
                break;

            case LIST_EDITOR_LEAVE:
            case PROFILE_EDITOR_LEAVE:
                message = R.string.confirm_discard;
                break;

            case TWEET_EDITOR_LEAVE:
                message = R.string.confirm_cancel_tweet;
                break;

            case LIST_EDITOR_ERROR:
            case MESSAGE_EDITOR_ERROR:
            case TWEET_EDITOR_ERROR:
            case PROFILE_EDITOR_ERROR:
                title = R.string.info_error;
                posButton = R.string.confirm_retry_button;
                negButton = R.string.dialog_button_cancel;
                break;

            case MESSAGE_EDITOR_LEAVE:
                message = R.string.confirm_cancel_message;
                break;

            case TWEET_DELETE:
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

            case LIST_REMOVE_USER:
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
        if (message != 0)
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
     * creates an animated circle to show a progress
     *
     * @param context Activity context
     * @param l       stop listener
     * @return dialog instance to show
     */
    public static Dialog createProgress(Context context, final OnProgressStopListener l) {
        View load = View.inflate(context, R.layout.item_load, null);
        ImageView cancel = load.findViewById(R.id.kill_button);
        ProgressBar circle = load.findViewById(R.id.progress_item);
        cancel.setImageResource(R.drawable.cross);
        final Dialog loadingCircle = new Dialog(context, R.style.LoadingDialog);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        AppStyles.setProgressColor(circle, settings.getHighlightColor());
        AppStyles.setDrawableColor(cancel, settings.getIconColor());
        loadingCircle.requestWindowFeature(FEATURE_NO_TITLE);
        loadingCircle.setCancelable(false);
        loadingCircle.setContentView(load);
        cancel.setVisibility(VISIBLE);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                l.stopProgress();
                loadingCircle.dismiss();
            }
        });
        return loadingCircle;
    }

    /**
     * create dialog window with app information and links
     *
     * @param context context to create dialog
     * @return dialog instance
     */
    @NonNull
    public static Dialog createInfoDialog(Context context) {
        Dialog dialog = new Dialog(context, R.style.AppInfoDialog);
        dialog.setContentView(R.layout.dialog_app_info);
        String versionName = " V" + BuildConfig.VERSION_NAME;
        TextView appInfo = dialog.findViewById(R.id.settings_app_info);
        appInfo.append(versionName);
        return dialog;
    }

    /**
     * Alert dialog listener
     */
    public interface OnDialogConfirmListener {

        /**
         * called when the positive button was clicked
         *
         * @param type type of dialog
         */
        void onConfirm(DialogType type);
    }

    /**
     * listener for progress
     */
    public interface OnProgressStopListener {

        /**
         * called when the progress stop button was clicked
         */
        void stopProgress();
    }
}