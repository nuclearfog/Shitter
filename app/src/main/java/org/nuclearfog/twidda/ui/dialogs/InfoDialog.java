package org.nuclearfog.twidda.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;

/**
 * dialog used to show app information and resource links
 *
 * @author nuclearfog
 */
public class InfoDialog extends DialogFragment {

	private static final String TAG = "InfoDialog";

	/**
	 *
	 */
	public InfoDialog() {
		setStyle(STYLE_NO_TITLE, R.style.AppInfoDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_app_info, container, false);
		TextView appInfo = view.findViewById(R.id.settings_app_info);
		appInfo.setText(" V");
		appInfo.append(BuildConfig.VERSION_NAME);
		AppStyles.setTheme((ViewGroup) view);
		return view;
	}

	/**
	 *
	 */
	public static void show(FragmentActivity activity) {
		Fragment dialogFragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
		if (dialogFragment == null) {
			InfoDialog dialog = new InfoDialog();
			dialog.show(activity.getSupportFragmentManager(), TAG);
		}
	}
}