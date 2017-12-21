package org.nuclearfog.twidda.Window;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import org.nuclearfog.twidda.R;

public class Settings extends AppCompatActivity {

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInst) {
        super.onCreate(savedInst);
        settings = getApplicationContext().getSharedPreferences("settings", 0);
        setContentView(R.layout.settings);
        Toolbar tool = (Toolbar) findViewById(R.id.toolbar_setting);
        setSupportActionBar(tool);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    /**
     * Create Actionbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.buttons, m);
        m.findItem(R.id.action_profile).setVisible(false);
        m.findItem(R.id.action_settings).setVisible(false);
        m.findItem(R.id.action_tweet).setVisible(false);

        return true;
    }

}
