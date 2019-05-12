package org.nuclearfog.twidda.window;

import android.content.Context;
import android.content.Intent;
import android.graphics.LightingColorFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.Registration;


public class LoginPage extends AppCompatActivity implements OnClickListener {

    private Registration registerAsync;
    private EditText pin;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_login);
        pin = findViewById(R.id.pin);
        Button btnLink = findViewById(R.id.linkButton);
        Button btnVeri = findViewById(R.id.verifier);
        btnLink.getBackground().setColorFilter(new LightingColorFilter(0xffffffff, 0xff00aa));
        btnVeri.getBackground().setColorFilter(new LightingColorFilter(0xffffffff, 0xff00aa));
        btnLink.setOnClickListener(this);
        btnVeri.setOnClickListener(this);
    }


    @Override
    protected void onDestroy() {
        if (registerAsync != null && registerAsync.getStatus() == Status.RUNNING)
            registerAsync.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }


    @Override
    public void onClick(View v) {
        if (registerAsync != null && registerAsync.getStatus() == Status.RUNNING)
            registerAsync.cancel(true);

        switch (v.getId()) {
            case R.id.linkButton:
                registerAsync = new Registration(this);
                registerAsync.execute("");
                break;

            case R.id.verifier:
                String twitterPin = pin.getText().toString();
                if (!twitterPin.trim().isEmpty()) {
                    registerAsync = new Registration(this);
                    registerAsync.execute(twitterPin);
                } else {
                    Toast.makeText(this, R.string.enter_pin, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    public void connect(String link) {
        ConnectivityManager mConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnect != null && mConnect.getActiveNetworkInfo() != null) {
            if (mConnect.getActiveNetworkInfo().isConnected()) {
                Intent browser = new Intent(Intent.ACTION_VIEW);
                browser.setData(Uri.parse(link));
                startActivity(browser);
            }
        } else {
            Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_SHORT).show();
        }
    }
}