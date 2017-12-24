package org.nuclearfog.twidda.Window;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

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

        Switch toggleImg = (Switch) findViewById(R.id.toggleImg);
        toggleImg.setChecked(settings.getBoolean("image_load",false));
        toggleImg.setOnCheckedChangeListener(
            new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked){
                SharedPreferences.Editor e = settings.edit();
                e.putBoolean("image_load", checked);
                e.apply();
            }
        });

        EditText locationField = (EditText) findViewById(R.id.location_edit);
        locationField.setText(settings.getString("location",""));

    }

    /**
     * Create Actionbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.setting, m);
        return true;
    }

    /**
     * Actionbar selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.back_settings:
                finish();
                break;
        }
        return true;
    }
}
