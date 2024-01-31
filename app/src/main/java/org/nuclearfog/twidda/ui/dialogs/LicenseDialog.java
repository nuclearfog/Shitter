package org.nuclearfog.twidda.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.R;

/**
 * Dialog class to show 3rd party licenses
 *
 * @author nuclearfog
 */
public class LicenseDialog extends DialogFragment {

	private static final String TAG = "LicenseDialog";

	/**
	 *
	 */
	public LicenseDialog() {
		setStyle(STYLE_NO_TITLE, R.style.LicenseDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_license, container, false);
		WebView htmlViewer = view.findViewById(R.id.dialog_license_view);
		htmlViewer.loadUrl("file:///android_asset/licenses.html");
		return view;
	}

	/**
	 *
	 */
	public static void show(FragmentActivity activity) {
		Fragment dialogFragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
		if (dialogFragment == null) {
			LicenseDialog dialog = new LicenseDialog();
			dialog.show(activity.getSupportFragmentManager(), TAG);
		}
	}
}