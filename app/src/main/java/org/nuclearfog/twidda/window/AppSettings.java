package org.nuclearfog.twidda.window;

import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.nuclearfog.twidda.R;

/**
 * App Settings Page
 * @see ColorPreferences
 */
public class AppSettings extends AppCompatActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, AlertDialog.OnClickListener {

    private EditText woeId;
    private SharedPreferences settings;
    private ClipboardManager clip;
    private TextView load_factor;
    private ColorPreferences mColor;
    private int row, wId;

    @Override
    protected void onCreate(Bundle savedInst) {
        super.onCreate(savedInst);
        setContentView(R.layout.settings);
        mColor = ColorPreferences.getInstance(this);
        settings = getApplicationContext().getSharedPreferences("settings", 0);
        clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        row = settings.getInt("preload",10);
        wId = settings.getInt("woeid",23424829);
        String location = Integer.toString(wId);
        String load = Integer.toString(row);

        Button delButon = (Button) findViewById(R.id.delete_db);
        CheckBox toggleImg = (CheckBox) findViewById(R.id.toggleImg);
        Button colorButton1 = (Button) findViewById(R.id.color_background);
        Button colorButton2 = (Button) findViewById(R.id.color_font);
        Button colorButton3 = (Button) findViewById(R.id.color_tweet);
        Button colorButton4 = (Button) findViewById(R.id.highlight_color);
        Button reduce = (Button) findViewById(R.id.less);
        Button enhance = (Button) findViewById(R.id.more);
        Button clip = (Button) findViewById(R.id.woeid_clip);
        load_factor = (TextView)findViewById(R.id.number_row);
        woeId = (EditText) findViewById(R.id.woeid);

        delButon.setOnClickListener(this);
        colorButton1.setOnClickListener(this);
        colorButton2.setOnClickListener(this);
        colorButton3.setOnClickListener(this);
        colorButton4.setOnClickListener(this);
        toggleImg.setOnCheckedChangeListener(this);
        reduce.setOnClickListener(this);
        enhance.setOnClickListener(this);
        clip.setOnClickListener(this);

        int color1 = mColor.getColor(ColorPreferences.BACKGROUND);
        int color2 = mColor.getColor(ColorPreferences.FONT_COLOR);
        int color3 = mColor.getColor(ColorPreferences.TWEET_COLOR);
        int color4 = mColor.getColor(ColorPreferences.HIGHLIGHTING);
        colorButton1.setBackgroundColor(color1);
        colorButton2.setBackgroundColor(color2);
        colorButton3.setBackgroundColor(color3);
        colorButton4.setBackgroundColor(color4);

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
        wId = Integer.parseInt(woeId.getText().toString());
        Editor edit  = settings.edit();
        edit.putInt("woeid", wId);
        edit.putInt("preload", row);
        edit.apply();
        mColor.commit();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.delete_db:
               new AlertDialog.Builder(this)
                .setMessage("Datenbank löschen?")
                .setPositiveButton(R.string.yes_confirm, this)
                .setNegativeButton(R.string.no_confirm, this)
                .show();
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
            case R.id.highlight_color:
                mColor.setColor(ColorPreferences.HIGHLIGHTING);
                break;
            case R.id.less:
                if(row > 10) {
                    row -= 10;
                    load_factor.setText(Integer.toString(row));
                }
                break;
            case R.id.more:
                if(row < 100) {
                    row += 10;
                    load_factor.setText(Integer.toString(row));
                }
                break;
            case R.id.woeid_clip:
                if(clip != null && clip.hasPrimaryClip()) {
                    String text = clip.getPrimaryClip().getItemAt(0).getText().toString();
                    if(text.matches("\\d++\n?")) {
                        woeId.setText(text);
                        Toast.makeText(getApplicationContext(),"Eingefügt!",Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"Falsches Format!",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(DialogInterface d, int id) {
        switch(id) {
            case DialogInterface.BUTTON_POSITIVE:
                deleteDatabase("database.db");
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton b, boolean checked) {
        settings.edit().putBoolean("image_load", checked).apply();
    }
}