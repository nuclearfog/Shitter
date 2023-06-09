package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.twitter.v1.Tokens;
import org.nuclearfog.twidda.backend.helper.ConnectionConfig;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.Configuration;

/**
 * API connection setup dialog
 *
 * @author nuclearfog
 */
public class ConnectionDialog extends Dialog implements OnCheckedChangeListener, OnClickListener {

	private SwitchButton enableApi, enableV2, enableHost;
	private TextView apiLabel, v2Label, hostLabel;
	private EditText host, api1, api2;

	private ConnectionConfig connection;


	public ConnectionDialog(Context context) {
		super(context, R.style.ConfirmDialog);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCanceledOnTouchOutside(false);
		setContentView(R.layout.dialog_connection);
		ViewGroup root = findViewById(R.id.dialog_connection_root);
		Button confirm = findViewById(R.id.dialog_connection_confirm);
		Button discard = findViewById(R.id.dialog_connection_discard);
		enableApi = findViewById(R.id.dialog_connection_custom_api);
		enableV2 = findViewById(R.id.dialog_connection_use_v2);
		enableHost = findViewById(R.id.dialog_connection_custom_host);
		apiLabel = findViewById(R.id.dialog_connection_custom_api_label);
		v2Label = findViewById(R.id.dialog_connection_use_v2_label);
		hostLabel = findViewById(R.id.dialog_connection_custom_host_label);
		host = findViewById(R.id.dialog_connection_hostname);
		api1 = findViewById(R.id.dialog_connection_api1);
		api2 = findViewById(R.id.dialog_connection_api2);

		int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9f);
		getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
		AppStyles.setTheme(root);
		enableApi.setOnCheckedChangeListener(this);
		enableHost.setOnCheckedChangeListener(this);
		confirm.setOnClickListener(this);
		discard.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_connection_confirm) {
			String api1Text = api1.getText().toString();
			String api2Text = api2.getText().toString();
			String hostText = host.getText().toString();
			switch (connection.getApiType()) {
				case TWITTER1:
				case TWITTER2:
					if (enableApi.isChecked() && !api1Text.trim().isEmpty() && !api2Text.trim().isEmpty()) {
						if (enableV2.isChecked()) {
							connection.setApiType(Configuration.TWITTER2);
						} else {
							connection.setApiType(Configuration.TWITTER1);
						}
						connection.setOauthTokens(api1Text, api2Text);
						dismiss();
					} else if (!enableApi.isChecked()) {
						if (Tokens.DISABLE_API_V2) {
							connection.setApiType(Configuration.TWITTER1);
						} else {
							connection.setApiType(Configuration.TWITTER2);
						}
						dismiss();
					} else {
						if (api1Text.trim().isEmpty()) {
							api1.setError(getContext().getString(R.string.info_missing_key));
						}
						if (api2Text.trim().isEmpty()) {
							api2.setError(getContext().getString(R.string.info_missing_key));
						}
					}
					break;

				case MASTODON:
					if (enableHost.isChecked() && Patterns.WEB_URL.matcher(hostText).matches()) {
						connection.setHost(hostText);
						dismiss();
					} else if (!enableHost.isChecked()) {
						connection.setHost("");
						dismiss();
					} else {
						host.setError(getContext().getString(R.string.info_missing_host));
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
		} else if (buttonView.getId() == R.id.dialog_connection_custom_host) {
			if (isChecked) {
				host.setVisibility(View.VISIBLE);
			} else {
				host.setVisibility(View.INVISIBLE);
			}
		}
	}


	@Override
	public void show() {
		// ignore method call, call instead show(int)
	}


	@Override
	public void dismiss() {
		if (isShowing()) {
			super.dismiss();
		}
	}


	public void show(ConnectionConfig connection) {
		switch (connection.getApiType()) {
			case TWITTER2:
				enableV2.setCheckedImmediately(true);
			case TWITTER1:
				if (connection.useTokens()) {
					enableApi.setCheckedImmediately(true);
					api1.setVisibility(View.VISIBLE);
					api2.setVisibility(View.VISIBLE);
					api1.setText(connection.getOauthConsumerToken());
					api2.setText(connection.getOauthTokenSecret());
				} else {
					enableApi.setCheckedImmediately(false);
					api1.setVisibility(View.INVISIBLE);
					api2.setVisibility(View.INVISIBLE);
				}
				enableApi.setVisibility(View.VISIBLE);
				apiLabel.setVisibility(View.VISIBLE);
				hostLabel.setVisibility(View.GONE);
				enableHost.setVisibility(View.GONE);
				host.setVisibility(View.GONE);
				break;

			case MASTODON:
				if (connection.useHost()) {
					enableHost.setCheckedImmediately(true);
					host.setVisibility(View.VISIBLE);
					host.setText(connection.getHostname());
				} else {
					enableHost.setCheckedImmediately(false);
					host.setVisibility(View.INVISIBLE);
				}
				hostLabel.setVisibility(View.VISIBLE);
				enableHost.setVisibility(View.VISIBLE);
				enableApi.setVisibility(View.GONE);
				apiLabel.setVisibility(View.GONE);
				enableV2.setVisibility(View.GONE);
				v2Label.setVisibility(View.GONE);
				api1.setVisibility(View.GONE);
				api2.setVisibility(View.GONE);
				break;

			default:
				return;
		}
		// erase all error messages
		if (api1.getError() != null)
			api1.setError(null);
		if (api2.getError() != null)
			api2.setError(null);
		if (host.getError() != null)
			host.setError(null);
		this.connection = connection;
		super.show();
	}
}