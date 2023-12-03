package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.TextView;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;

/**
 * dialog used to show app information and resource links
 *
 * @author nuclearfog
 */
public class InfoDialog extends Dialog {

	/**
	 *
	 */
	public InfoDialog(Activity activity) {
		super(activity, R.style.AppInfoDialog);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_app_info);
		TextView appInfo = findViewById(R.id.settings_app_info);

		appInfo.append(" V");
		appInfo.append(BuildConfig.VERSION_NAME);
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