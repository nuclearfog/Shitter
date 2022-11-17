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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.NetworkAdapter;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.Tokens;
import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.backend.async.LoginAction;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * Account Activity of the App
 * called from {@link MainActivity} when this app isn't logged in
 *
 * @author nuclearfog
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener, OnCheckedChangeListener, OnItemSelectedListener {

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

	/**
	 * ID used if Twitter is selected
	 */
	public static final int SELECTOR_TWITTER = 10;

	private LoginAction loginAsync;
	private GlobalSettings settings;

	private EditText pinInput, apiKey1, apiKey2;
	private SwitchButton apiSwitch;
	private ViewGroup root;

	@Nullable
	private String requestToken;
	private int hostSelected = SELECTOR_TWITTER;


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
		Spinner hostSelector = findViewById(R.id.login_network_selector);
		View switchLabel = findViewById(R.id.login_enable_key_input_label);
		apiSwitch = findViewById(R.id.login_enable_key_input);
		root = findViewById(R.id.login_root);
		pinInput = findViewById(R.id.login_enter_code);
		apiKey1 = findViewById(R.id.login_enter_key1);
		apiKey2 = findViewById(R.id.login_enter_key2);

		settings = GlobalSettings.getInstance(this);
		toolbar.setTitle(R.string.login_info);
		setSupportActionBar(toolbar);
		pinInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.key, 0, 0, 0);
		NetworkAdapter adapter = new NetworkAdapter(this);
		hostSelector.setAdapter(adapter);
		hostSelector.setSelection(0);

		if (settings.isCustomApiSet() || !Tokens.USE_DEFAULT_KEYS) {
			if (!Tokens.USE_DEFAULT_KEYS) {
				apiSwitch.setVisibility(View.GONE);
				switchLabel.setVisibility(View.GONE);
			}
			apiSwitch.setCheckedImmediately(true);
			apiKey1.setText(settings.getConsumerKey());
			apiKey2.setText(settings.getConsumerSecret());
		} else {
			apiKey1.setVisibility(View.INVISIBLE);
			apiKey2.setVisibility(View.INVISIBLE);
		}

		AppStyles.setTheme(root, settings.getBackgroundColor());

		linkButton.setOnClickListener(this);
		loginButton.setOnClickListener(this);
		hostSelector.setOnItemSelectedListener(this);
		apiSwitch.setOnCheckedChangeListener(this);

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
			AppStyles.setTheme(root, settings.getBackgroundColor());
		}
	}


	@Override
	public void onClick(View v) {
		if (loginAsync != null && loginAsync.getStatus() == RUNNING)
			return;
		// get login request token
		if (v.getId() == R.id.login_get_link) {
			if (hostSelected == SELECTOR_TWITTER) {
				if (requestToken == null) {
					// check if input is ok
					if (apiSwitch.isChecked() && (apiKey1.length() == 0 || apiKey2.length() == 0)) {
						if (apiKey1.length() == 0) {
							apiKey1.setError(getString(R.string.error_empty_token));
						}
						if (apiKey2.length() == 0) {
							apiKey2.setError(getString(R.string.error_empty_token));
						}
					}
					// use user defined token keys
					else if (apiSwitch.isChecked()) {
						String apiTxt1 = apiKey1.getText().toString();
						String apiTxt2 = apiKey2.getText().toString();
						Toast.makeText(this, R.string.info_open_twitter_login, LENGTH_LONG).show();
						loginAsync = new LoginAction(this, LoginAction.MODE_REQUEST, hostSelected);
						loginAsync.execute(apiTxt1, apiTxt2);
					}
					// use system keys
					else if (Tokens.USE_DEFAULT_KEYS) {
						Toast.makeText(this, R.string.info_open_twitter_login, LENGTH_LONG).show();
						loginAsync = new LoginAction(this, LoginAction.MODE_REQUEST, hostSelected);
						loginAsync.execute(null, null);
					}
				} else {
					// re-use request token
					connect();
				}
			}
		}
		// verify login credentials
		else if (v.getId() == R.id.login_verifier) {
			if (hostSelected == SELECTOR_TWITTER) {
				// check if user clicked on PIN button
				if (requestToken == null) {
					Toast.makeText(this, R.string.info_get_link, LENGTH_LONG).show();
				}
				// check if input is ok
				else if (pinInput.length() == 0 || (apiSwitch.isChecked() && (apiKey1.length() == 0 || apiKey2.length() == 0))) {
					if (apiKey1.length() == 0) {
						apiKey1.setError(getString(R.string.error_empty_token));
					}
					if (apiKey2.length() == 0) {
						apiKey2.setError(getString(R.string.error_empty_token));
					}
					if (pinInput.length() == 0) {
						pinInput.setError(getString(R.string.error_enter_pin));
					}
				}
				// login app
				else {
					String twitterPin = pinInput.getText().toString();
					Toast.makeText(this, R.string.info_login_to_twitter, LENGTH_LONG).show();
					loginAsync = new LoginAction(this, LoginAction.MODE_LOGIN, hostSelected);
					if (apiSwitch.isChecked()) {
						String apiTxt1 = apiKey1.getText().toString();
						String apiTxt2 = apiKey2.getText().toString();
						loginAsync.execute(requestToken, twitterPin, apiTxt1, apiTxt2);
					} else {
						loginAsync.execute(requestToken, twitterPin);
					}
				}
			}
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.login_enable_key_input) {
			if (isChecked) {
				apiKey1.setVisibility(View.VISIBLE);
				apiKey2.setVisibility(View.VISIBLE);
			} else {
				apiKey1.setVisibility(View.INVISIBLE);
				apiKey2.setVisibility(View.INVISIBLE);
			}
			// reset request token
			requestToken = null;
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (id == NetworkAdapter.ID_TWITTER) {
			hostSelected = SELECTOR_TWITTER;
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	/**
	 * Called when the app is registered successfully
	 */
	public void onSuccess(int mode, String result) {
		switch (mode) {
			case LoginAction.MODE_LOGIN:
				setResult(RETURN_LOGIN_SUCCESSFUL);
				finish();
				break;

			case LoginAction.MODE_REQUEST:
				requestToken = result;
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
		String link = Twitter.AUTHENTICATE + "?oauth_token=" + requestToken;
		Intent loginIntent = new Intent(ACTION_VIEW, Uri.parse(link));
		try {
			startActivity(loginIntent);
		} catch (ActivityNotFoundException err) {
			Toast.makeText(this, R.string.error_open_link, LENGTH_SHORT).show();
		}
	}
}