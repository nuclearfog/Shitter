package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.LoginAction;
import org.nuclearfog.twidda.backend.async.LoginAction.LoginParam;
import org.nuclearfog.twidda.backend.async.LoginAction.LoginResult;
import org.nuclearfog.twidda.backend.helper.ConnectionResult;
import org.nuclearfog.twidda.backend.helper.update.ConnectionUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.backend.utils.LinkUtils;
import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.adapter.listview.DropdownAdapter;
import org.nuclearfog.twidda.ui.dialogs.ConnectionDialog;

import java.io.Serializable;

/**
 * Account Activity of the App
 * called from {@link MainActivity} when this app isn't logged in
 *
 * @author nuclearfog
 */
public class LoginActivity extends AppCompatActivity implements ActivityResultCallback<ActivityResult>, AsyncCallback<LoginResult>, OnClickListener, OnItemSelectedListener {

	/**
	 * return code to notify if a login process was successful
	 */
	public static final int RETURN_LOGIN_SUCCESSFUL = 0x145;

	/**
	 * return code to notify if a login process was successful
	 */
	public static final int RETURN_LOGIN_CANCELED = 0x2485;

	/**
	 * return code to notify if settings may changed
	 */
	public static final int RETURN_SETTINGS_CHANGED = 0x227;

	/**
	 * key to return login information to parent activity
	 * value type is {@link org.nuclearfog.twidda.model.Account}
	 */
	public static final String RETURN_ACCOUNT = "account-data";

	/**
	 * dropdown selection index of Mastodon
	 *
	 * @see R.array .networks
	 */
	private static final int IDX_MASTODON = 0;

	/**
	 * key used to save connection configuration
	 */
	private static final String KEY_SAVE = "connection_save";

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private LoginAction loginAsync;
	private GlobalSettings settings;
	private ConnectionDialog connectionDialog;
	private DropdownAdapter adapter;

	private EditText pinInput;
	private Spinner hostSelector;
	private ViewGroup root;

