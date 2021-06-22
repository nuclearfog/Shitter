package org.nuclearfog.twidda.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.Registration;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.content.Intent.ACTION_VIEW;
import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.AccountActivity.KEY_DISABLE_SELECTOR;
import static org.nuclearfog.twidda.activity.AccountActivity.RET_ACCOUNT_CHANGE;

/**
 * Account Activity of the App
 * called from {@link MainActivity} when this app isn't logged in to twitter
 *
 * @author nuclearfog
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener {

    /**
     * request code to open account manager
     */
    private static final int REQUEST_ACCOUNT_SELECT = 0x384F;

    private Registration registerAsync;
    private GlobalSettings settings;

    private EditText pinInput;
    private View root;

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

        linkButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        AppStyles.setTheme(settings, root);
    }


    @Override
    protected void onDestroy() {
        if (registerAsync != null && registerAsync.getStatus() == RUNNING)
            registerAsync.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.login, m);
        AppStyles.setMenuIconColor(m, settings.getIconColor());
        m.findItem(R.id.login_select_account).setVisible(!settings.isLoggedIn());
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.login_setting) {
            Intent settings = new Intent(this, AppSettings.class);
            startActivity(settings);
        } else if (item.getItemId() == R.id.login_select_account) {
            Intent accountManager = new Intent(this, AccountActivity.class);
            accountManager.putExtra(KEY_DISABLE_SELECTOR, true);
            startActivityForResult(accountManager, REQUEST_ACCOUNT_SELECT);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ACCOUNT_SELECT && resultCode == RET_ACCOUNT_CHANGE) {
            // account selected, return to MainActivity
            onSuccess();
        }
    }


    @Override
    public void onClick(View v) {
        // get login request link
        if (v.getId() == R.id.login_get_link) {
            if (registerAsync == null || registerAsync.getStatus() != RUNNING) {
                Toast.makeText(this, R.string.info_fetching_link, LENGTH_LONG).show();
                registerAsync = new Registration(this);
                registerAsync.execute();
            }
        }
        // verify user
        else if (v.getId() == R.id.login_verifier) {
            // check if user clicked on PIN button
            if (registerAsync == null || registerAsync.getStatus() != FINISHED) {
                Toast.makeText(this, R.string.info_get_link, LENGTH_LONG).show();
            }
            // check if PIN exists
            else if (pinInput.getText() != null && pinInput.length() > 0) {
                Toast.makeText(this, R.string.info_login_to_twitter, LENGTH_LONG).show();
                String twitterPin = pinInput.getText().toString();
                registerAsync = new Registration(this);
                registerAsync.execute(twitterPin);
            } else {
                Toast.makeText(this, R.string.error_enter_pin, LENGTH_LONG).show();
            }
        }
    }

    /**
     * Called when a twitter login link was created
     *
     * @param link Link to twitter login page
     */
    public void connect(String link) {
        Intent loginIntent = new Intent(ACTION_VIEW, Uri.parse(link));
        try {
            startActivity(loginIntent);
        } catch (ActivityNotFoundException err) {
            Toast.makeText(this, R.string.error_connection_failed, LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the app is registered successfully to twitter
     */
    public void onSuccess() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    /**
     * called when an error occurs while login
     *
     * @param error Twitter exception
     */
    public void onError(EngineException error) {
        ErrorHandler.handleFailure(this, error);
    }
}