package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.twitter.v1.Tokens;
import org.nuclearfog.twidda.backend.helper.update.ConnectionUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.Configuration;

/**
 * API connection setup dialog
 *
 * @author nuclearfog
 */
public class ConnectionDialog extends Dialog implements OnCheckedChangeListener, OnClickListener {

	private SwitchButton enableApi, enableV2;
	private TextView apiLabel, v2Label, appNameLabel, hostLabel;
	private EditText host, api1, api2, appName;

	private ConnectionUpdate connection;

	/**
	 *
	 */
	public ConnectionDialog(Activity activity) {
		super(activity, R.style.ConfirmDialog);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_connection);
		ViewGroup root = findViewById(R.id.dialog_connection_root);
		Button confirm = findViewById(R.id.dialog_connection_confirm);
		Button discard = findViewById(R.id.dialog_connection_discard);
		enableApi = findViewById(R.id.dialog_connection_custom_api);
		enableV2 = findViewById(R.id.dialog_connection_use_v2);
		apiLabel = findViewById(R.id.dialog_connection_custom_api_label);
		v2Label = findViewById(R.id.dialog_connection_use_v2_label);
		appNameLabel = findViewById(R.id.dialog_connection_app_name_label);
		hostLabel = findViewById(R.id.dialog_connection_hostname_label);
		host = findViewById(R.id.dialog_connection_hostname);
		api1 = findViewById(R.id.dialog_connection_api1);
		api2 = findViewById(R.id.dialog_connection_api2);
		appName = findViewById(R.id.dialog_connection_app_name);

		AppStyles.setTheme(root);

		enableApi.setOnCheckedChangeListener(this);
		confirm.setOnClickListener(this);
		discard.setOnClickListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		if (connection != null) {
			switch (connection.getApiType()) {
				case TWITTER2:
					enableV2.setCheckedImmediately(true);
					// fall through

				case TWITTER1:
					// setup Twitter configuration views
					if (connection.useTokens()) {
						api1.setText(connection.getOauthConsumerToken());
						api2.setText(connection.getOauthTokenSecret());
						enableApi.setCheckedImmediately(true);
						api1.setVisibility(View.VISIBLE);
						api2.setVisibility(View.VISIBLE);
						enableV2.setVisibility(View.VISIBLE);
						v2Label.setVisibility(View.VISIBLE);
					} else {
						enableApi.setCheckedImmediately(false);
						api1.setVisibility(View.INVISIBLE);
						api2.setVisibility(View.INVISIBLE);
						enableV2.setVisibility(View.INVISIBLE);
						v2Label.setVisibility(View.INVISIBLE);
					}
					// enable Twitter configuration views
					enableApi.setVisibility(View.VISIBLE);
					apiLabel.setVisibility(View.VISIBLE);
					// disable mastodon configuration views
					host.setVisibility(View.GONE);
					appNameLabel.setVisibility(View.GONE);
					appName.setVisibility(View.GONE);
					hostLabel.setVisibility(View.GONE);
					break;

				case MASTODON:
					// setup Mastodon configuration views
					if (connection.useHost()) {
						host.setText(connection.getHostname());
					} else {
						host.setText("");
					}
					// enable Mastodon configuration views
					host.setVisibility(View.VISIBLE);
					appNameLabel.setVisibility(View.VISIBLE);
					appName.setVisibility(View.VISIBLE);
					hostLabel.setVisibility(View.VISIBLE);
					// disable Twitter configuration views
					enableApi.setVisibility(View.GONE);
					apiLabel.setVisibility(View.GONE);
					enableV2.setVisibility(View.GONE);
					v2Label.setVisibility(View.GONE);
					api1.setVisibility(View.GONE);
					api2.setVisibility(View.GONE);
					break;
			}
		}
		// reset all error messages
		if (api1.getError() != null) {
			api1.setError(null);
		}
		if (api2.getError() != null) {
			api2.setError(null);
		}
		if (host.getError() != null) {
			host.setError(null);
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_connection_confirm) {
			switch (connection.getApiType()) {
				case TWITTER1:
				case TWITTER2:
					String api1Text = api1.getText().toString();
					String api2Text = api2.getText().toString();
					if (enableApi.isChecked()) {
						if (!api1Text.trim().isEmpty() && !api2Text.trim().isEmpty()) {
							connection.setOauthTokens(api1Text, api2Text);
						} else {
							if (api1Text.trim().isEmpty()) {
								api1.setError(getContext().getString(R.string.info_missing_key));
							}
							if (api2Text.trim().isEmpty()) {
								api2.setError(getContext().getString(R.string.info_missing_key));
							}
							return;
						}
					} else {
						connection.setOauthTokens("", "");
					}
					if (enableV2.isChecked()) {
						connection.setApiType(Configuration.TWITTER2);
					} else {
						if (Tokens.DISABLE_API_V2) {
							connection.setApiType(Configuration.TWITTER1);
						} else {
							connection.setApiType(Configuration.TWITTER2);
						}
					}
					dismiss();
					break;

				case MASTODON:
					String appNameStr = appName.getText().toString();
					String hostText = host.getText().toString();
					if (hostText.trim().isEmpty() || Patterns.WEB_URL.matcher(hostText).matches()) {
						connection.setHostname(hostText);
						dismiss();
					} else {
						host.setError(getContext().getString(R.string.info_missing_host));
					}
					if (!appNameStr.trim().isEmpty()) {
						connection.setAppName(appNameStr);
					} else {
						connection.setAppName("");
					}
					break;
			}
		} else if (v.getId() == R.id.dialog_connection_discard) {
			dismiss();
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.dialog_connection_custom_api) {
			if (isChecked) {
				enableV2.setVisibility(View.VISIBLE);
				v2Label.setVisibility(View.VISIBLE);
				api1.setVisibility(View.VISIBLE);
				api2.setVisibility(View.VISIBLE);
			} else {
				enableV2.setCheckedImmediately(false);
				enableV2.setVisibility(View.INVISIBLE);
				v2Label.setVisibility(View.INVISIBLE);
				api1.setVisibility(View.INVISIBLE);
				api2.setVisibility(View.INVISIBLE);
			}
		}
	}


	@Override
	public void show() {
		// using show(ConnectionConfig) instead
	}


	@Override
	public void dismiss() {
		if (isShowing()) {
			super.dismiss();
		}
	}

	/**
	 *
	 */
	public void show(ConnectionUpdate connection) {
		if (!isShowing()) {
			this.connection = connection;
			super.show();
		}
	}
}