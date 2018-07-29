package org.nuclearfog.twidda.window;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.GlobalSettings;
import org.nuclearfog.twidda.database.ErrorLog;
import org.nuclearfog.twidda.viewadapter.LogAdapter;
import org.nuclearfog.twidda.viewadapter.WorldIdAdapter;

import java.util.List;


public class AppSettings extends AppCompatActivity implements OnClickListener,
        OnColorChangedListener, OnItemSelectedListener {

    private GlobalSettings settings;
    private CheckBox toggleImg;
    private Button colorButton1,colorButton2,colorButton3,colorButton4;
    private Spinner woeId;
    private int background,tweet,font,highlight;
    private long wId;
    private int row;
    private int woeIdPos;
    private int mode = 0;

    @Override
    protected void onCreate(Bundle savedInst) {
        super.onCreate(savedInst);
        setContentView(R.layout.settingpage);
        Toolbar toolbar = findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        settings = GlobalSettings.getInstance(this);

        Button delButton = findViewById(R.id.delete_db);
        Button errorCall = findViewById(R.id.error_call);
        Button load_popup = findViewById(R.id.load_dialog);
        colorButton1 = findViewById(R.id.color_background);
        colorButton2 = findViewById(R.id.color_font);
        colorButton3 = findViewById(R.id.color_tweet);
        colorButton4 = findViewById(R.id.highlight_color);
        toggleImg = findViewById(R.id.toggleImg);
        woeId = findViewById(R.id.woeid);
        load();

        woeId.setSelection(woeIdPos);
        colorButton1.setBackgroundColor(background);
        colorButton2.setBackgroundColor(font);
        colorButton3.setBackgroundColor(tweet);
        colorButton4.setBackgroundColor(highlight);

        load_popup.setOnClickListener(this);
        delButton.setOnClickListener(this);
        errorCall.setOnClickListener(this);
        colorButton1.setOnClickListener(this);
        colorButton2.setOnClickListener(this);
        colorButton3.setOnClickListener(this);
        colorButton4.setOnClickListener(this);
        woeId.setOnItemSelectedListener(this);
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
                RecyclerView logList = new RecyclerView(this);
                logList.setLayoutManager(new LinearLayoutManager(this));
                logList.setAdapter(adp);
                Dialog pList = new Dialog(this);
                pList.setContentView(logList);
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        wId = id;
        woeIdPos = position;
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent){}


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
        background = settings.getBackgroundColor();
        font = settings.getFontColor();
        tweet = settings.getTweetColor();
        highlight = settings.getHighlightColor();
        row = settings.getRowLimit();
        wId = settings.getWoeId();
        toggleImg.setChecked( settings.loadImages() );
        woeIdPos = settings.getWoeIdSelection();
        woeId.setAdapter( new WorldIdAdapter(this) );
    }

    private void save() {
        settings.setBackgroundColor(background);
        settings.setHighlightColor(highlight);
        settings.setTweetColor(tweet);
        settings.setFontColor(font);
        settings.setImageLoad( toggleImg.isChecked() );
        settings.setWoeId(wId);
        settings.setRowLimit(row);
        settings.setWoeIdSelection(woeIdPos);
        Toast.makeText(getApplicationContext(),"Gespeichert",Toast.LENGTH_SHORT).show();
    }
}