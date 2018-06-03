package org.nuclearfog.twidda.window;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.ErrorLog;
import org.nuclearfog.twidda.viewadapter.LogAdapter;

import java.util.List;

/**
 * App Settings Activity
 */
public class AppSettings extends AppCompatActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, OnColorChangedListener {

    private EditText woeId;
    private SharedPreferences settings;
    private ClipboardManager clip;
    private CheckBox toggleImg;
    private Button colorButton1,colorButton2,colorButton3,colorButton4;
    private int background,tweet,font,highlight;
    private boolean imgEnabled;
    private int row, wId;
    private int mode = 0;

    @Override
    protected void onCreate(Bundle savedInst) {
        super.onCreate(savedInst);
        setContentView(R.layout.settingpage);
        Toolbar toolbar = findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        Button delButton = findViewById(R.id.delete_db);
        Button errorcall = findViewById(R.id.error_call);
        colorButton1 = findViewById(R.id.color_background);
        colorButton2 = findViewById(R.id.color_font);
        colorButton3 = findViewById(R.id.color_tweet);
        colorButton4 = findViewById(R.id.highlight_color);
        Button clipButton = findViewById(R.id.woeid_clip);
        Button load_popup = findViewById(R.id.load_dialog);
        toggleImg = findViewById(R.id.toggleImg);
        woeId = findViewById(R.id.woeid);
        load();

        load_popup.setOnClickListener(this);
        delButton.setOnClickListener(this);
        errorcall.setOnClickListener(this);
        colorButton1.setOnClickListener(this);
        colorButton2.setOnClickListener(this);
        colorButton3.setOnClickListener(this);
        colorButton4.setOnClickListener(this);
        toggleImg.setOnCheckedChangeListener(this);
        clipButton.setOnClickListener(this);

        colorButton1.setBackgroundColor(background);
        colorButton2.setBackgroundColor(font);
        colorButton3.setBackgroundColor(tweet);
        colorButton4.setBackgroundColor(highlight);
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
    @SuppressLint("InflateParams")
    public void onClick( View v ) {
        switch(v.getId()) {
            case R.id.delete_db:
               new AlertDialog.Builder(this)
                .setMessage("Datenbank löschen?")
                .setNegativeButton(R.string.no_confirm, null)
                .setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDatabase("database.db");
                    }
                })
                .show();
                break;
            case R.id.error_call:
                List<String> messages = new ErrorLog(this).getErrorList();
                LogAdapter adp = new LogAdapter(messages);
                RecyclerView loglist = new RecyclerView(this);
                loglist.setLayoutManager(new LinearLayoutManager(this));
                loglist.setAdapter(adp);
                Dialog pList = new Dialog(this);
                pList.setContentView(loglist);
                pList.show();
                break;
            case R.id.color_background:
                setColor(background);
                mode = 0;
                break;
            case R.id.color_font:
                setColor(font);
                mode = 1;
                break;
            case R.id.color_tweet:
                setColor(tweet);
                mode = 2;
                break;
            case R.id.highlight_color:
                setColor(highlight);
                mode = 3;
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
            case R.id.load_dialog:
                Dialog load_popup = new Dialog(this);
                final NumberPicker load = new NumberPicker(this);
                load.setMaxValue(100);
                load.setMinValue(10);
                load.setValue(row);
                load.setWrapSelectorWheel(false);
                load_popup.setContentView(load);
                load_popup.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        row = load.getValue();
                    }
                });
                load_popup.show();
                break;
        }
    }


    @Override
    public void onColorChanged(int color) {
        switch(mode) {
            case 0:
                background = color;
                break;
            case 1:
                font = color;
                break;
            case 2:
                tweet = color;
                break;
            case 3:
                highlight = color;
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton b, boolean checked) {
        imgEnabled = checked;
    }


    public void setColor(int preColor) {
        Dialog d = ColorPickerDialogBuilder.with(this)
                .showAlphaSlider(false).initialColor(preColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).density(20)
                .setOnColorChangedListener(this).build();
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                colorButton1.setBackgroundColor(background);
                colorButton2.setBackgroundColor(font);
                colorButton3.setBackgroundColor(tweet);
                colorButton4.setBackgroundColor(highlight);
            }
        });
        d.show();
    }

    private void load() {
        settings = getSharedPreferences("settings",0);
        background = settings.getInt("background_color",0xff0f114a);
        font = settings.getInt("font_color",0xffffffff);
        tweet = settings.getInt("tweet_color",0xff19aae8);
        highlight = settings.getInt("highlight_color",0xffff00ff);
        row = settings.getInt("preload",20);
        wId = settings.getInt("woeid",23424829);
        imgEnabled = settings.getBoolean("image_load",true);
        String location = Integer.toString(wId);
        woeId.setText(location);
        toggleImg.setChecked(imgEnabled);
    }

    private void save() {
        wId = Integer.parseInt(woeId.getText().toString());
        Editor edit  = settings.edit();
        edit.putInt("woeid", wId);
        edit.putInt("preload", row);
        edit.putBoolean("image_load", imgEnabled);
        edit.putInt("background_color",background);
        edit.putInt("font_color",font);
        edit.putInt("tweet_color", tweet);
        edit.putInt("highlight_color", highlight);
        edit.apply();
        Toast.makeText(this, "Gespeichert", Toast.LENGTH_SHORT).show();
    }
}