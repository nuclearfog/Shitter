package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
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

	/**
	 *
	 */
	public ProgressDialog(Activity activity, @Nullable OnProgressStopListener listener) {
		super(activity, R.style.LoadingDialog);
		this.listener = listener;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_load);
		ProgressBar circle = findViewById(R.id.progress_item);
		ImageView cancel = findViewById(R.id.kill_button);

		GlobalSettings settings = GlobalSettings.get(getContext());
		AppStyles.setProgressColor(circle, settings.getHighlightColor());
		AppStyles.setDrawableColor(cancel, settings.getIconColor());
		if (listener != null) {
			cancel.setVisibility(View.VISIBLE);
		} else {
			cancel.setVisibility(View.GONE);
		}
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
	 * listener for progress
	 */
	public interface OnProgressStopListener {

		/**
		 * called when the progress stop button was clicked
		 */
		void stopProgress();
	}
}