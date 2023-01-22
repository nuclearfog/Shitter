package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;

public class ConnectionDialog extends Dialog implements OnCheckedChangeListener, OnClickListener {

	public static final int TYPE_TWITTER = 1;

	public static final int TYPE_MASTODON = 2;

	private SwitchButton enableApi, enableV2, enableHost;
	private TextView apiLabel, v2Label, hostLabel;
	private EditText host, api1, api2;
	private Button confirm;

	private OnConnectionSetCallback callback;
	private int type;

	public ConnectionDialog(Context context, OnConnectionSetCallback callback) {
		super(context, R.style.ConfirmDialog);
		this.callback = callback;
		setContentView(R.layout.dialog_connection);
		enableApi = findViewById(R.id.dialog_connection_custom_api);
		enableV2 = findViewById(R.id.dialog_connection_use_v2);
		enableHost = findViewById(R.id.dialog_connection_custom_host);
		apiLabel = findViewById(R.id.dialog_connection_custom_api_label);
		v2Label = findViewById(R.id.dialog_connection_use_v2_label);
		hostLabel = findViewById(R.id.dialog_connection_custom_host_label);
		host = findViewById(R.id.dialog_connection_hostname);
		api1 = findViewById(R.id.dialog_connection_api1);
		api2 = findViewById(R.id.dialog_connection_api2);
		confirm = findViewById(R.id.dialog_connection_confirm);

		enableApi.setOnCheckedChangeListener(this);
		enableHost.setOnCheckedChangeListener(this);
		confirm.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_connection_confirm) {

		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.dialog_connection_custom_api) {
			if (isChecked) {
				enableV2.setVisibility(View.VISIBLE);
				v2Label.setVisibility(View.VISIBLE);
			} else {
				enableV2.setVisibility(View.INVISIBLE);
				v2Label.setVisibility(View.INVISIBLE);
			}
		}
		else if (buttonView.getId() == R.id.dialog_connection_custom_host) {
			if (isChecked) {
				host.setVisibility(View.VISIBLE);
			} else {
				host.setVisibility(View.INVISIBLE);
			}
		}
	}


	@Override
	public void show() {
	}


	public void show(int type) {
		switch(type) {
			case TYPE_TWITTER:
				host.setVisibility(View.GONE);

				break;

			case TYPE_MASTODON:
				break;
		}
		super.show();
	}


	public interface OnConnectionSetCallback {

		void onConnectionSet(String key1, String key2, String hostname);
	}
}