package org.nuclearfog.twidda.activity;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.Registration;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import static android.content.Intent.ACTION_VIEW;
import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.widget.Toast.LENGTH_LONG;

/**
 * Login Activity of the App
 * called from {@link MainActivity} when this app isn't logged in to twitter
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener {

    private Registration registerAsync;
    private GlobalSettings settings;

    private Button linkButton, loginButton;
    private EditText pinInput;
    private View root;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_login);
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        linkButton = findViewById(R.id.linkButton);
        loginButton = findViewById(R.id.verifier);
        root = findViewById(R.id.login_root);
        pinInput = findViewById(R.id.pin);

        settings = GlobalSettings.getInstance(this);
        toolbar.setTitle(R.string.login_info);
        setSupportActionBar(toolbar);

        linkButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        checkTLSSupport();
    }


    @Override
    protected void onStart() {
        super.onStart();
        linkButton.setTypeface(settings.getFontFace());
        loginButton.setTypeface(settings.getFontFace());
        pinInput.setTypeface(settings.getFontFace());
        root.setBackgroundColor(settings.getBackgroundColor());
        FontTool.setViewFontAndColor(settings, root);
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
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.login_setting) {
            Intent settings = new Intent(this, AppSettings.class);
            startActivity(settings);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linkButton:
                if (registerAsync == null || registerAsync.getStatus() != RUNNING) {
                    Toast.makeText(this, R.string.info_fetching_link, LENGTH_LONG).show();
                    registerAsync = new Registration(this);
                    registerAsync.execute();
                }
                break;

            case R.id.verifier:
                if (registerAsync == null || registerAsync.getStatus() != FINISHED) {
                    Toast.makeText(this, R.string.info_get_link, LENGTH_LONG).show();
                } else if (pinInput.getText() != null && pinInput.length() > 0) {
                    Toast.makeText(this, R.string.info_login_to_twitter, LENGTH_LONG).show();
                    String twitterPin = pinInput.getText().toString();
                    registerAsync = new Registration(this);
                    registerAsync.execute(twitterPin);
                } else {
                    Toast.makeText(this, R.string.error_enter_pin, LENGTH_LONG).show();
                }
                break;
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
            // If no browser was found, a popup with the login link appears
            Dialog dialog = new Dialog(this, R.style.AppInfoDialog);
            dialog.setContentView(R.layout.dialog_login_info);
            TextView callbackURL = dialog.findViewById(R.id.login_request_link);
            callbackURL.setLinkTextColor(settings.getHighlightColor());
            callbackURL.setText(link);
            dialog.show();
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

    /**
     * Check if phone supports TLS 1.2 which is required for twitter api
     */
    private void checkTLSSupport() {
        boolean tls12Found = false;
        try {
            SSLParameters param = SSLContext.getDefault().getDefaultSSLParameters();
            String[] protocols = param.getProtocols();
            for (String protocol : protocols) {
                if (protocol.equals("TLSv1.2") || protocol.equals("TLSv1.3")) {
                    tls12Found = true;
                    break;
                }
            }
        } catch (NoSuchAlgorithmException er) {
            // ignore
        }
        if (!tls12Found) {
            Toast.makeText(this, R.string.info_phone_tls_support, LENGTH_LONG).show();
        }
    }
}