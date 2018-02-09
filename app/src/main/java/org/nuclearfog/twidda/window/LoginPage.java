package org.nuclearfog.twidda.window;

import android.app.Activity;
import android.content.ClipboardManager;
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
        findViewById(R.id.clipboard).setOnClickListener(this);
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

            case R.id.clipboard:
                ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if(clip != null && clip.hasPrimaryClip()) {
                    String text = clip.getPrimaryClip().getItemAt(0).getText().toString();
                    if(text.matches("\\d++\n?")) {
                        pin.setText(text);
                        Toast.makeText(getApplicationContext(),"Eingefügt!",Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"Falsches Format!",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
}