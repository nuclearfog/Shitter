package org.nuclearfog.twidda.window;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.LocationLoader;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.items.TrendLocation;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.GONE;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.MainActivity.APP_LOGOUT;
import static org.nuclearfog.twidda.MainActivity.DB_CLEARED;


public class AppSettings extends AppCompatActivity implements OnClickListener,
        OnDismissListener, OnCheckedChangeListener {

    private static final int BACKGROUND = 0;
    private static final int FONTCOLOR = 1;
    private static final int HIGHLIGHT = 2;
    private static final int POPUPCOLOR = 3;

    private GlobalSettings settings;
    private ConnectivityManager mConnect;
    private LocationLoader locationAsync;
    private Button colorButton1, colorButton2, colorButton3, colorButton4;
    private EditText proxyAddr, proxyPort, proxyUser, proxyPass;
    private CheckBox toggleImg, toggleAns;
    private ArrayAdapter<TrendLocation> adapter;
    private Spinner woeId;
    private View root;

    private int color = 0;
    private int mode = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_settings);
        Button delButton = findViewById(R.id.delete_db);
        Button load_popup = findViewById(R.id.load_dialog);
        Button logout = findViewById(R.id.logout);
        View login_layout = findViewById(R.id.Login_options);
        colorButton1 = findViewById(R.id.color_background);
        colorButton2 = findViewById(R.id.color_font);
        colorButton3 = findViewById(R.id.color_popup);
        colorButton4 = findViewById(R.id.highlight_color);
        proxyAddr = findViewById(R.id.edit_proxyadress);
        proxyPort = findViewById(R.id.edit_proxyport);
        proxyUser = findViewById(R.id.edit_proxyuser);
        proxyPass = findViewById(R.id.edit_proxypass);
        toggleImg = findViewById(R.id.toggleImg);
        toggleAns = findViewById(R.id.toggleAns);
        woeId = findViewById(R.id.woeid);
        root = findViewById(R.id.settings_layout);
        Toolbar toolbar = findViewById(R.id.toolbar_setting);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.settings);

        mConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        settings = GlobalSettings.getInstance(this);
        if (!settings.getLogin())
            login_layout.setVisibility(GONE);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        adapter.add(settings.getTrendLocation());
        woeId.setAdapter(adapter);

        logout.setOnClickListener(this);
        load_popup.setOnClickListener(this);
        delButton.setOnClickListener(this);
        colorButton1.setOnClickListener(this);
        colorButton2.setOnClickListener(this);
        colorButton3.setOnClickListener(this);
        colorButton4.setOnClickListener(this);
        toggleImg.setOnCheckedChangeListener(this);
        toggleAns.setOnCheckedChangeListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        toggleImg.setChecked(settings.getImageLoad());
        toggleAns.setChecked(settings.getAnswerLoad());
        root.setBackgroundColor(settings.getBackgroundColor());
        colorButton1.setBackgroundColor(settings.getBackgroundColor());
        colorButton2.setBackgroundColor(settings.getFontColor());
        colorButton3.setBackgroundColor(settings.getPopupColor());
        colorButton4.setBackgroundColor(settings.getHighlightColor());
        colorButton1.setTextColor(settings.getBackgroundColor() ^ 0xffffff);
        colorButton2.setTextColor(settings.getFontColor() ^ 0xffffff);
        colorButton3.setTextColor(settings.getPopupColor() ^ 0xffffff);
        colorButton4.setTextColor(settings.getHighlightColor() ^ 0xffffff);
        proxyAddr.setText(settings.getProxyHost());
        proxyPort.setText(settings.getProxyPort());
        proxyUser.setText(settings.getProxyUser());
        proxyPass.setText(settings.getProxyPass());

        if (settings.getLogin() && adapter.getCount() <= 1) {
            locationAsync = new LocationLoader(this);
            locationAsync.execute();
        }
    }


    @Override
    public void onBackPressed() {
        if (validateInputs()) {
            settings.setProxyServer(proxyAddr.getText().toString(), proxyPort.getText().toString());
            settings.setProxyLogin(proxyUser.getText().toString(), proxyPass.getText().toString());
            settings.configureProxy();
            TrendLocation location = (TrendLocation) woeId.getSelectedItem();
            settings.setTrendLocation(location);
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
            if (mConnect.getActiveNetworkInfo() != null && mConnect.getActiveNetworkInfo().isConnected()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String link = "https://github.com/nuclearfog/Shitter";
                intent.setData(Uri.parse(link));
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.connection_failed, LENGTH_SHORT).show();
            }
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
                                DatabaseAdapter.deleteDatabase(getApplicationContext());
                                setResult(DB_CLEARED);
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
                                DatabaseAdapter.deleteDatabase(getApplicationContext());
                                setResult(APP_LOGOUT);
                                finish();
                            }
                        })
                        .show();
                break;

            case R.id.color_background:
                mode = BACKGROUND;
                color = settings.getBackgroundColor();
                setColor(color);
                break;

            case R.id.color_font:
                mode = FONTCOLOR;
                color = settings.getFontColor();
                setColor(color);
                break;

            case R.id.color_popup:
                mode = POPUPCOLOR;
                color = settings.getPopupColor();
                setColor(color);
                break;

            case R.id.highlight_color:
                mode = HIGHLIGHT;
                color = settings.getHighlightColor();
                setColor(color);
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
    public void onDismiss(DialogInterface d) {
        switch (mode) {
            case BACKGROUND:
                root.setBackgroundColor(color);
                settings.setBackgroundColor(color);
                colorButton1.setBackgroundColor(color);
                colorButton1.setTextColor(color ^ 0xffffff);
                break;

            case FONTCOLOR:
                settings.setFontColor(color);
                colorButton2.setBackgroundColor(color);
                colorButton2.setTextColor(color ^ 0xffffff);
                break;

            case POPUPCOLOR:
                settings.setPopupColor(color);
                colorButton3.setBackgroundColor(color);
                colorButton3.setTextColor(color ^ 0xffffff);
                break;

            case HIGHLIGHT:
                settings.setHighlightColor(color);
                colorButton4.setBackgroundColor(color);
                colorButton4.setTextColor(color ^ 0xffffff);
                break;
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


    private void setColor(int preColor) {
        Dialog d = ColorPickerDialogBuilder.with(this)
                .showAlphaSlider(false).initialColor(preColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).density(20)
                .setOnColorChangedListener(new OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int i) {
                        color = i;
                    }
                }).build();
        d.setOnDismissListener(this);
        d.show();
    }


    private boolean validateInputs() {
        boolean success = true;
        Editable editAddr = proxyAddr.getText();
        Editable editUser = proxyUser.getText();

        if (editAddr != null && !editAddr.toString().isEmpty()) {
            Editable editPort = proxyPort.getText();
            if (editPort == null || editPort.toString().isEmpty()) {
                String errMsg = getString(R.string.error_empty_port);
                proxyPort.setError(errMsg);
                success = false;
            }
        }
        if (editUser != null && !editUser.toString().isEmpty()) {
            Editable editPass = proxyPass.getText();
            if (editPass != null && editPass.toString().isEmpty()) {
                String errMsg = getString(R.string.error_empty_pass);
                proxyPass.setError(errMsg);
                success = false;
            }
        }
        return success;
    }
}