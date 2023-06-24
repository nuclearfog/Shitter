package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * dialog to show a rotating circle with a cross button inside
 *
 * @author nuclearfog
 */
public class ProgressDialog extends Dialog implements OnClickListener {

	@Nullable
	private OnProgressStopListener listener;

	private ImageView cancel;

	/**
	 *
	 */
	public ProgressDialog(Activity activity) {
		super(activity, R.style.LoadingDialog);
		// setup dialog
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCanceledOnTouchOutside(false);
		setCancelable(false);

		setContentView(R.layout.item_load);
		cancel = findViewById(R.id.kill_button);
		ProgressBar circle = findViewById(R.id.progress_item);

		GlobalSettings settings = GlobalSettings.get(activity);
		AppStyles.setProgressColor(circle, settings.getHighlightColor());
		AppStyles.setDrawableColor(cancel, settings.getIconColor());

		cancel.setOnClickListener(this);
	}


	@Override
	public void show() {
		if (!isShowing()) {
			super.show();
		}
	}


	@Override
	public void dismiss() {
		if (isShowing()) {
			super.dismiss();
		}
	}


	@Override
	public void onClick(View v) {
		if (listener != null) {
			listener.stopProgress();
			dismiss();
		}
	}

	/**
	 * enables cancel button and adds a listener
	 */
	public void addOnProgressStopListener(OnProgressStopListener listener) {
		cancel.setVisibility(View.VISIBLE);
		this.listener = listener;
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