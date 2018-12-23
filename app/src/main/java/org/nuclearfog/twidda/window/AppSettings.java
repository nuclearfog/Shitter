package org.nuclearfog.twidda.window;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.WorldIdAdapter;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * App settings page
 *
 * @see GlobalSettings
 */
public class AppSettings extends AppCompatActivity implements OnClickListener,
        OnColorChangedListener, OnItemSelectedListener, OnCheckedChangeListener {

    private GlobalSettings settings;
    private Button colorButton1, colorButton2, colorButton3, colorButton4;
    private CheckBox toggleImg;
    private EditText woeIdText;
    private Spinner woeId;
    private View root;
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
        toggleImg.setOnCheckedChangeListener(this);
        colorButton1.setOnClickListener(this);
        colorButton2.setOnClickListener(this);
        colorButton3.setOnClickListener(this);
        colorButton4.setOnClickListener(this);
        woeId.setOnItemSelectedListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        toggleImg.setChecked(settings.loadImages());
        woeId.setAdapter(new WorldIdAdapter(this));
        woeId.setSelection(settings.getWoeIdSelection());
        colorButton1.setBackgroundColor(settings.getBackgroundColor());
        colorButton2.setBackgroundColor(settings.getFontColor());
        colorButton3.setBackgroundColor(settings.getTweetColor());
        colorButton4.setBackgroundColor(settings.getHighlightColor());

        if (settings.customWoeIdset()) {
            String text = Long.toString(settings.getWoeId());
            woeIdText.setVisibility(View.VISIBLE);
            woeIdText.setText(text);
        }
    }


    @Override
    public void onBackPressed() {
        if (settings.customWoeIdset()) {
            String woeText = woeIdText.getText().toString();
            if (!woeText.isEmpty())
                settings.setWoeId(Long.parseLong(woeText));
            else
                settings.setWoeId(1);
        }
        super.onBackPressed();
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
                        .setMessage(R.string.confirm_log_lout)
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
                setColor(settings.getBackgroundColor());
                mode = 0;
                break;

            case R.id.color_font:
                setColor(settings.getFontColor());
                mode = 1;
                break;

            case R.id.color_tweet:
                setColor(settings.getTweetColor());
                mode = 2;
                break;

            case R.id.highlight_color:
                setColor(settings.getHighlightColor());
                mode = 3;
                break;

            case R.id.load_dialog:
                Dialog load_popup = new Dialog(this);
                final NumberPicker load = new NumberPicker(this);
                load.setMaxValue(100);
                load.setMinValue(10);
                load.setValue(settings.getRowLimit());
                load.setWrapSelectorWheel(false);
                load_popup.setContentView(load);
                load_popup.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (settings.getRowLimit() != load.getValue()) {
                            settings.setRowLimit(load.getValue());
                        }
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
                root.setBackgroundColor(color);
                settings.setBackgroundColor(color);
                colorButton1.setBackgroundColor(color);
                break;
            case 1:
                settings.setFontColor(color);
                colorButton2.setBackgroundColor(color);
                break;
            case 2:
                settings.setTweetColor(color);
                colorButton3.setBackgroundColor(color);
                break;
            case 3:
                settings.setHighlightColor(color);
                colorButton4.setBackgroundColor(color);
                break;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == parent.getCount() - 1) {
            woeIdText.setVisibility(View.VISIBLE);
            settings.setCustomWoeId(true);
            settings.setWoeId(1);
        } else {
            woeIdText.setVisibility(View.INVISIBLE);
            woeIdText.setText("");
            settings.setCustomWoeId(false);
            settings.setWoeId(id);
        }
        settings.setWoeIdSelection(position);
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        woeId.setSelection(settings.getWoeIdSelection());
    }


    @Override
    public void onCheckedChanged(CompoundButton c, boolean checked) {
        settings.setImageLoad(checked);
    }


    private void setColor(int preColor) {
        Dialog d = ColorPickerDialogBuilder.with(this)
                .showAlphaSlider(false).initialColor(preColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).density(20)
                .setOnColorChangedListener(this).build();
        d.show();
    }
}