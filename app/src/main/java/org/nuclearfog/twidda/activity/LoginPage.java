package org.nuclearfog.twidda.activity;

import android.app.Activity;
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
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.content.Intent.ACTION_VIEW;
import static android.os.AsyncTask.Status.RUNNING;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

public class LoginPage extends AppCompatActivity implements OnClickListener {

    private Registration registerAsync;
    private Button linkButton, loginButton;
    private EditText pinInput;
    private View root;
    @Nullable
    private String link;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_login);
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        linkButton = findViewById(R.id.linkButton);
        loginButton = findViewById(R.id.verifier);
        root = findViewById(R.id.login_root);
        pinInput = findViewById(R.id.pin);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        linkButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        GlobalSettings settings = GlobalSettings.getInstance(this);
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
        if (registerAsync != null && registerAsync.getStatus() == RUNNING)
            registerAsync.cancel(true);

        switch (v.getId()) {
            case R.id.linkButton:
                if (link == null) {
                    registerAsync = new Registration(this);
                    registerAsync.execute();
                } else {
                    connect(link);
                }
                break;

            case R.id.verifier:
                if (link == null) {
                    Toast.makeText(this, R.string.info_get_link, LENGTH_LONG).show();
                } else if (pinInput.getText() != null) {
                    String twitterPin = pinInput.getText().toString();
                    if (!twitterPin.trim().isEmpty()) {
                        registerAsync = new Registration(this);
                        registerAsync.execute(twitterPin);
                    } else {
                        Toast.makeText(this, R.string.error_enter_pin, LENGTH_LONG).show();
                    }
                }
                break;
        }
    }


    public void connect(String link) {
        this.link = link;
        Intent loginIntent = new Intent(ACTION_VIEW, Uri.parse(link));
        if (loginIntent.resolveActivity(getPackageManager()) != null)
            startActivity(loginIntent);
        else
            Toast.makeText(this, R.string.error_connection_failed, LENGTH_SHORT).show();
    }


    public void onSuccess() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    public void onError(EngineException error) {
        ErrorHandler.handleFailure(this, error);
    }
}