package org.nuclearfog.twidda.ui.dialogs;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.Window.FEATURE_NO_TITLE;

import android.app.Dialog;
import android.content.Context;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.twitter.Tokens;
import org.nuclearfog.twidda.backend.utils.AppStyles;

/**
 * API connection setup dialog
 *
 * @author nuclearfog
 */
public class ConnectionDialog extends Dialog implements OnCheckedChangeListener, OnClickListener {

	public static final int TYPE_TWITTER = 1;

	public static final int TYPE_MASTODON = 2;

	private SwitchButton enableApi, enableV2, enableHost;
	private TextView apiLabel, v2Label, hostLabel;
	private EditText host, api1, api2;

	private OnConnectionSetCallback callback;
	private int type;


	public ConnectionDialog(Context context, OnConnectionSetCallback callback) {
		super(context, R.style.ConfirmDialog);
		this.callback = callback;
		requestWindowFeature(FEATURE_NO_TITLE);
		setCanceledOnTouchOutside(false);
		setCancelable(false);
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

		int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
		getWindow().setLayout(width, WRAP_CONTENT);
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
			switch (type) {
				case TYPE_TWITTER:
					if (enableApi.isChecked() && !api1Text.trim().isEmpty() && !api2Text.trim().isEmpty()) {
						if (enableV2.isChecked()) {
							callback.onConnectionSet(api1Text, api2Text, OnConnectionSetCallback.TWITTER_V2);
						} else {
							callback.onConnectionSet(api1Text, api2Text, OnConnectionSetCallback.TWITTER_V1);
						}
						dismiss();
					} else if (!enableApi.isChecked()) {
						if (!Tokens.DISABLE_API_V2) {
							callback.onConnectionSet(null, null, OnConnectionSetCallback.TWITTER_V2);
						} else {
							callback.onConnectionSet(null, null, OnConnectionSetCallback.TWITTER_V1);
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

				case TYPE_MASTODON:
					if (enableHost.isChecked() && Patterns.WEB_URL.matcher(hostText).matches()) {
						callback.onConnectionSet(null, null, hostText);
						dismiss();
					} else if (!enableHost.isChecked()) {
						callback.onConnectionSet(null, null, null);
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


	public void show(int type) {
		switch (type) {
			case TYPE_TWITTER:
				enableApi.setCheckedImmediately(false);
				enableV2.setCheckedImmediately(false);
				enableApi.setVisibility(View.VISIBLE);
				apiLabel.setVisibility(View.VISIBLE);
				api1.setVisibility(View.INVISIBLE);
				api2.setVisibility(View.INVISIBLE);
				hostLabel.setVisibility(View.GONE);
				enableHost.setVisibility(View.GONE);
				host.setVisibility(View.GONE);
				break;

			case TYPE_MASTODON:
				enableHost.setCheckedImmediately(false);
				hostLabel.setVisibility(View.VISIBLE);
				enableHost.setVisibility(View.VISIBLE);
				host.setVisibility(View.INVISIBLE);
				enableApi.setVisibility(View.GONE);
				apiLabel.setVisibility(View.GONE);
				enableV2.setVisibility(View.GONE);
				v2Label.setVisibility(View.GONE);
				api1.setVisibility(View.GONE);
				api2.setVisibility(View.GONE);
				break;
		}
		if (api1.getError() != null)
			api1.setError(null);
		if (api2.getError() != null)
			api2.setError(null);
		if (host.getError() != null)
			host.setError(null);
		this.type = type;
		super.show();
	}

	/**
	 * Callback used to set API settings back to activity
	 */
	public interface OnConnectionSetCallback {

		String TWITTER_V1 = "api-v1";

		String TWITTER_V2 = "api-v2";

		/**
		 * @param key1     first API key
		 * @param key2     second API key
		 * @param hostname hostname or type of API {@link #TWITTER_V1,#TWITTER_V2}
		 */
		void onConnectionSet(@Nullable String key1, @Nullable String key2, @Nullable String hostname);
	}
}