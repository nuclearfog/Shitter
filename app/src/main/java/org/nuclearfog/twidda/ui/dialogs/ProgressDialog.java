package org.nuclearfog.twidda.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * dialog to show a rotating circle with a cross button inside
 *
 * @author nuclearfog
 */
public class ProgressDialog extends DialogFragment implements OnClickListener {

	/**
	 *
	 */
	private static final String TAG = "progress_dialog";

	/**
	 * Bundle key used to set progress dialog cancellable
	 * value type is boolean
	 */
	private static final String KEY_CANCELLABLE = "progress_dismiss_enable";

	private boolean cancellable = false;

	/**
	 *
	 */
	public ProgressDialog() {
		setStyle(STYLE_NO_TITLE, R.style.LoadingDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.item_load, container, false);
		ProgressBar circle = view.findViewById(R.id.progress_item);
		ImageView cancel = view.findViewById(R.id.kill_button);

		GlobalSettings settings = GlobalSettings.get(requireContext());
		AppStyles.setProgressColor(circle, settings.getHighlightColor());
		AppStyles.setDrawableColor(cancel, settings.getIconColor());

		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			cancellable = savedInstanceState.getBoolean(KEY_CANCELLABLE, false);
			if (cancellable) {
				cancel.setVisibility(View.VISIBLE);
			} else {
				cancel.setVisibility(View.GONE);
			}
		}
		cancel.setOnClickListener(this);
		return view;
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putBoolean(KEY_CANCELLABLE, cancellable);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.kill_button) {
			if (getActivity() instanceof OnProgressStopListener) {
				((OnProgressStopListener) getActivity()).stopProgress();
			}
			dismiss();
		}
	}

	/**
	 * show progress dialog
	 *
	 * @param activity   activity from which to show the dialog
	 * @param cancelable true to enable cancel button
	 */
	public static void show(FragmentActivity activity, boolean cancelable) {
		Fragment dialogFragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
		if (dialogFragment == null) {
			ProgressDialog dialog = new ProgressDialog();
			Bundle args = new Bundle();
			args.putBoolean(KEY_CANCELLABLE, cancelable);
			dialog.setArguments(args);
			dialog.show(activity.getSupportFragmentManager(), TAG);
		}
	}

	/**
	 * dismiss dialog shown by the activity
	 *
	 * @param activity activity containing an instance of this dialog
	 */
	public static void dismiss(FragmentActivity activity) {
		Fragment dialogFragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
		if (dialogFragment instanceof ProgressDialog) {
			((ProgressDialog) dialogFragment).dismiss();
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