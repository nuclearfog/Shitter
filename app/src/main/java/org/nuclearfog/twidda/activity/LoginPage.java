package org.nuclearfog.twidda.activity;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.Registration;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.content.Intent.ACTION_VIEW;
import static android.os.AsyncTask.Status.RUNNING;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

public class LoginPage extends AppCompatActivity implements OnClickListener {

    private GlobalSettings settings;
    private Registration registerAsync;
    private Button btnLink, btnVeri;
    private EditText pin;
    private View root;
    private String link;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_login);
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        btnLink = findViewById(R.id.linkButton);
        btnVeri = findViewById(R.id.verifier);
        root = findViewById(R.id.login_root);
        pin = findViewById(R.id.pin);

        settings = GlobalSettings.getInstance(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        btnLink.setOnClickListener(this);
        btnVeri.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        btnLink.setTypeface(settings.getFontFace());
        btnVeri.setTypeface(settings.getFontFace());
        pin.setTypeface(settings.getFontFace());
        root.setBackgroundColor(settings.getBackgroundColor());
        FontTool.setViewFont(root, settings.getFontFace());
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
    public boolean onOptionsItemSelected(MenuItem item) {
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
                } else if (pin.getText() != null) {
                    String twitterPin = pin.getText().toString();
                    if (!twitterPin.trim().isEmpty()) {
                        registerAsync = new Registration(this);
                        registerAsync.execute(twitterPin);
                    } else {
                        Toast.makeText(this, R.string.enter_pin, LENGTH_LONG).show();
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
            Toast.makeText(this, R.string.connection_failed, LENGTH_SHORT).show();
    }
}