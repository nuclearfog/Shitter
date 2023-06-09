package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.webkit.WebView;

import org.nuclearfog.twidda.R;

/**
 * Dialog class to show 3rd party licenses
 *
 * @author nuclearfog
 */
public class LicenseDialog extends Dialog {


	public LicenseDialog(Context context) {
		super(context, R.style.LicenseDialog);
		WebView htmlViewer = new WebView(context);
		setContentView(htmlViewer);

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