package org.nuclearfog.twidda.window;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
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
        CompoundButton.OnCheckedChangeListener, AlertDialog.OnClickListener, Dialog.OnDismissListener, OnColorChangedListener {

    private EditText woeId;
    private SharedPreferences settings;
    private ClipboardManager clip;
    private Button colorButton1, colorButton2,colorButton3,colorButton4;
    private TextView load_factor;
    private CheckBox toggleImg;
    private Dialog d;
    private int row, wId;
    private int background, tweet, font, highlight;
    private int mode = 0;
    private boolean imgEnabled;

    @Override
    protected void onCreate(Bundle savedInst) {
        super.onCreate(savedInst);
        setContentView(R.layout.settingpage);
        Toolbar toolbar = findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        Button delButton = findViewById(R.id.delete_db);
        Button errorcall = findViewById(R.id.error_call);
        colorButton1 = findViewById(R.id.color_background);
        colorButton2 = findViewById(R.id.color_font);
        colorButton3 = findViewById(R.id.color_tweet);
        colorButton4 = findViewById(R.id.highlight_color);
        Button reduce = findViewById(R.id.less);
        Button enhance = findViewById(R.id.more);
        Button clipButton = findViewById(R.id.woeid_clip);
        toggleImg = findViewById(R.id.toggleImg);
        load_factor = findViewById(R.id.number_row);
        woeId = findViewById(R.id.woeid);

        delButton.setOnClickListener(this);
        errorcall.setOnClickListener(this);
        colorButton1.setOnClickListener(this);
        colorButton2.setOnClickListener(this);
        colorButton3.setOnClickListener(this);
        colorButton4.setOnClickListener(this);
        toggleImg.setOnCheckedChangeListener(this);
        reduce.setOnClickListener(this);
        enhance.setOnClickListener(this);
        clipButton.setOnClickListener(this);

        settings = getSharedPreferences("settings",0);
        background = settings.getInt("background_color",0xff0f114a);
        font = settings.getInt("font_color",0xffffffff);
        tweet = settings.getInt("tweet_color",0xff19aae8);
        highlight = settings.getInt("highlight_color",0xffff00ff);

        colorButton1.setBackgroundColor(background);
        colorButton2.setBackgroundColor(font);
        colorButton3.setBackgroundColor(tweet);
        colorButton4.setBackgroundColor(highlight);

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
    public void onClick( View v ) {
        switch(v.getId()) {
            case R.id.delete_db:
               new AlertDialog.Builder(this)
                .setMessage("Datenbank löschen?")
                .setPositiveButton(R.string.yes_confirm, this)
                .setNegativeButton(R.string.no_confirm, this)
                .show();
                break;
            case R.id.error_call:
                List<String> messages = new ErrorLog(this).getErrorList();
                LogAdapter adp = new LogAdapter(messages);
                View list = LayoutInflater.from(this).inflate(R.layout.errorpage,null,false);
                RecyclerView loglist = list.findViewById(R.id.log_list);
                loglist.setLayoutManager(new LinearLayoutManager(this));
                loglist.setAdapter(adp);
                Dialog pList = new Dialog(this);
                pList.setContentView(list);
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
            case R.id.less:
                if(row > 10) {
                    row -= 10;
                    String out1 = Integer.toString(row);
                    load_factor.setText(out1);
                }
                break;
            case R.id.more:
                if(row < 100) {
                    row += 10;
                    String out2 = Integer.toString(row);
                    load_factor.setText(out2);
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

    @Override
    public void onDismiss(DialogInterface i) {
        colorButton1.setBackgroundColor(background);
        colorButton2.setBackgroundColor(font);
        colorButton3.setBackgroundColor(tweet);
        colorButton4.setBackgroundColor(highlight);
        d.dismiss();
    }

    public void setColor(int preColor) {
        d = ColorPickerDialogBuilder.with(this)
                .showAlphaSlider(false).initialColor(preColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).density(20)
                .setOnColorChangedListener(this).build();
        d.setOnDismissListener(this);
        d.show();
    }

    private void loadSettings() {
        settings = getSharedPreferences("settings", 0);
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
        edit.putInt("background_color",background);
        edit.putInt("font_color",font);
        edit.putInt("tweet_color", tweet);
        edit.putInt("highlight_color", highlight);
        edit.apply();
        Toast.makeText(this, "Gespeichert", Toast.LENGTH_SHORT).show();
    }
}