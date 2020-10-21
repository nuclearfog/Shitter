package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.items.TrendLocation;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.FontTool;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.List;
import java.util.regex.Matcher;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.nuclearfog.twidda.activity.MainActivity.RETURN_APP_LOGOUT;
import static org.nuclearfog.twidda.activity.MainActivity.RETURN_DB_CLEARED;

public class AppSettings extends AppCompatActivity implements OnClickListener, OnDismissListener,
        OnCheckedChangeListener, OnItemSelectedListener, DialogInterface.OnClickListener, OnColorChangedListener {

    private static final int INVERTCOLOR = 0xffffff;
    private static final String[] PICKER_SELECT = {"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};

    private enum ColorMode {
        BACKGROUND,
        FONTCOLOR,
        HIGHLIGHT,
        POPUPCOLOR,
        NONE
    }

    private GlobalSettings settings;
    private LocationListLoader locationAsync;
    private Dialog load_dialog_selector, proxyDialog, databaseDialog, logoutDialog, color_dialog_selector;
    private Button colorButton1, colorButton2, colorButton3, colorButton4;
    private EditText proxyAddr, proxyPort, proxyUser, proxyPass;
    private NumberPicker load_picker;
    private CompoundButton enableProxy, enableAuth;
    private Spinner locationSpinner;
    private LocationAdapter locationAdapter;
    private View root, colorButton1_edge;

    private ColorMode mode = ColorMode.NONE;
    private int color = 0;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_settings);
        Button delButton = findViewById(R.id.delete_db);
        Button load_popup = findViewById(R.id.load_dialog);
        Button logout = findViewById(R.id.logout);
        Toolbar toolbar = findViewById(R.id.toolbar_setting);
        View login_layout = findViewById(R.id.Login_options);
        CompoundButton toggleImg = findViewById(R.id.toggleImg);
        CompoundButton toggleAns = findViewById(R.id.toggleAns);
        Spinner fontSpinner = findViewById(R.id.spinner_font);
        enableProxy = findViewById(R.id.settings_enable_proxy);
        enableAuth = findViewById(R.id.settings_enable_auth);
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

        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);

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
        load_picker.setValue((settings.getListSize()) / 10);
        enableProxy.setChecked(settings.isProxyEnabled());
        enableAuth.setChecked(settings.isProxyAuthSet());
        setProxySetupVisibility(settings.isProxyEnabled(), settings.isProxyAuthSet());

        logout.setOnClickListener(this);
        load_popup.setOnClickListener(this);
        delButton.setOnClickListener(this);
        colorButton1.setOnClickListener(this);
        colorButton2.setOnClickListener(this);
        colorButton3.setOnClickListener(this);
        colorButton4.setOnClickListener(this);
        toggleImg.setOnCheckedChangeListener(this);
        toggleAns.setOnCheckedChangeListener(this);
        enableProxy.setOnCheckedChangeListener(this);
        enableAuth.setOnCheckedChangeListener(this);
        fontSpinner.setOnItemSelectedListener(this);
        locationSpinner.setOnItemSelectedListener(this);
        load_dialog_selector.setOnDismissListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (settings.getLogin() && locationAsync == null) {
            locationAsync = new LocationListLoader(this);
            locationAsync.execute();
        }
    }


    @Override
    public void onBackPressed() {
        if (saveProxySettings()) {
            TwitterEngine.resetTwitter();
            super.onBackPressed();
        } else {
            if (proxyDialog == null) {
                Builder builder = new Builder(this, R.style.ConfirmDialog);
                builder.setTitle(R.string.info_error);
                builder.setMessage(R.string.info_wrong_proxy_settings);
                builder.setPositiveButton(R.string.confirm_discard_proxy_changes, this);
                builder.setNegativeButton(android.R.string.cancel, null);
                proxyDialog = builder.create();
            }
            if (!proxyDialog.isShowing()) {
                proxyDialog.show();
            }
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings_info) {
            Dialog dialog = new Dialog(this, R.style.AppInfoDialog);
            dialog.setContentView(R.layout.dialog_app_info);
            String versionName = " V" + BuildConfig.VERSION_NAME;
            TextView appInfo = dialog.findViewById(R.id.settings_app_info);
            appInfo.setLinkTextColor(settings.getHighlightColor());
            appInfo.append(versionName);
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == BUTTON_POSITIVE) {
            if (dialog == proxyDialog) {
                // exit without saving proxy settings
                AppSettings.super.onBackPressed();
            } else if (dialog == databaseDialog) {
                DatabaseAdapter.deleteDatabase(getApplicationContext());
                setResult(RETURN_DB_CLEARED);
            } else if (dialog == logoutDialog) {
                settings.logout();
                TwitterEngine.resetTwitter();
                DatabaseAdapter.deleteDatabase(getApplicationContext());
                setResult(RETURN_APP_LOGOUT);
                finish();
            }
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_db:
                if (databaseDialog == null) {
                    Builder builder = new Builder(this, R.style.ConfirmDialog);
                    builder.setMessage(R.string.confirm_delete_database);
                    builder.setNegativeButton(R.string.confirm_no, null);
                    builder.setPositiveButton(R.string.confirm_yes, this);
                    databaseDialog = builder.create();
                }
                if (!databaseDialog.isShowing()) {
                    databaseDialog.show();
                }
                break;

            case R.id.logout:
                if (logoutDialog == null) {
                    Builder builder = new Builder(this, R.style.ConfirmDialog);
                    builder.setMessage(R.string.confirm_log_lout);
                    builder.setNegativeButton(R.string.confirm_no, null);
                    builder.setPositiveButton(R.string.confirm_yes, this);
                    logoutDialog = builder.create();
                }
                if (!logoutDialog.isShowing()) {
                    logoutDialog.show();
                }
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
                if (!load_dialog_selector.isShowing()) {
                    load_dialog_selector.show();
                }
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
            if (settings.getListSize() != selection) {
                settings.setListSize(selection);
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

            case R.id.settings_enable_proxy:
                setProxySetupVisibility(checked, checked & enableAuth.isChecked());
                break;

            case R.id.settings_enable_auth:
                setProxySetupVisibility(true, checked);
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


    @Override
    public void onColorChanged(int i) {
        color = i;
    }

    /**
     * set location information from twitter
     *
     * @param data locaion data
     */
    public void setLocationData(List<TrendLocation> data) {
        locationAdapter.setData(data);
        int position = locationAdapter.getPosition(settings.getTrendLocation());
        locationSpinner.setSelection(position);
    }

    /**
     * called when an error occurs
     *
     * @param err exception from twitter
     */
    public void onError(EngineException err) {
        ErrorHandler.handleFailure(this, err);
    }

    /**
     * show color picker dialog with preselected color
     *
     * @param preColor preselected color
     */
    private void setColor(int preColor) {
        if (color_dialog_selector == null) {
            color_dialog_selector = ColorPickerDialogBuilder.with(this)
                    .showAlphaSlider(false).initialColor(preColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                    .setOnColorChangedListener(this).density(15).build();
            color_dialog_selector.setOnDismissListener(this);
        }
        if (!color_dialog_selector.isShowing()) {
            color_dialog_selector.show();
        }
    }

    /**
     * set visibility of proxy layouts
     *
     * @param proxySetup visibility of proxy setup
     * @param proxyLogin visibility of proxy login
     */
    private void setProxySetupVisibility(boolean proxySetup, boolean proxyLogin) {
        int setupVisibility = proxySetup ? VISIBLE : GONE;
        int authVisibility = proxyLogin ? VISIBLE : GONE;
        proxyAddr.setVisibility(setupVisibility);
        proxyPort.setVisibility(setupVisibility);
        enableAuth.setVisibility(setupVisibility);
        proxyUser.setVisibility(authVisibility);
        proxyPass.setVisibility(authVisibility);
    }

    /**
     * check proxy settings and save them if they are correct
     *
     * @return true if settings are saved successfully
     */
    private boolean saveProxySettings() {
        boolean checkPassed = true;
        if (enableProxy.isChecked()) {
            checkPassed = proxyAddr.length() > 0 && proxyPort.length() > 0;
            if (checkPassed) {
                Matcher ipMatch = Patterns.IP_ADDRESS.matcher(proxyAddr.getText());
                checkPassed = ipMatch.matches();
            }
            if (checkPassed) {
                int port = 0;
                String portStr = proxyPort.getText().toString();
                if (!portStr.isEmpty()) {
                    port = Integer.parseInt(portStr);
                }
                checkPassed = port > 0 && port < 65536;
            }
            if (enableAuth.isChecked() && checkPassed) {
                checkPassed = proxyUser.length() > 0 && proxyPass.length() > 0;
            }
            if (checkPassed) {
                String proxyAddrStr = proxyAddr.getText().toString();
                String proxyPortStr = proxyPort.getText().toString();
                String proxyUserStr = proxyUser.getText().toString();
                String proxyPassStr = proxyPass.getText().toString();
                settings.setProxyServer(proxyAddrStr, proxyPortStr, proxyUserStr, proxyPassStr);
                settings.setProxyEnabled(true);
                settings.setProxyAuthSet(enableAuth.isChecked());
            }
        } else {
            settings.clearProxyServer();
        }
        return checkPassed;
    }
}