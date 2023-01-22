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
import android.util.Patterns;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.NetworkAdapter;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.Tokens;
import org.nuclearfog.twidda.backend.async.LoginAction;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.ui.dialogs.ConnectionDialog;
import org.nuclearfog.twidda.ui.dialogs.ConnectionDialog.OnConnectionSetCallback;

/**
 * Account Activity of the App
 * called from {@link MainActivity} when this app isn't logged in
 *
 * @author nuclearfog
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener, OnItemSelectedListener, OnConnectionSetCallback {

	/**
	 * request code to open {@link AccountActivity}
	 */
	private static final int REQUEST_ACCOUNT_SELECT = 0x384F;

	/**
	 * request code to open {@link SettingsActivity}
	 */
	private static final int REQUEST_SETTINGS = 0x123;

	/**
	 * return code to notify if a login process was successful
	 */
	public static final int RETURN_LOGIN_SUCCESSFUL = 0x145;

	/**
	 * return code to notify if settings may changed
	 */
	public static final int RETURN_SETTINGS_CHANGED = 0x227;

	@Nullable
	private LoginAction loginAsync;
	private GlobalSettings settings;
	private ConnectionDialog connectionDialog;

	private EditText pinInput;
	private Spinner hostSelector;
	private ViewGroup root;

	@Nullable
	private String loginLink;
	@Nullable
	private String hostname, key1, key2;


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
		NetworkAdapter adapter = new NetworkAdapter(this);
		connectionDialog = new ConnectionDialog(this, this);
		hostSelector.setAdapter(adapter);
		hostSelector.setSelection(0);

		AppStyles.setTheme(root);

		linkButton.setOnClickListener(this);
		loginButton.setOnClickListener(this);
		settingsButton.setOnClickListener(this);
		hostSelector.setOnItemSelectedListener(this);

		// set default result code
		setResult(RESULT_CANCELED);
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
			startActivityForResult(intent, REQUEST_SETTINGS);
			// notify MainActivity that settings may changed
			setResult(RETURN_SETTINGS_CHANGED);
		}
		// open account selector
		else if (item.getItemId() == R.id.login_select_account) {
			Intent accountManager = new Intent(this, AccountActivity.class);
			accountManager.putExtra(KEY_DISABLE_SELECTOR, true);
			startActivityForResult(accountManager, REQUEST_ACCOUNT_SELECT);
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ACCOUNT_SELECT) {
			if (resultCode == AccountActivity.RETURN_ACCOUNT_CHANGED) {
				// account selected, return to MainActivity
				setResult(RETURN_LOGIN_SUCCESSFUL);
				finish();
			}
		} else if (requestCode == REQUEST_SETTINGS) {
			AppStyles.setTheme(root);
		}
	}


	@Override
	public void onClick(View v) {
		if (loginAsync != null && loginAsync.getStatus() == RUNNING)
			return;
		// get login request token
		if (v.getId() == R.id.login_get_link) {
			// re use login link
			if (loginLink != null) {
				connect();
			}
			// generate Twitter login link
			else if (hostSelector.getSelectedItemId() == NetworkAdapter.ID_TWITTER) {
				// use user defined token keys
				if (key1 != null && key2 != null) {
					if (key1.trim().isEmpty() || key2.trim().isEmpty()) {
						Toast.makeText(getApplicationContext(), R.string.error_empty_token, LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), R.string.info_open_twitter_login, LENGTH_LONG).show();
						loginAsync = new LoginAction(this, LoginAction.LOGIN_TWITTER, LoginAction.MODE_REQUEST);
						loginAsync.execute(key1, key2);
					}
				}
				// use system tokens
				else if (Tokens.USE_DEFAULT_KEYS) {
					Toast.makeText(getApplicationContext(), R.string.info_open_twitter_login, LENGTH_LONG).show();
					loginAsync = new LoginAction(this, LoginAction.LOGIN_TWITTER, LoginAction.MODE_REQUEST);
					loginAsync.execute();
				}
			}
			// generate Mastodon login
			else if (hostSelector.getSelectedItemId() == NetworkAdapter.ID_MASTODON) {
				if (hostname == null || Patterns.WEB_URL.matcher(hostname).matches()) {
					Toast.makeText(getApplicationContext(), R.string.info_open_mastodon_login, LENGTH_LONG).show();
					loginAsync = new LoginAction(this, LoginAction.LOGIN_MASTODON, LoginAction.MODE_REQUEST);
					if (hostname != null) {
						// open userdefined url
						String link = hostname;
						if (!link.startsWith("https://"))
							link = "https://" + link;
						if (link.endsWith("/"))
							link = link.substring(0, link.length() - 1);
						loginAsync.execute(link);
					} else {
						// use default Mastodon url (mastodon.social)
						loginAsync.execute();
					}
				} else {
					Toast.makeText(getApplicationContext(), R.string.error_invalid_url, LENGTH_LONG).show();
				}
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
				if (key1 != null && key2 != null) {
					if (key1.trim().isEmpty() || key2.trim().isEmpty()) {
						Toast.makeText(getApplicationContext(), R.string.error_empty_token, LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), R.string.info_login_to_twitter, LENGTH_LONG).show();
						loginAsync = new LoginAction(this, LoginAction.LOGIN_TWITTER, LoginAction.MODE_LOGIN);
						loginAsync.execute(loginLink, code, key1, key2);
					}
				}
				// use system tokens
				else if (Tokens.USE_DEFAULT_KEYS) {
					Toast.makeText(getApplicationContext(), R.string.info_login_to_twitter, LENGTH_LONG).show();
					loginAsync = new LoginAction(this, LoginAction.LOGIN_TWITTER, LoginAction.MODE_LOGIN);
					loginAsync.execute(loginLink, code);
				}
			}
			// login to mastodon
			else if (hostSelector.getSelectedItemId() == NetworkAdapter.ID_MASTODON) {
				Toast.makeText(getApplicationContext(), R.string.info_login_to_mastodon, LENGTH_LONG).show();
				loginAsync = new LoginAction(this, LoginAction.LOGIN_MASTODON, LoginAction.MODE_LOGIN);
				loginAsync.execute(loginLink, code);
			}
		}
		// open API settings dialog
		else if (v.getId() == R.id.login_network_settings) {
			if (hostSelector.getSelectedItemId() == NetworkAdapter.ID_TWITTER) {
				connectionDialog.show(ConnectionDialog.TYPE_TWITTER);
			} else if (hostSelector.getSelectedItemId() == NetworkAdapter.ID_MASTODON) {
				connectionDialog.show(ConnectionDialog.TYPE_MASTODON);
			}
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// reset login link after provider change
		loginLink = null;
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	@Override
	public void onConnectionSet(@Nullable String key1, @Nullable String key2, @Nullable String hostname) {
		this.hostname = hostname;
		this.key1 = key1;
		this.key2 = key2;
	}

	/**
	 * Called when the app is registered successfully
	 */
	public void onSuccess(int mode, @NonNull String result) {
		switch (mode) {
			case LoginAction.MODE_LOGIN:
				setResult(RETURN_LOGIN_SUCCESSFUL);
				finish();
				break;

			case LoginAction.MODE_REQUEST:
				loginLink = result;
				connect();
				break;
		}
	}

	/**
	 * called when an error occurs while login
	 */
	public void onError(@Nullable ConnectionException error) {
		ErrorHandler.handleFailure(this, error);
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