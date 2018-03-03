package org.nuclearfog.twidda.window;

import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
    private ColorPreferences mColor;
    private ClipboardManager clip;
    private TextView load_factor;
    private CheckBox toggleImg;
    private int row, wId;
    private boolean imgEnabled;

    @Override
    protected void onCreate(Bundle savedInst) {
        super.onCreate(savedInst);
        setContentView(R.layout.settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        Button delButon = (Button) findViewById(R.id.delete_db);
        Button colorButton1 = (Button) findViewById(R.id.color_background);
        Button colorButton2 = (Button) findViewById(R.id.color_font);
        Button colorButton3 = (Button) findViewById(R.id.color_tweet);
        Button colorButton4 = (Button) findViewById(R.id.highlight_color);
        Button reduce = (Button) findViewById(R.id.less);
        Button enhance = (Button) findViewById(R.id.more);
        Button clipButton = (Button) findViewById(R.id.woeid_clip);
        toggleImg = (CheckBox) findViewById(R.id.toggleImg);
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
        clipButton.setOnClickListener(this);

        mColor = ColorPreferences.getInstance(this);
        colorButton1.setBackgroundColor(mColor.getColor(ColorPreferences.BACKGROUND));
        colorButton2.setBackgroundColor(mColor.getColor(ColorPreferences.FONT_COLOR));
        colorButton3.setBackgroundColor(mColor.getColor(ColorPreferences.TWEET_COLOR));
        colorButton4.setBackgroundColor( mColor.getColor(ColorPreferences.HIGHLIGHTING));

        loadSettings();
    }

    /**
     * Home Button
     */
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onCreateOptionsMenu( Menu m ) {
        getMenuInflater().inflate(R.menu.setting, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch(item.getItemId()) {
            case R.id.save_settings:
                save();
                return true;
            case R.id.back_settings:
                finish();
                return true;
            default:return false;
        }
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
        imgEnabled = true;
    }

    private void loadSettings() {
        settings = getApplicationContext().getSharedPreferences("settings", 0);
        clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        row = settings.getInt("preload",10);
        wId = settings.getInt("woeid",23424829);
        imgEnabled = settings.getBoolean("image_load",true);

        String location = Integer.toString(wId);
        woeId.setText(location);
        toggleImg.setChecked(imgEnabled);
        String load = Integer.toString(row);
        load_factor.setText(load);
    }

    private void save() {
        wId = Integer.parseInt(woeId.getText().toString());
        Editor edit  = settings.edit();
        edit.putInt("woeid", wId);
        edit.putInt("preload", row);
        edit.putBoolean("image_load", imgEnabled);
        edit.apply();
        mColor.commit();
        Toast.makeText(getApplicationContext(), "Gespeichert", Toast.LENGTH_SHORT).show();
    }
}