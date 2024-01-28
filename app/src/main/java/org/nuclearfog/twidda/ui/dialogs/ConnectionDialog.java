package org.nuclearfog.twidda.ui.dialogs;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.update.ConnectionUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;

/**
 * API connection setup dialog
 *
 * @author nuclearfog
 */
public class ConnectionDialog extends DialogFragment implements OnClickListener {

	/**
	 *
	 */
	private static final String TAG = "ConnectionDialog";

	/**
	 * bundle key used to set/restore connection configuration
	 * value type is {@link ConnectionUpdate}
	 */
	private static final String KEY_CONNECTION = "dialog-connection";


	private EditText host, appName;

	private ConnectionUpdate connection = new ConnectionUpdate();

	/**
	 *
	 */
	public ConnectionDialog() {
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		setStyle(STYLE_NO_TITLE, R.style.ConfirmDialog);
		View view = inflater.inflate(R.layout.dialog_connection, container, false);
		Button confirm = view.findViewById(R.id.dialog_connection_confirm);
		Button discard = view.findViewById(R.id.dialog_connection_discard);
		host = view.findViewById(R.id.dialog_connection_hostname);
		appName = view.findViewById(R.id.dialog_connection_app_name);

		AppStyles.setTheme((ViewGroup) view);

		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_CONNECTION);
			if (data instanceof ConnectionUpdate)
				connection = (ConnectionUpdate) data;
		}
		confirm.setOnClickListener(this);
		discard.setOnClickListener(this);
		return view;
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_CONNECTION, connection);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_connection_confirm) {
			String appNameStr = appName.getText().toString();
			String hostText = host.getText().toString();
			if (!appNameStr.trim().isEmpty()) {
				connection.setAppName(appNameStr);
			} else {
				connection.setAppName("");
			}
			if (hostText.trim().isEmpty() || Patterns.WEB_URL.matcher(hostText).matches()) {
				connection.setHostname(hostText);
				if (getActivity() instanceof OnConnectionSetListener) {
					((OnConnectionSetListener) getActivity()).onConnecionSet(connection);
				}
				dismiss();
			} else {
				host.setError(v.getContext().getString(R.string.info_missing_host));
			}
		} else if (v.getId() == R.id.dialog_connection_discard) {
			dismiss();
		}
	}

	/**
	 * show connection settings dialog
	 *
	 * @param activity         activity from which to show this dialog
	 * @param connectionUpdate connection configturation to update
	 */
	public static void show(FragmentActivity activity, ConnectionUpdate connectionUpdate) {
		Bundle args = new Bundle();
		args.putSerializable(KEY_CONNECTION, connectionUpdate);
		Fragment dialogFragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
		if (dialogFragment == null) {
			ConnectionDialog dialog = new ConnectionDialog();
			dialog.setArguments(args);
			dialog.show(activity.getSupportFragmentManager(), TAG);
		}
	}

	/**
	 * Callback listener used to set the conneciton configuration
	 */
	public interface OnConnectionSetListener {

		/**
		 * called to set the connection configuration
		 */
		void onConnecionSet(ConnectionUpdate connectionUpdate);
	}
}