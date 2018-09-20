package org.nuclearfog.twidda.window;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.viewadapter.WorldIdAdapter;

/**
 * App settings page
 *
 * @see GlobalSettings
 */
public class AppSettings extends AppCompatActivity implements OnClickListener,
        OnColorChangedListener, OnItemSelectedListener {

    private GlobalSettings settings;
    private CheckBox toggleImg;
    private Button colorButton1, colorButton2, colorButton3, colorButton4;
    private Spinner woeId;
    private EditText woeIdText;
    private View root;
    private int background, tweet, font, highlight;
    private long wId;
    private int row;
    private int woeIdPos;
    private boolean customWoeId;
    private int mode = 0;


    @Override
    protected void onCreate(Bundle savedInst) {
        super.onCreate(savedInst);
        setContentView(R.layout.page_settings);
        Toolbar toolbar = findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.settings);

        settings = GlobalSettings.getInstance(this);

        Button delButton = findViewById(R.id.delete_db);
        Button load_popup = findViewById(R.id.load_dialog);
        Button logout = findViewById(R.id.logout);
        colorButton1 = findViewById(R.id.color_background);
        colorButton2 = findViewById(R.id.color_font);
        colorButton3 = findViewById(R.id.color_tweet);
        colorButton4 = findViewById(R.id.highlight_color);
        toggleImg = findViewById(R.id.toggleImg);
        woeIdText = findViewById(R.id.woe_id);
        woeId = findViewById(R.id.woeid);
        root = findViewById(R.id.settings_layout);
        root.setBackgroundColor(settings.getBackgroundColor());

        load_popup.setOnClickListener(this);
        delButton.setOnClickListener(this);
        logout.setOnClickListener(this);
        colorButton1.setOnClickListener(this);
        colorButton2.setOnClickListener(this);
        colorButton3.setOnClickListener(this);
        colorButton4.setOnClickListener(this);
        woeId.setOnItemSelectedListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        background = settings.getBackgroundColor();
        font = settings.getFontColor();
        tweet = settings.getTweetColor();
        highlight = settings.getHighlightColor();
        row = settings.getRowLimit();
        wId = settings.getWoeId();
        toggleImg.setChecked(settings.loadImages());
        woeIdPos = settings.getWoeIdSelection();
        customWoeId = settings.customWoeIdset();
        woeId.setAdapter(new WorldIdAdapter(this));

        woeId.setSelection(woeIdPos);
        colorButton1.setBackgroundColor(background);
        colorButton2.setBackgroundColor(font);
        colorButton3.setBackgroundColor(tweet);
        colorButton4.setBackgroundColor(highlight);
        if (customWoeId) {
            String text = Long.toString(wId);
            woeIdText.setVisibility(View.VISIBLE);
            woeIdText.setText(text);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.setting, m);
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back_settings:
                finish();
                break;

            case R.id.save_settings:
                settings.setBackgroundColor(background);
                settings.setHighlightColor(highlight);
                settings.setTweetColor(tweet);
                settings.setFontColor(font);
                settings.setImageLoad(toggleImg.isChecked());
                settings.setRowLimit(row);
                settings.setWoeIdSelection(woeIdPos);
                settings.setCustomWoeId(customWoeId);
                String woeText = woeIdText.getText().toString();
                if (customWoeId && !woeText.isEmpty())
                    wId = Long.parseLong(woeText);
                settings.setWoeId(wId);
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_db:
                new Builder(this)
                        .setMessage(R.string.delete_database_popup)
                        .setNegativeButton(R.string.no_confirm, null)
                        .setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteDatabase("database.db");
                            }
                        })
                        .show();
                break;

            case R.id.logout:
                new Builder(this)
                        .setMessage(R.string.should_log_lout)
                        .setNegativeButton(R.string.no_confirm, null)
                        .setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                settings.logout();
                                TwitterEngine.destroyInstance();
                                deleteDatabase("database.db");
                                finish();
                            }
                        })
                        .show();
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
        switch (mode) {
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
        if (position == parent.getCount() - 1) {
            woeIdText.setVisibility(View.VISIBLE);
            customWoeId = true;
            wId = 1;
        } else {
            woeIdText.setVisibility(View.INVISIBLE);
            woeIdText.setText("");
            customWoeId = false;
            wId = id;
        }
        woeIdPos = position;
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        woeId.setSelection(woeIdPos);
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
                root.setBackgroundColor(background);
            }
        });
        d.show();
    }
}