package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.webkit.WebView;

import org.nuclearfog.twidda.R;


/**
 * Dialog class to show 3rd party licenses
 *
 * @author nuclearfog
 */
public class LicenseDialog extends Dialog {

	/**
	 *
	 */
	public LicenseDialog(Activity activity) {
		super(activity, R.style.LicenseDialog);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_license);
		WebView htmlViewer = findViewById(R.id.dialog_license_view);
		htmlViewer.loadUrl("file:///android_asset/licenses.html");
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
}