	@Nullable
	private ConnectionResult connectionResult;
	private ConnectionUpdate connection = new ConnectionUpdate();


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_login);
		Toolbar toolbar = findViewById(R.id.login_toolbar);
		Button linkButton = findViewById(R.id.login_get_link);
		Button loginButton = findViewById(R.id.login_verifier);
		ImageView settingsButton = findViewById(R.id.login_network_settings);
		hostSelector = findViewById(R.id.login_network_selector);
		root = findViewById(R.id.login_root);
		pinInput = findViewById(R.id.login_enter_code);

		settings = GlobalSettings.get(this);
		toolbar.setTitle(R.string.login_info);
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);

		adapter = new DropdownAdapter(this);
		connectionDialog = new ConnectionDialog(this);
		loginAsync = new LoginAction(this);

		adapter.setItems(R.array.networks);
		hostSelector.setAdapter(adapter);
		if (savedInstanceState != null) {
			Serializable data = savedInstanceState.getSerializable(KEY_SAVE);
			if (data instanceof ConnectionUpdate) {
				connection = (ConnectionUpdate) data;
			}
		}

		connection.setApiType(settings.getLogin().getConfiguration());
		switch (settings.getLogin().getConfiguration()) {
			default:
			case MASTODON:
				hostSelector.setSelection(0);
				break;
		}
		linkButton.setOnClickListener(this);
		loginButton.setOnClickListener(this);
		settingsButton.setOnClickListener(this);
		hostSelector.setOnItemSelectedListener(this);
	}


	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_SAVE, connection);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onBackPressed() {
		// set default result code
		setResult(RETURN_LOGIN_CANCELED);
		super.onBackPressed();
	}


	@Override
	protected void onDestroy() {
		loginAsync.cancel();
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.login, m);
		AppStyles.setMenuIconColor(m, settings.getIconColor());
		m.findItem(R.id.login_select_account).setVisible(!settings.isLoggedIn());
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		// open settings page
		if (item.getItemId() == R.id.login_setting) {
			Intent intent = new Intent(this, SettingsActivity.class);
			activityResultLauncher.launch(intent);
			// notify MainActivity that settings may changed
			setResult(RETURN_SETTINGS_CHANGED);
			return true;
		}
		// open account selector
		else if (item.getItemId() == R.id.login_select_account) {
			Intent accountManager = new Intent(this, AccountActivity.class);
			accountManager.putExtra(AccountActivity.KEY_DISABLE_SELECTOR, true);
			activityResultLauncher.launch(accountManager);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		switch (result.getResultCode()) {
			case AccountActivity.RETURN_ACCOUNT_CHANGED:
				// account selected, return to MainActivity
				Intent intent = new Intent();
				if (result.getData() != null) {
					// delegate login information
					intent.putExtra(RETURN_ACCOUNT, result.getData().getSerializableExtra(AccountActivity.RETURN_ACCOUNT));
				}
				setResult(RETURN_LOGIN_SUCCESSFUL, intent);
				finish();
				break;

			case SettingsActivity.RETURN_SETTINGS_CHANGED:
				setResult(RETURN_SETTINGS_CHANGED);
				AppStyles.setTheme(root);
				adapter.notifyDataSetChanged();
				break;
		}
	}


	@Override
	public void onClick(View v) {
		if (!loginAsync.isIdle()) {
			return;
		}
		// get login request token
		if (v.getId() == R.id.login_get_link) {
			// generate Mastodon login
			if (hostSelector.getSelectedItemPosition() == IDX_MASTODON) {
				Toast.makeText(getApplicationContext(), R.string.info_open_mastodon_login, Toast.LENGTH_LONG).show();
				LoginParam param = new LoginParam(LoginParam.MODE_REQUEST, connection.getApiType(), connection, "");
				loginAsync.execute(param, this);
			}
		}
		// verify login credentials
		else if (v.getId() == R.id.login_verifier) {
			String code = pinInput.getText().toString();
			// check if user clicked on PIN button
			if (connectionResult == null) {
				Toast.makeText(getApplicationContext(), R.string.info_get_link, Toast.LENGTH_LONG).show();
			} else if (code.isEmpty()) {
				pinInput.setError(getString(R.string.error_enter_code));
			}
			// login to mastodon
			else if (hostSelector.getSelectedItemPosition() == IDX_MASTODON) {
				Toast.makeText(getApplicationContext(), R.string.info_login_to_mastodon, Toast.LENGTH_LONG).show();
				LoginParam param = new LoginParam(LoginParam.MODE_LOGIN, connection.getApiType(), connection, code);
				loginAsync.execute(param, this);
			}
		}
		// open API settings dialog
		else if (v.getId() == R.id.login_network_settings) {
			if (!connectionDialog.isShowing()) {
				if (hostSelector.getSelectedItemPosition() == IDX_MASTODON) {
					connectionDialog.show(connection);
				}
				reset();
			}
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// "Mastodon" selected
		if (position == IDX_MASTODON) {
			connection.setApiType(Configuration.MASTODON);
		}
		reset();
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	@Override
	public void onResult(@NonNull LoginResult result) {
		switch (result.mode) {
			case LoginResult.MODE_LOGIN:
				Intent intent = new Intent();
				intent.putExtra(RETURN_ACCOUNT, result.account);
				setResult(RETURN_LOGIN_SUCCESSFUL, intent);
				finish();
				break;

			case LoginResult.MODE_REQUEST:
				connectionResult = result.connection;
				if (connectionResult != null) {
					connection.setConnection(connectionResult);
					connect(connectionResult.getAuthorizationUrl());
				}
				break;

			case LoginResult.MODE_ERROR:
				ErrorUtils.showErrorMessage(getApplicationContext(), result.exception);
				break;
		}
	}

	/**
	 * open login page
	 */
	private void connect(String loginLink) {
		LinkUtils.openLink(this, loginLink);
	}

	/**
	 * reset connection information
	 */
	private void reset() {
		connection.setConnection(null);
		connectionResult = null;
	}
}