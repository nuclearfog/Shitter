package org.nuclearfog.twidda.window;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.RegisterAccount;


public class LoginPage extends Activity implements View.OnClickListener {

    private EditText pin;

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.login);
        pin = findViewById(R.id.pin);
        findViewById(R.id.linkButton).setOnClickListener(this);
        findViewById(R.id.get).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }


    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.linkButton:
                RegisterAccount account = new RegisterAccount(this);
                account.execute("");
                break;

            case R.id.get:
                String twitterPin = pin.getText().toString();
                if(!twitterPin.trim().isEmpty()) {
                    new RegisterAccount(this).execute(twitterPin);
                } else {
                    Toast.makeText(getApplicationContext(),"PIN eingeben!",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}