package org.nuclearfog.twidda.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.view.View.VISIBLE;
import static android.view.Window.FEATURE_NO_TITLE;

/**
 * dialog to show a rotating circle with a cross button inside
 *
 * @author nuclearfog
 */
public class ProgressDialog extends Dialog implements OnClickListener {

    private OnProgressStopListener l;

    /**
     *
     */
    public ProgressDialog(Context context, @Nullable OnProgressStopListener l) {
        super(context, R.style.LoadingDialog);
        // setup dialog
        requestWindowFeature(FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        setContentView(R.layout.item_load);
        ImageView cancel = findViewById(R.id.kill_button);
        ProgressBar circle = findViewById(R.id.progress_item);

        GlobalSettings settings = GlobalSettings.getInstance(context);
        AppStyles.setProgressColor(circle, settings.getHighlightColor());
        AppStyles.setDrawableColor(cancel, settings.getIconColor());

        if (l != null) {
            cancel.setVisibility(VISIBLE);
            cancel.setImageResource(R.drawable.cross);
            cancel.setOnClickListener(this);
            this.l = l;
        }
    }


    @Override
    public void onClick(View v) {
        if (l != null) {
            l.stopProgress();
            dismiss();
        }
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