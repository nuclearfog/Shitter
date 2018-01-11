package org.nuclearfog.twidda.window;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.ColorPreferences;

/**
 * App Settings Page
 * @see ColorPreferences
 */
public class AppSettings extends AppCompatActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private EditText woeId;
    private SharedPreferences settings;
    private TextView load_factor;
    private ColorPreferences mColor;
    private boolean imgldr;
    private int row, wId;

    @Override
    protected void onCreate(Bundle savedInst) {
        super.onCreate(savedInst);
        setContentView(R.layout.settings);
        mColor = ColorPreferences.getInstance(this);
        settings = getApplicationContext().getSharedPreferences("settings", 0);
        row = settings.getInt("preload",10);
        wId = settings.getInt("woeid",23424829);
        String location = Integer.toString(wId);
        String load = Integer.toString(row);

        Button delButon = (Button) findViewById(R.id.delete_db);
        CheckBox toggleImg = (CheckBox) findViewById(R.id.toggleImg);
        Button colorButton1 = (Button) findViewById(R.id.color_background);
        Button colorButton2 = (Button) findViewById(R.id.color_font);
        Button colorButton3 = (Button) findViewById(R.id.color_tweet);
        Button reduce = (Button) findViewById(R.id.less);
        Button enhance = (Button) findViewById(R.id.more);
        load_factor = (TextView)findViewById(R.id.number_row);
        woeId = (EditText) findViewById(R.id.woeid);

        delButon.setOnClickListener(this);
        colorButton1.setOnClickListener(this);
        colorButton2.setOnClickListener(this);
        colorButton3.setOnClickListener(this);
        toggleImg.setOnCheckedChangeListener(this);
        reduce.setOnClickListener(this);
        enhance.setOnClickListener(this);

        int color1 = mColor.getColor(ColorPreferences.BACKGROUND);
        int color2 = mColor.getColor(ColorPreferences.TWEET_COLOR);
        int color3 = mColor.getColor(ColorPreferences.FONT_COLOR);
        String color1Str = "#"+Integer.toHexString(color1);
        String color2Str = "#"+Integer.toHexString(color2);
        String color3Str = "#"+Integer.toHexString(color3);

        colorButton1.setBackgroundColor(color1);
        colorButton2.setBackgroundColor(color2);
        colorButton3.setBackgroundColor(color3);
        colorButton1.setText(color1Str);
        colorButton2.setText(color2Str);
        colorButton3.setText(color3Str);

        toggleImg.setChecked(settings.getBoolean("image_load",false));

        load_factor.setText(load);
        woeId.setText(location);
    }

    @Override
    public boolean onCreateOptionsMenu( Menu m ) {
        getMenuInflater().inflate(R.menu.setting, m);
        return true;
    }

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
        Editor edit  = settings.edit();
        edit.putInt("woeid", wId);
        edit.putInt("preload", row);
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
            case R.id.color_tweet:
                mColor.setColor(ColorPreferences.TWEET_COLOR);
                break;
            case R.id.less:
                if(row > 5)
                    row -= 5;
                load_factor.setText(Integer.toString(row));
                break;
            case R.id.more:
                if(row < 200)
                    row += 5;
                load_factor.setText(Integer.toString(row));
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton b, boolean checked) {
        imgldr = checked;
    }
}