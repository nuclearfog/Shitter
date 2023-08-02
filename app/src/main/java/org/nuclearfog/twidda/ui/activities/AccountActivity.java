package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.fragments.AccountFragment;
import org.nuclearfog.twidda.ui.fragments.ListFragment;

import java.io.Serializable;

/**
 * account manager activity
 *
 * @author nuclearfog
 */
public class AccountActivity extends AppCompatActivity implements ActivityResultCallback<ActivityResult> {

	/**
	 * return code to notify that a new account was selected
	 */
	public static final int RETURN_ACCOUNT_CHANGED = 0x3660;

	/**
	 * return code to notify if settings may changed
	 */
	public static final int RETURN_SETTINGS_CHANGED = 0x336;

	/**
	 * key used to return selected login account
	 * value type is {@link org.nuclearfog.twidda.model.Account}
	 */
	public static final String RETURN_ACCOUNT = "account";

	/**
	 * key to disable account selector option from menu
	 * value type is Boolean
	 */
	public static final String KEY_DISABLE_SELECTOR = "disable-acc-manager";

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private GlobalSettings settings;
	private ListFragment fragment;

	private ViewGroup root;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_fragment);
		Toolbar tool = findViewById(R.id.fragment_toolbar);
		root = findViewById(R.id.fragment_root);
		fragment = new AccountFragment();

		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, fragment);
		fragmentTransaction.commit();

		tool.setTitle(R.string.account_page);
		setSupportActionBar(tool);

		settings = GlobalSettings.get(this);
		AppStyles.setTheme(root);
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.accounts, m);
		// disable account selector icon if this activity started from LoginActivity
		boolean disableSelector = getIntent().getBooleanExtra(KEY_DISABLE_SELECTOR, false);
		m.findItem(R.id.action_add_account).setVisible(!disableSelector);
		// theme icons
		AppStyles.setMenuIconColor(m, settings.getIconColor());
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.action_add_account) {
			// open login page to add new account
			Intent loginIntent = new Intent(this, LoginActivity.class);
			activityResultLauncher.launch(loginIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		switch (result.getResultCode()) {
			case LoginActivity.RETURN_LOGIN_SUCCESSFUL:
				Intent intent = new Intent();
				if (result.getData() != null) {
					Serializable data = result.getData().getSerializableExtra(LoginActivity.RETURN_ACCOUNT);
					intent.putExtra(RETURN_ACCOUNT, data);
				}
				// new account registered, reload fragment
				setResult(AccountActivity.RETURN_ACCOUNT_CHANGED, intent);
				fragment.reset();
				break;

			case LoginActivity.RETURN_SETTINGS_CHANGED:
				AppStyles.setTheme(root);
				setResult(RETURN_SETTINGS_CHANGED);
				fragment.reset();
				break;
		}
	}
}