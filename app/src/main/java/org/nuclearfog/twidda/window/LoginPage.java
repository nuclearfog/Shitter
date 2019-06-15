package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.net.ConnectivityManager;
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
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.content.Intent.ACTION_VIEW;
import static android.os.AsyncTask.Status.RUNNING;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.backend.Registration.Mode.LINK;
import static org.nuclearfog.twidda.backend.Registration.Mode.LOGIN;


public class LoginPage extends AppCompatActivity implements OnClickListener {

    private Registration registerAsync;
    private EditText pin;
    private View root;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_login);
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        Button btnLink = findViewById(R.id.linkButton);
        Button btnVeri = findViewById(R.id.verifier);
        root = findViewById(R.id.login_root);
        pin = findViewById(R.id.pin);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        btnLink.setOnClickListener(this);
        btnVeri.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
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
                registerAsync = new Registration(this, LINK);
                registerAsync.execute("");
                break;

            case R.id.verifier:
                String twitterPin = pin.getText().toString();
                if (!twitterPin.trim().isEmpty()) {
                    registerAsync = new Registration(this, LOGIN);
                    registerAsync.execute(twitterPin);
                } else {
                    Toast.makeText(this, R.string.enter_pin, LENGTH_LONG).show();
                }
                break;
        }
    }


    public void connect(String link) {
        ConnectivityManager mConnect = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (mConnect != null && mConnect.getActiveNetworkInfo() != null) {
            if (mConnect.getActiveNetworkInfo().isConnected()) {
                Intent browser = new Intent(ACTION_VIEW);
                browser.setData(Uri.parse(link));
                startActivity(browser);
            }
        } else {
            Toast.makeText(this, R.string.connection_failed, LENGTH_SHORT).show();
        }
    }
}