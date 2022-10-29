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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.backend.async.LoginAction;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * Account Activity of the App
 * called from {@link MainActivity} when this app isn't logged in to twitter
 *
 * @author nuclearfog
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener {

	/**
	 * request code to open {@link AccountActivity}
	 */
	private static final int REQUEST_ACCOUNT_SELECT = 0x384F;

	/**
	 * return code to notify if a login process was successful
	 */
	public static final int RETURN_LOGIN_SUCCESSFUL = 0x145;

	/**
	 * return code to notify if settings may changed
	 */
	public static final int RETURN_SETTINGS_CHANGED = 0x227;

	private LoginAction registerAsync;
	private GlobalSettings settings;

	private EditText pinInput;
	private ViewGroup root;

	private String requestToken;

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
		root = findViewById(R.id.login_root);
		pinInput = findViewById(R.id.login_enter_pin);

		settings = GlobalSettings.getInstance(this);
		toolbar.setTitle(R.string.login_info);
		setSupportActionBar(toolbar);
		pinInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.key, 0, 0, 0);
		setResult(RESULT_CANCELED);

		linkButton.setOnClickListener(this);
		loginButton.setOnClickListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		AppStyles.setTheme(root, settings.getBackgroundColor());
	}


	@Override
	protected void onDestroy() {
		if (registerAsync != null && registerAsync.getStatus() == RUNNING)
			registerAsync.cancel(true);
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
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			// notify MainActivity that settings will change maybe
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
		if (requestCode == REQUEST_ACCOUNT_SELECT && resultCode == AccountActivity.RETURN_ACCOUNT_CHANGED) {
			// account selected, return to MainActivity
			onSuccess();
		}
	}


	@Override
	public void onClick(View v) {
		// get login request token
		if (v.getId() == R.id.login_get_link) {
			if (requestToken == null) {
				if (registerAsync == null || registerAsync.getStatus() != RUNNING) {
					Toast.makeText(this, R.string.info_fetching_link, LENGTH_LONG).show();
					registerAsync = new LoginAction(this);
					registerAsync.execute();
				}
			} else {
				// re-use request token
				connect();
			}
		}
		// verify login credentials
		else if (v.getId() == R.id.login_verifier) {
			// check if user clicked on PIN button
			if (requestToken == null) {
				Toast.makeText(this, R.string.info_get_link, LENGTH_LONG).show();
			}
			// check if PIN exists
			else if (pinInput.length() == 0) {
				Toast.makeText(this, R.string.error_enter_pin, LENGTH_LONG).show();
			}
			//
			else if (registerAsync == null || registerAsync.getStatus() != RUNNING) {
				if (pinInput.getText() != null && pinInput.length() > 0) {
					Toast.makeText(this, R.string.info_login_to_twitter, LENGTH_LONG).show();
					String twitterPin = pinInput.getText().toString();
					registerAsync = new LoginAction(this);
					registerAsync.execute(requestToken, twitterPin);
				}
			}
		}
	}

	/**
	 * Called when the app is registered successfully to twitter
	 */
	public void onSuccess() {
		setResult(RETURN_LOGIN_SUCCESSFUL);
		finish();
	}

	/**
	 * called when an error occurs while login
	 *
	 * @param error Twitter exception
	 */
	public void onError(@Nullable ConnectionException error) {
		ErrorHandler.handleFailure(this, error);
	}

	/**
	 * Called when a twitter login link was created
	 *
	 * @param requestToken temporary request token
	 */
	public void connect(String requestToken) {
		this.requestToken = requestToken;
		connect();
	}


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