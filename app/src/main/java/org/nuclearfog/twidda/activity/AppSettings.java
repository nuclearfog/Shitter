package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.text.Editable;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FontAdapter;
import org.nuclearfog.twidda.adapter.LocationAdapter;
import org.nuclearfog.twidda.backend.LocationListLoader;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.backend.items.TrendLocation;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.List;
import java.util.regex.Matcher;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.GONE;
import static org.nuclearfog.twidda.activity.MainActivity.APP_LOGOUT;
import static org.nuclearfog.twidda.activity.MainActivity.DB_CLEARED;

public class AppSettings extends AppCompatActivity implements OnClickListener, OnDismissListener,
        OnCheckedChangeListener, OnItemSelectedListener {

    private static final int INVERTCOLOR = 0xffffff;
    private static final String[] PICKER_SELECT = {"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};

    private enum ColorMode {
        BACKGROUND,
        FONTCOLOR,
        HIGHLIGHT,
        POPUPCOLOR
    }

    private GlobalSettings settings;
    private LocationListLoader locationAsync;
    private Button colorButton1, colorButton2, colorButton3, colorButton4;
    private EditText proxyAddr, proxyPort, proxyUser, proxyPass;
    private NumberPicker load_picker;
    private Dialog load_dialog_selector, color_dialog_selector;
    private Spinner locationSpinner;
    private LocationAdapter locationAdapter;
    private View root, colorButton1_edge;

    private ColorMode mode;
    private int color;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_settings);
        Button delButton = findViewById(R.id.delete_db);
        Button load_popup = findViewById(R.id.load_dialog);
        Button logout = findViewById(R.id.logout);
        Toolbar toolbar = findViewById(R.id.toolbar_setting);
        View login_layout = findViewById(R.id.Login_options);
        CheckBox toggleImg = findViewById(R.id.toggleImg);
        CheckBox toggleAns = findViewById(R.id.toggleAns);
        Spinner fontSpinner = findViewById(R.id.spinner_font);
        locationSpinner = findViewById(R.id.spinner_woeid);
        colorButton1_edge = findViewById(R.id.color_background_edge);
        colorButton1 = findViewById(R.id.color_background);
        colorButton2 = findViewById(R.id.color_font);
        colorButton3 = findViewById(R.id.color_popup);
        colorButton4 = findViewById(R.id.highlight_color);
        proxyAddr = findViewById(R.id.edit_proxyadress);
        proxyPort = findViewById(R.id.edit_proxyport);
        proxyUser = findViewById(R.id.edit_proxyuser);
        proxyPass = findViewById(R.id.edit_proxypass);
        root = findViewById(R.id.settings_layout);
        load_picker = new NumberPicker(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.settings);

        settings = GlobalSettings.getInstance(this);
        if (!settings.getLogin())
            login_layout.setVisibility(GONE);

        locationAdapter = new LocationAdapter(settings);
        locationAdapter.addTop(settings.getTrendLocation());
        locationSpinner.setAdapter(locationAdapter);
        FontAdapter fontAdapter = new FontAdapter();
        fontSpinner.setAdapter(fontAdapter);
        fontSpinner.setSelection(settings.getFont());
        load_picker.setMinValue(1);
        load_picker.setMaxValue(10);
        load_picker.setDisplayedValues(PICKER_SELECT);
        load_picker.setWrapSelectorWheel(false);
        load_picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        load_dialog_selector = new Dialog(this);
        load_dialog_selector.setContentView(load_picker);

        FontTool.setViewFontAndColor(settings, root);
        toggleImg.setChecked(settings.getImageLoad());
        toggleAns.setChecked(settings.getAnswerLoad());
        root.setBackgroundColor(settings.getBackgroundColor());
        colorButton1_edge.setBackgroundColor(settings.getBackgroundColor() ^ INVERTCOLOR);
        colorButton1.setBackgroundColor(settings.getBackgroundColor());
        colorButton2.setBackgroundColor(settings.getFontColor());
        colorButton3.setBackgroundColor(settings.getPopupColor());
        colorButton4.setBackgroundColor(settings.getHighlightColor());
        colorButton1.setTextColor(settings.getBackgroundColor() ^ INVERTCOLOR);
        colorButton2.setTextColor(settings.getFontColor() ^ INVERTCOLOR);
        colorButton3.setTextColor(settings.getPopupColor() ^ INVERTCOLOR);
        colorButton4.setTextColor(settings.getHighlightColor() ^ INVERTCOLOR);
        proxyAddr.setText(settings.getProxyHost());
        proxyPort.setText(settings.getProxyPort());
        proxyUser.setText(settings.getProxyUser());
        proxyPass.setText(settings.getProxyPass());
        load_picker.setValue((settings.getRowLimit()) / 10);

        logout.setOnClickListener(this);
        load_popup.setOnClickListener(this);
        delButton.setOnClickListener(this);
        colorButton1.setOnClickListener(this);
        colorButton2.setOnClickListener(this);
        colorButton3.setOnClickListener(this);
        colorButton4.setOnClickListener(this);
        toggleImg.setOnCheckedChangeListener(this);
        toggleAns.setOnCheckedChangeListener(this);
        fontSpinner.setOnItemSelectedListener(this);
        locationSpinner.setOnItemSelectedListener(this);
        load_dialog_selector.setOnDismissListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (settings.getLogin() && locationAdapter.getCount() <= 1) {
            locationAsync = new LocationListLoader(this);
            locationAsync.execute();
        }
    }


    @Override
    public void onBackPressed() {
        if (validateInputs()) {
            settings.setProxyServer(proxyAddr.getText().toString(), proxyPort.getText().toString());
            settings.setProxyLogin(proxyUser.getText().toString(), proxyPass.getText().toString());
            TwitterEngine.resetTwitter();
            super.onBackPressed();
        }
    }


    @Override
    protected void onDestroy() {
        if (locationAsync != null && locationAsync.getStatus() == RUNNING)
            locationAsync.cancel(true);
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.settings, m);
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings_info) {
            Dialog dialog = new Dialog(this, R.style.AppInfoDialog);
            dialog.setContentView(R.layout.popup_app_info);
            String versionName = " V" + BuildConfig.VERSION_NAME;
            TextView appInfo = dialog.findViewById(R.id.settings_app_info);
            appInfo.setLinkTextColor(settings.getHighlightColor());
            appInfo.append(versionName);
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_db:
                new Builder(this, R.style.ConfirmDialog)
                        .setMessage(R.string.confirm_delete_database)
                        .setNegativeButton(R.string.confirm_no, null)
                        .setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseAdapter.deleteDatabase(getApplicationContext());
                                setResult(DB_CLEARED);
                            }
                        })
                        .show();
                break;

            case R.id.logout:
                new Builder(this, R.style.ConfirmDialog)
                        .setMessage(R.string.confirm_log_lout)
                        .setNegativeButton(R.string.confirm_no, null)
                        .setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                settings.logout();
                                TwitterEngine.resetTwitter();
                                DatabaseAdapter.deleteDatabase(getApplicationContext());
                                setResult(APP_LOGOUT);
                                finish();
                            }
                        })
                        .show();
                break;

            case R.id.color_background:
                mode = ColorMode.BACKGROUND;
                color = settings.getBackgroundColor();
                setColor(color);
                break;

            case R.id.color_font:
                mode = ColorMode.FONTCOLOR;
                color = settings.getFontColor();
                setColor(color);
                break;

            case R.id.color_popup:
                mode = ColorMode.POPUPCOLOR;
                color = settings.getPopupColor();
                setColor(color);
                break;

            case R.id.highlight_color:
                mode = ColorMode.HIGHLIGHT;
                color = settings.getHighlightColor();
                setColor(color);
                break;

            case R.id.load_dialog:
                load_dialog_selector.show();
                break;
        }
    }


    @Override
    public void onDismiss(DialogInterface d) {
        if (d == color_dialog_selector) {
            switch (mode) {
                case BACKGROUND:
                    root.setBackgroundColor(color);
                    settings.setBackgroundColor(color);
                    colorButton1.setBackgroundColor(color);
                    colorButton1_edge.setBackgroundColor(color ^ INVERTCOLOR);
                    colorButton1.setTextColor(color ^ INVERTCOLOR);
                    break;

                case FONTCOLOR:
                    settings.setFontColor(color);
                    FontTool.setViewFontAndColor(settings, root);
                    colorButton2.setBackgroundColor(color);
                    colorButton2.setTextColor(color ^ INVERTCOLOR);
                    break;

                case POPUPCOLOR:
                    settings.setPopupColor(color);
                    colorButton3.setBackgroundColor(color);
                    colorButton3.setTextColor(color ^ INVERTCOLOR);
                    break;

                case HIGHLIGHT:
                    settings.setHighlightColor(color);
                    colorButton4.setBackgroundColor(color);
                    colorButton4.setTextColor(color ^ INVERTCOLOR);
                    break;
            }
        } else if (d == load_dialog_selector) {
            int selection = load_picker.getValue() * 10;
            if (settings.getRowLimit() != selection) {
                settings.setRowLimit(selection);
            }
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton c, boolean checked) {
        switch (c.getId()) {
            case R.id.toggleImg:
                settings.setImageLoad(checked);
                break;

            case R.id.toggleAns:
                settings.setAnswerLoad(checked);
                break;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getAdapter() instanceof LocationAdapter) {
            settings.setTrendLocation(locationAdapter.getItem(position));
        } else if (parent.getAdapter() instanceof FontAdapter) {
            settings.setFont(position);
            FontTool.setViewFont(settings, root);
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }


    public void setLocationData(List<TrendLocation> data) {
        locationAdapter.setData(data);
        int position = locationAdapter.getPosition(settings.getTrendLocation());
        locationSpinner.setSelection(position);
    }


    private void setColor(int preColor) {
        color_dialog_selector = ColorPickerDialogBuilder.with(this)
                .showAlphaSlider(false).initialColor(preColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).density(20)
                .setOnColorChangedListener(new OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int i) {
                        color = i;
                    }
                }).build();
        color_dialog_selector.setOnDismissListener(this);
        color_dialog_selector.show();
    }


    private boolean validateInputs() {
        boolean success = true;
        Editable editAddr = proxyAddr.getText();
        Editable editPort = proxyPort.getText();
        Editable editUser = proxyUser.getText();
        Editable editPass = proxyPass.getText();

        if (editAddr != null && editAddr.length() > 0) {
            Matcher ipMatch = Patterns.IP_ADDRESS.matcher(editAddr);
            if (!ipMatch.matches()) {
                String errMsg = getString(R.string.error_wrong_ip);
                proxyAddr.setError(errMsg);
                success = false;
            }
            if (editPort == null || editPort.length() == 0) {
                String errMsg = getString(R.string.error_empty_port);
                proxyPort.setError(errMsg);
                success = false;
            }
        }
        if (editUser != null && editUser.length() > 0) {
            if (editPass != null && editPass.length() == 0) {
                String errMsg = getString(R.string.error_empty_pass);
                proxyPass.setError(errMsg);
                success = false;
            }
        }
        return success;
    }
}