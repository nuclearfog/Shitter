package org.nuclearfog.twidda.window;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.ColorPreferences;

public class AppSettings extends AppCompatActivity implements View.OnClickListener{

    private Button delButon,save_woeid, colorButton1, colorButton2;
    private int backgroundColor, fontColor;
    private Switch toggleImg;
    private EditText woeId;
    private SharedPreferences settings;
    private NumberPicker load_factor;
    private ColorPreferences mColor;
    private Editor edit;
    private boolean modified = false;
    private boolean imgldr;

    @Override
    protected void onCreate(Bundle savedInst) {
        super.onCreate(savedInst);
        setContentView(R.layout.settings);
        mColor = ColorPreferences.getInstance(this);
        settings = getApplicationContext().getSharedPreferences("settings", 0);
        int location = settings.getInt("woeid",23424829);
        edit  = settings.edit();


        load_factor = (NumberPicker)findViewById(R.id.tweet_load);
        delButon = (Button) findViewById(R.id.delete_db);
        toggleImg = (Switch) findViewById(R.id.toggleImg);
        woeId = (EditText) findViewById(R.id.woeid);
        colorButton1 = (Button) findViewById(R.id.color_background);
        colorButton2 = (Button) findViewById(R.id.color_font);
        save_woeid = (Button) findViewById(R.id.save_woeid);

        delButon.setOnClickListener(this);
        colorButton1.setOnClickListener(this);
        colorButton2.setOnClickListener(this);
        save_woeid.setOnClickListener(this);

        load_factor.setMinValue(5);
        load_factor.setMaxValue(100);
        toggleImg.setChecked(settings.getBoolean("image_load",false));
        load_factor.setValue(settings.getInt("preload",10));
        woeId.setText(""+location);

        loadSettings();
        setListener();
    }

    /**
     * Create Actionbar
     */
    @Override
    public boolean onCreateOptionsMenu( Menu m ) {
        getMenuInflater().inflate(R.menu.setting, m);
        return true;
    }

    /**
     * Actionbar selection
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
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
        edit.putBoolean("image_load", imgldr);
        edit.apply();
        mColor.commit();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.delete_db:
                deleteDatabase(getString(R.string.database));
                break;
            case R.id.color_background:
                mColor.setColor(ColorPreferences.BACKGROUND);
                break;
            case R.id.color_font:
                mColor.setColor(ColorPreferences.FONT_COLOR);
                break;
        }
    }

    private void setListener() {
        toggleImg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                imgldr = checked;
                modified = true;
            }
        });
    }

    private void loadSettings() {
        backgroundColor = settings.getInt("background",10);
        fontColor = settings.getInt("fontColor",10);
    }

}