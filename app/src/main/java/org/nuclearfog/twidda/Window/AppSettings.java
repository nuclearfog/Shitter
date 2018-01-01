package org.nuclearfog.twidda.Window;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;

import org.nuclearfog.twidda.R;

public class AppSettings extends AppCompatActivity {


    private Button delButon;
    private Switch toggleImg;
    private EditText woeId;
    private SharedPreferences settings;
    private NumberPicker load_factor;
    private Editor edit;

    @Override
    protected void onCreate(Bundle savedInst) {
        super.onCreate(savedInst);
        setContentView(R.layout.settings);

        settings = getApplicationContext().getSharedPreferences("settings", 0);
        int location = settings.getInt("woeid",23424829);
        edit  = settings.edit();

        Toolbar tool = (Toolbar) findViewById(R.id.toolbar_setting);
        load_factor = (NumberPicker)findViewById(R.id.tweet_load);
        delButon = (Button) findViewById(R.id.delete_db);
        toggleImg = (Switch) findViewById(R.id.toggleImg);
        woeId = (EditText) findViewById(R.id.woeid);

        setSupportActionBar(tool);
        load_factor.setMinValue(5);
        load_factor.setMaxValue(100);
        toggleImg.setChecked(settings.getBoolean("image_load",false));
        woeId.setText(""+location);

        setListener();
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
        switch(item.getItemId()) {
            case R.id.back_settings:
                finish();
                return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        edit.putInt("woeid", Integer.valueOf(woeId.getText().toString()));
        edit.putInt("preload", load_factor.getValue());
        edit.apply();
        super.onDestroy();
    }



    private void setListener() {

        toggleImg.setOnCheckedChangeListener(
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton b, boolean checked) {
                    edit.putBoolean("image_load", checked);
                    edit.apply();
                }
            });


        delButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getApplicationContext().deleteDatabase(getApplicationContext().getString(R.string.database));
            }
        });

    }
}