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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FontAdapter;
import org.nuclearfog.twidda.adapter.LocationAdapter;
import org.nuclearfog.twidda.backend.LocationLoader;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.model.TrendLocation;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.AccountDatabase;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.dialog.ConfirmDialog;
import org.nuclearfog.twidda.dialog.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.dialog.InfoDialog;

import java.util.List;
import java.util.regex.Matcher;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.nuclearfog.twidda.activity.MainActivity.RETURN_APP_LOGOUT;
import static org.nuclearfog.twidda.activity.MainActivity.RETURN_DB_CLEARED;
import static org.nuclearfog.twidda.dialog.ConfirmDialog.DialogType;

/**
 * Settings Activity class.
 *
 * @author nuclearfog
 */
public class AppSettings extends AppCompatActivity implements OnClickListener, OnDismissListener, OnSeekBarChangeListener,
        OnCheckedChangeListener, OnItemSelectedListener, OnConfirmListener, OnColorChangedListener {

    private GlobalSettings settings;
    private LocationLoader locationAsync;
    private LocationAdapter locationAdapter;
    private FontAdapter fontAdapter;

    private Dialog connectDialog, databaseDialog, logoutDialog, color_dialog_selector, appInfo;
    private View root, layout_hq_image, layout_key, layout_proxy, layout_auth_en, layout_auth;
    private EditText proxyAddr, proxyPort, proxyUser, proxyPass, api_key1, api_key2;
    private CompoundButton enableProxy, enableAuth, hqImage, enableAPI;
    private Spinner locationSpinner;
    private TextView list_size;
    private Button[] colorButtons = new Button[ColorMode.values().length];

    private ColorMode mode;
    private int color = 0;

    private enum ColorMode {
        BACKGROUND(0),
        FONTCOLOR(1),
        POPUPCOLOR(2),
        HIGHLIGHT(3),
        CARDCOLOR(4),
        ICONCOLOR(5),
        RETWEETCOLOR(6),
        FAVORITECOLOR(7),
        FOLLOWPENING(8),
        FOLLOWCOLOR(9);

        final int INDEX;

        ColorMode(int idx) {
            this.INDEX = idx;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_settings);
        Button delButton = findViewById(R.id.delete_db);
        Button logout = findViewById(R.id.logout);
        Toolbar toolbar = findViewById(R.id.toolbar_setting);
        View trend_card = findViewById(R.id.settings_trend_card);
        View user_card = findViewById(R.id.settings_data_card);
        CompoundButton toggleImg = findViewById(R.id.toggleImg);
        CompoundButton toggleAns = findViewById(R.id.toggleAns);
        CompoundButton toolbarOverlap = findViewById(R.id.settings_toolbar_ov);
        CompoundButton enablePreview = findViewById(R.id.settings_enable_prev);
        SeekBar listSizeSelector = findViewById(R.id.settings_list_seek);
        Spinner fontSpinner = findViewById(R.id.spinner_font);
        enableProxy = findViewById(R.id.settings_enable_proxy);
        enableAuth = findViewById(R.id.settings_enable_auth);
        hqImage = findViewById(R.id.settings_image_hq);
        enableAPI = findViewById(R.id.settings_set_custom_keys);
        locationSpinner = findViewById(R.id.spinner_woeid);
        colorButtons[ColorMode.BACKGROUND.INDEX] = findViewById(R.id.color_background);
        colorButtons[ColorMode.FONTCOLOR.INDEX] = findViewById(R.id.color_font);
        colorButtons[ColorMode.POPUPCOLOR.INDEX] = findViewById(R.id.color_popup);
        colorButtons[ColorMode.HIGHLIGHT.INDEX] = findViewById(R.id.highlight_color);
        colorButtons[ColorMode.CARDCOLOR.INDEX] = findViewById(R.id.color_card);
        colorButtons[ColorMode.ICONCOLOR.INDEX] = findViewById(R.id.color_icon);
        colorButtons[ColorMode.RETWEETCOLOR.INDEX] = findViewById(R.id.color_rt);
        colorButtons[ColorMode.FAVORITECOLOR.INDEX] = findViewById(R.id.color_fav);
        colorButtons[ColorMode.FOLLOWPENING.INDEX] = findViewById(R.id.color_f_req);
        colorButtons[ColorMode.FOLLOWCOLOR.INDEX] = findViewById(R.id.color_follow);
        proxyAddr = findViewById(R.id.edit_proxy_address);
        proxyPort = findViewById(R.id.edit_proxy_port);
        proxyUser = findViewById(R.id.edit_proxyuser);
        proxyPass = findViewById(R.id.edit_proxypass);
        api_key1 = findViewById(R.id.settings_custom_key1);
        api_key2 = findViewById(R.id.settings_custom_key2);
        list_size = findViewById(R.id.settings_list_size);
        layout_proxy = findViewById(R.id.settings_layout_proxy);
        layout_hq_image = findViewById(R.id.settings_image_hq_layout);
        layout_auth_en = findViewById(R.id.settings_layout_auth_enable);
        layout_auth = findViewById(R.id.settings_layout_proxy_auth);
        layout_key = findViewById(R.id.settings_layout_key);
        root = findViewById(R.id.settings_layout);

        toolbar.setTitle(R.string.title_settings);
        setSupportActionBar(toolbar);

        settings = GlobalSettings.getInstance(this);
        locationAdapter = new LocationAdapter(settings);
        locationAdapter.addTop(settings.getTrendLocation());
        locationSpinner.setAdapter(locationAdapter);
        locationSpinner.setSelected(false);
        fontAdapter = new FontAdapter(settings);
        fontSpinner.setAdapter(fontAdapter);
        fontSpinner.setSelection(settings.getFontIndex(), false);
        fontSpinner.setSelected(false);

        AppStyles.setTheme(settings, root);

        if (!settings.isLoggedIn()) {
            trend_card.setVisibility(GONE);
            user_card.setVisibility(GONE);
        }
        if (!settings.isProxyEnabled()) {
            layout_proxy.setVisibility(GONE);
            layout_auth_en.setVisibility(GONE);
        }
        if (!settings.imagesEnabled()) {
            layout_hq_image.setVisibility(GONE);
        }
        if (!settings.isProxyAuthSet()) {
            layout_auth.setVisibility(GONE);
        }
        if (!settings.isCustomApiSet()) {
            layout_key.setVisibility(GONE);
        }
        toggleImg.setChecked(settings.imagesEnabled());
        toggleAns.setChecked(settings.replyLoadingEnabled());
        enableAPI.setChecked(settings.isCustomApiSet());
        enablePreview.setChecked(settings.linkPreviewEnabled());
        toolbarOverlap.setChecked(settings.toolbarOverlapEnabled());
        proxyAddr.setText(settings.getProxyHost());
        proxyPort.setText(settings.getProxyPort());
        proxyUser.setText(settings.getProxyUser());
        proxyPass.setText(settings.getProxyPass());
        api_key1.setText(settings.getConsumerKey());
        api_key2.setText(settings.getConsumerSecret());
        list_size.setText(Integer.toString(settings.getListSize()));
        listSizeSelector.setProgress(settings.getListSize() / 10 - 1);
        enableProxy.setChecked(settings.isProxyEnabled());
        enableAuth.setChecked(settings.isProxyAuthSet());
        hqImage.setEnabled(settings.imagesEnabled());
        hqImage.setChecked(settings.getImageQuality());
        setButtonColors();

        connectDialog = new ConfirmDialog(this, DialogType.WRONG_PROXY, this);
        connectDialog = new ConfirmDialog(this, DialogType.WRONG_PROXY, this);
        databaseDialog = new ConfirmDialog(this, DialogType.DEL_DATABASE, this);
        logoutDialog = new ConfirmDialog(this, DialogType.APP_LOG_OUT, this);
        appInfo = new InfoDialog(this);

        for (Button button : colorButtons)
            button.setOnClickListener(this);
        logout.setOnClickListener(this);
        delButton.setOnClickListener(this);
        toggleImg.setOnCheckedChangeListener(this);
        toggleAns.setOnCheckedChangeListener(this);
        enableAPI.setOnCheckedChangeListener(this);
        enablePreview.setOnCheckedChangeListener(this);
        enableProxy.setOnCheckedChangeListener(this);
        enableAuth.setOnCheckedChangeListener(this);
        hqImage.setOnCheckedChangeListener(this);
        toolbarOverlap.setOnCheckedChangeListener(this);
        fontSpinner.setOnItemSelectedListener(this);
        listSizeSelector.setOnSeekBarChangeListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (settings.isLoggedIn() && locationAsync == null) {
            locationAsync = new LocationLoader(this);
            locationAsync.execute();
        }
    }


    @Override
    public void onBackPressed() {
        if (saveConnectionSettings()) {
            TwitterEngine.resetTwitter();
            super.onBackPressed();
        } else {
            if (!connectDialog.isShowing()) {
                connectDialog.show();
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
        AppStyles.setMenuIconColor(m, settings.getIconColor());
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings_info) {
            if (!appInfo.isShowing()) {
                appInfo.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConfirm(DialogType type) {
        // confirm log out
        if (type == DialogType.APP_LOG_OUT) {
            // reset twitter singleton
            TwitterEngine.resetTwitter();
            // remove account from database
            AccountDatabase.getInstance(this).removeLogin(settings.getCurrentUserId());
            settings.logout();
            setResult(RETURN_APP_LOGOUT);
            finish();
        }
        // confirm delete database
        else if (type == DialogType.DEL_DATABASE) {
            DatabaseAdapter.deleteDatabase(getApplicationContext());
            setResult(RETURN_DB_CLEARED);
        }
        // confirm leaving without saving proxy changes
        else if (type == DialogType.WRONG_PROXY) {
            // exit without saving proxy settings
            finish();
        }
    }


    @Override
    public void onClick(View v) {
        // delete database
        if (v.getId() == R.id.delete_db) {
            if (!databaseDialog.isShowing()) {
                databaseDialog.show();
            }
        }
        // logout from twitter
        else if (v.getId() == R.id.logout) {
            if (!logoutDialog.isShowing()) {
                logoutDialog.show();
            }
        }
        // set background color
        else if (v.getId() == R.id.color_background) {
            mode = ColorMode.BACKGROUND;
            color = settings.getBackgroundColor();
            setColor(color, false);
        }
        // set font color
        else if (v.getId() == R.id.color_font) {
            mode = ColorMode.FONTCOLOR;
            color = settings.getFontColor();
            setColor(color, false);
        }
        // set popup color
        else if (v.getId() == R.id.color_popup) {
            mode = ColorMode.POPUPCOLOR;
            color = settings.getPopupColor();
            setColor(color, false);
        }
        // set highlight color
        else if (v.getId() == R.id.highlight_color) {
            mode = ColorMode.HIGHLIGHT;
            color = settings.getHighlightColor();
            setColor(color, false);
        }
        // set card color
        else if (v.getId() == R.id.color_card) {
            mode = ColorMode.CARDCOLOR;
            color = settings.getCardColor();
            setColor(color, true);
        }
        // set icon color
        else if (v.getId() == R.id.color_icon) {
            mode = ColorMode.ICONCOLOR;
            color = settings.getIconColor();
            setColor(color, false);
        }
        // set retweet icon color
        else if (v.getId() == R.id.color_rt) {
            mode = ColorMode.RETWEETCOLOR;
            color = settings.getRetweetIconColor();
            setColor(color, false);
        }
        // set favorite icon color
        else if (v.getId() == R.id.color_fav) {
            mode = ColorMode.FAVORITECOLOR;
            color = settings.getFavoriteIconColor();
            setColor(color, false);
        }
        // set follow icon color
        else if (v.getId() == R.id.color_f_req) {
            mode = ColorMode.FOLLOWPENING;
            color = settings.getFollowPendingColor();
            setColor(color, false);
        }
        // set follow icon color
        else if (v.getId() == R.id.color_follow) {
            mode = ColorMode.FOLLOWCOLOR;
            color = settings.getFollowIconColor();
            setColor(color, false);
        }
    }


    @Override
    public void onDismiss(DialogInterface d) {
        if (d == color_dialog_selector) {
            switch (mode) {
                case BACKGROUND:
                    settings.setBackgroundColor(color);
                    fontAdapter.notifyDataSetChanged();
                    if (settings.isLoggedIn()) {
                        locationAdapter.notifyDataSetChanged();
                    }
                    AppStyles.setTheme(settings, root);
                    setButtonColors();
                    break;

                case FONTCOLOR:
                    settings.setFontColor(color);
                    fontAdapter.notifyDataSetChanged();
                    if (settings.isLoggedIn()) {
                        locationAdapter.notifyDataSetChanged();
                    }
                    AppStyles.setTheme(settings, root);
                    setButtonColors();
                    break;

                case POPUPCOLOR:
                    settings.setPopupColor(color);
                    AppStyles.setColorButton(colorButtons[mode.INDEX], color);
                    break;

                case HIGHLIGHT:
                    settings.setHighlightColor(color);
                    AppStyles.setColorButton(colorButtons[mode.INDEX], color);
                    break;

                case CARDCOLOR:
                    settings.setCardColor(color);
                    fontAdapter.notifyDataSetChanged();
                    if (settings.isLoggedIn()) {
                        locationAdapter.notifyDataSetChanged();
                    }
                    AppStyles.setTheme(settings, root);
                    setButtonColors();
                    break;

                case ICONCOLOR:
                    settings.setIconColor(color);
                    invalidateOptionsMenu();
                    AppStyles.setTheme(settings, root);
                    setButtonColors();
                    break;

                case RETWEETCOLOR:
                    settings.setRetweetIconColor(color);
                    AppStyles.setColorButton(colorButtons[mode.INDEX], color);
                    break;

                case FAVORITECOLOR:
                    settings.setFavoriteIconColor(color);
                    AppStyles.setColorButton(colorButtons[mode.INDEX], color);
                    break;

                case FOLLOWPENING:
                    settings.setFollowPendingColor(color);
                    AppStyles.setColorButton(colorButtons[mode.INDEX], color);
                    break;

                case FOLLOWCOLOR:
                    settings.setFollowIconColor(color);
                    AppStyles.setColorButton(colorButtons[mode.INDEX], color);
                    break;
            }
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton c, boolean checked) {
        // toggle image loading
        if (c.getId() == R.id.toggleImg) {
            settings.setImageLoad(checked);
            hqImage.setEnabled(checked);
            if (checked) {
                layout_hq_image.setVisibility(VISIBLE);
            } else {
                layout_hq_image.setVisibility(GONE);
            }
        }
        // toggle automatic answer load
        else if (c.getId() == R.id.toggleAns) {
            settings.setAnswerLoad(checked);
        }
        // enable high quality images
        else if (c.getId() == R.id.settings_image_hq) {
            settings.setHighQualityImage(checked);
        }
        // enable toolbar overlap
        else if (c.getId() == R.id.settings_toolbar_ov) {
            settings.setToolbarOverlap(checked);
        }
        // enable link preview
        else if (c.getId() == R.id.settings_enable_prev) {
            settings.setLinkPreview(checked);
        }
        // enable proxy settings
        else if (c.getId() == R.id.settings_enable_proxy) {
            if (checked) {
                layout_proxy.setVisibility(VISIBLE);
                layout_auth_en.setVisibility(VISIBLE);
            } else {
                layout_proxy.setVisibility(GONE);
                layout_auth_en.setVisibility(GONE);
                enableAuth.setChecked(false);
            }
        }
        //enable proxy authentication
        else if (c.getId() == R.id.settings_enable_auth) {
            if (checked) {
                layout_auth.setVisibility(VISIBLE);
            } else {
                layout_auth.setVisibility(GONE);
            }
        }
        // enable custom API setup
        else if (c.getId() == R.id.settings_set_custom_keys) {
            if (checked) {
                layout_key.setVisibility(VISIBLE);
            } else {
                layout_key.setVisibility(GONE);
            }
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Trend location spinner
        if (parent.getId() == R.id.spinner_woeid) {
            settings.setTrendLocation(locationAdapter.getItem(position));
        }
        // Font type spinner
        else if (parent.getId() == R.id.spinner_font) {
            settings.setFontIndex(position);
            AppStyles.setViewFont(settings, root);
            if (settings.isLoggedIn()) {
                locationAdapter.notifyDataSetChanged();
            }
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }


    @Override
    public void onColorChanged(int i) {
        color = i;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        String text = Integer.toString((progress + 1) * 10);
        list_size.setText(text);
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        settings.setListSize((seekBar.getProgress() + 1) * 10);
    }

    /**
     * set location information from twitter
     *
     * @param data location data
     */
    public void setLocationData(List<TrendLocation> data) {
        locationAdapter.setData(data);
        int position = locationAdapter.getPosition(settings.getTrendLocation());
        if (position > 0) {
            locationSpinner.setSelection(position, false);
        }
        locationSpinner.setOnItemSelectedListener(this);
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
     * @param preColor    preselected color
     * @param enableAlpha true to enable alpha slider
     */
    private void setColor(int preColor, boolean enableAlpha) {
        if (color_dialog_selector == null || !color_dialog_selector.isShowing()) {
            color_dialog_selector = ColorPickerDialogBuilder.with(this)
                    .showAlphaSlider(enableAlpha).initialColor(preColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                    .setOnColorChangedListener(this).density(15).build();
            color_dialog_selector.setOnDismissListener(this);
            color_dialog_selector.show();
        }
    }

    /**
     * setup all color buttons color
     */
    private void setButtonColors() {
        int[] colors = settings.getAllColors();
        for (int i = 0; i < colorButtons.length; i++) {
            AppStyles.setColorButton(colorButtons[i], colors[i]);
        }
    }

    /**
     * check proxy settings and save them if they are correct
     *
     * @return true if settings are saved successfully
     */
    private boolean saveConnectionSettings() {
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
        if (enableAPI.isChecked()) {
            if (api_key1.length() > 0 && api_key2.length() > 0) {
                String key1 = api_key1.getText().toString();
                String key2 = api_key2.getText().toString();
                settings.setCustomAPI(key1, key2);
            } else {
                checkPassed = false;
            }
        } else {
            settings.removeCustomAPI();
        }
        return checkPassed;
    }
}