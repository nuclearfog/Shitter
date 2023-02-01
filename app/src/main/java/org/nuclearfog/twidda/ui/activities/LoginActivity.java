package org.nuclearfog.twidda.ui.activities;

import static android.content.Intent.ACTION_VIEW;
import static android.os.AsyncTask.Status.RUNNING;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.ui.activities.AccountActivity.KEY_DISABLE_SELECTOR;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import org.nuclearfog.twidda.ui.adapter.NetworkAdapter;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.Tokens;
import org.nuclearfog.twidda.backend.async.LoginAction;
import org.nuclearfog.twidda.backend.helper.ConnectionConfig;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.dialogs.ConnectionDialog;

/**
 * Account Activity of the App
 * called from {@link MainActivity} when this app isn't logged in
 *
 * @author nuclearfog
 */
public class LoginActivity extends AppCompatActivity implements ActivityResultCallback<ActivityResult>, OnClickListener, OnItemSelectedListener {

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

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	@Nullable
	private LoginAction loginAsync;
	private GlobalSettings settings;
	private ConnectionDialog connectionDialog;

	private EditText pinInput;
	private Spinner hostSelector;
	private ViewGroup root;

	@Nullable
	private String loginLink;
	private ConnectionConfig connection = new ConnectionConfig(ConnectionConfig.API_TWITTER_2);


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.page_login);
		Toolbar toolbar = findViewById(R.id.login_toolbar);
		Button linkButton = findViewById(R.id.login_get_link);
		Button loginButton = findViewById(R.id.login_verifier);
		ImageView settingsButton = findViewById(R.id.login_network_settings);
		hostSelector = findViewById(R.id.login_network_selector);
		root = findViewById(R.id.login_root);
		pinInput = findViewById(R.id.login_enter_code);

		settings = GlobalSettings.getInstance(this);
		toolbar.setTitle(R.string.login_info);
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);

		NetworkAdapter adapter = new NetworkAdapter(this);
		connectionDialog = new ConnectionDialog(this);
		hostSelector.setAdapter(adapter);

		switch (settings.getLogin().getConfiguration()) {
			case TWITTER1:
			case TWITTER2:
				hostSelector.setSelection(1);
				break;

			case MASTODON:
				hostSelector.setSelection(0);
				break;
		}

		linkButton.setOnClickListener(this);
		loginButton.setOnClickListener(this);
		settingsButton.setOnClickListener(this);
		hostSelector.setOnItemSelectedListener(this);

		// set default result code
		setResult(RETURN_LOGIN_CANCELED);
	}


	@Override
	protected void onDestroy() {
		if (loginAsync != null && loginAsync.getStatus() == RUNNING)
			loginAsync.cancel(true);
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.login, m);
		AppStyles.setMenuIconColor(m, settings.getIconColor());
		m.findItem(R.id.login_select_account).setVisible(!settings.isLoggedIn());
		return super.onCreateOptionsMenu(m);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		// open settings page
		if (item.getItemId() == R.id.login_setting) {
			Intent intent = new Intent(this, SettingsActivity.class);
			activityResultLauncher.launch(intent);
			// notify MainActivity that settings may changed
			setResult(RETURN_SETTINGS_CHANGED);
		}
		// open account selector
		else if (item.getItemId() == R.id.login_select_account) {
			Intent accountManager = new Intent(this, AccountActivity.class);
			accountManager.putExtra(KEY_DISABLE_SELECTOR, true);
			activityResultLauncher.launch(accountManager);
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		if (result.getResultCode() == AccountActivity.RETURN_ACCOUNT_CHANGED) {
			// account selected, return to MainActivity
			setResult(RETURN_LOGIN_SUCCESSFUL);
			finish();
		} else {
			AppStyles.setTheme(root);
		}
	}


	@Override
	public void onClick(View v) {
		if (loginAsync != null && loginAsync.getStatus() == RUNNING) {
			return;
		}
		// get login request token
		if (v.getId() == R.id.login_get_link) {
			// re use login link
			if (loginLink != null) {
				connect();
			}
			// generate Twitter login link
			else if (hostSelector.getSelectedItemId() == NetworkAdapter.ID_TWITTER) {
				// use userdefined or default token keys
				if (connection.useTokens() || Tokens.USE_DEFAULT_KEYS) {
					Toast.makeText(getApplicationContext(), R.string.info_open_twitter_login, LENGTH_LONG).show();
					loginAsync = new LoginAction(this, LoginAction.MODE_REQUEST_TWITTER, connection);
					loginAsync.execute();
				}
				// no tokens are set, print error message
				else {
					Toast.makeText(getApplicationContext(), R.string.info_missing_api_keys, LENGTH_SHORT).show();
				}
			}
			// generate Mastodon login
			else if (hostSelector.getSelectedItemId() == NetworkAdapter.ID_MASTODON) {
				Toast.makeText(getApplicationContext(), R.string.info_open_mastodon_login, LENGTH_LONG).show();
				loginAsync = new LoginAction(this, LoginAction.MODE_REQUEST_MASTODON, connection);
				loginAsync.execute();
			}
		}
		// verify login credentials
		else if (v.getId() == R.id.login_verifier) {
			String code = pinInput.getText().toString();
			// check if user clicked on PIN button
			if (loginLink == null) {
				Toast.makeText(getApplicationContext(), R.string.info_get_link, LENGTH_LONG).show();
			} else if (code.isEmpty()) {
				pinInput.setError(getString(R.string.error_enter_code));
			}
			// login to Twitter
			else if (hostSelector.getSelectedItemId() == NetworkAdapter.ID_TWITTER) {
				if (connection.useTokens() || Tokens.USE_DEFAULT_KEYS) {
					Toast.makeText(getApplicationContext(), R.string.info_login_to_twitter, LENGTH_LONG).show();
					loginAsync = new LoginAction(this, LoginAction.MODE_LOGIN_TWITTER, connection);
					loginAsync.execute(loginLink, code);
				} else {
					Toast.makeText(getApplicationContext(), R.string.info_missing_api_keys, LENGTH_SHORT).show();
				}
			}
			// login to mastodon
			else if (hostSelector.getSelectedItemId() == NetworkAdapter.ID_MASTODON) {
				Toast.makeText(getApplicationContext(), R.string.info_login_to_mastodon, LENGTH_LONG).show();
				loginAsync = new LoginAction(this, LoginAction.MODE_LOGIN_MASTODON, connection);
				loginAsync.execute(loginLink, code);
			}
		}
		// open API settings dialog
		else if (v.getId() == R.id.login_network_settings) {
			if (hostSelector.getSelectedItemId() == NetworkAdapter.ID_TWITTER) {
				connectionDialog.show(connection);
			} else if (hostSelector.getSelectedItemId() == NetworkAdapter.ID_MASTODON) {
				connectionDialog.show(connection);
			}
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// reset login link after provider change
		loginLink = null;
		if (id == NetworkAdapter.ID_TWITTER) {
			connection.setApiType(ConnectionConfig.API_TWITTER_2);
		} else if (id == NetworkAdapter.ID_MASTODON) {
			connection.setApiType(ConnectionConfig.API_MASTODON);
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	/**
	 * Called when the app is registered successfully
	 */
	public void onSuccess(int mode, @NonNull String result) {
		switch (mode) {
			case LoginAction.MODE_LOGIN_MASTODON:
			case LoginAction.MODE_LOGIN_TWITTER:
				setResult(RETURN_LOGIN_SUCCESSFUL);
				finish();
				break;

			case LoginAction.MODE_REQUEST_MASTODON:
			case LoginAction.MODE_REQUEST_TWITTER:
				loginLink = result;
				connect();
				break;
		}
	}

	/**
	 * called when an error occurs while login
	 */
	public void onError(@Nullable ConnectionException exception) {
		String message = ErrorHandler.getErrorMessage(this, exception);
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * open login page
	 */
	private void connect() {
		Intent loginIntent = new Intent(ACTION_VIEW, Uri.parse(loginLink));
		try {
			startActivity(loginIntent);
		} catch (ActivityNotFoundException err) {
			Toast.makeText(getApplicationContext(), R.string.error_open_link, LENGTH_SHORT).show();
		}
	}
}