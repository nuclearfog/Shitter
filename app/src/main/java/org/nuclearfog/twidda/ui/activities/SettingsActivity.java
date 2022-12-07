package org.nuclearfog.twidda.ui.activities;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FontAdapter;
import org.nuclearfog.twidda.adapter.LocationAdapter;
import org.nuclearfog.twidda.adapter.ScaleAdapter;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.LocationLoader;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.AccountDatabase;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.InfoDialog;
import org.nuclearfog.twidda.ui.dialogs.LicenseDialog;

import java.util.List;
import java.util.regex.Matcher;

/**
 * Settings Activity class.
 *
 * @author nuclearfog
 */
public class SettingsActivity extends AppCompatActivity implements OnClickListener, OnDismissListener, OnSeekBarChangeListener,
		OnCheckedChangeListener, OnItemSelectedListener, OnConfirmListener, OnColorChangedListener {

	/**
	 * return code to recognize {@link MainActivity} that the current account was removed from login
	 */
	public static final int RETURN_APP_LOGOUT = 0x530;

	/**
	 * return code to recognize {@link MainActivity} that the database was removed
	 */
	public static final int RETURN_DATA_CLEARED = 0x955;

	/**
	 * total count of all colors defined
	 */
	private static final int COLOR_COUNT = 10;
	// app colors
	private static final int COLOR_BACKGROUND = 0;
	private static final int COLOR_TEXT = 1;
	private static final int COLOR_WINDOW = 2;
	private static final int COLOR_HIGHLIGHT = 3;
	private static final int COLOR_CARD = 4;
	private static final int COLOR_ICON = 5;
	private static final int COLOR_REPOST = 6;
	private static final int COLOR_FAVORITE = 7;
	private static final int COLOR_FOLLOW_REQUEST = 8;
	private static final int COLOR_FOLLOWING = 9;

	private GlobalSettings settings;

	private LocationLoader locationAsync;
	private LocationAdapter locationAdapter;
	private BaseAdapter fontAdapter, scaleAdapter;

	private Dialog color_dialog_selector, appInfo, license;
	private ConfirmDialog confirmDialog;

	private View enableAuthTxt;
	private EditText proxyAddr, proxyPort, proxyUser, proxyPass;
	private SwitchButton enableProxy, enableAuth;
	private Spinner locationSpinner;
	private TextView list_size;
	private ViewGroup root;
	private Button[] colorButtons = new Button[COLOR_COUNT];

	@IntRange(from = 0, to = COLOR_COUNT - 1)
	private int mode = 0;
	private int color = 0;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
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
		SwitchButton toggleImg = findViewById(R.id.toggleImg);
		SwitchButton toggleAns = findViewById(R.id.toggleAns);
		SwitchButton toolbarOverlap = findViewById(R.id.settings_toolbar_ov);
		SwitchButton enablePreview = findViewById(R.id.settings_enable_prev);
		SwitchButton enableLike = findViewById(R.id.enable_like);
		SwitchButton enableTwitterAlt = findViewById(R.id.settings_enable_twitter_alt);
		SwitchButton enableStatusIcons = findViewById(R.id.enable_status_indicators);
		SeekBar listSizeSelector = findViewById(R.id.settings_list_seek);
		Spinner fontSelector = findViewById(R.id.spinner_font);
		Spinner scaleSelector = findViewById(R.id.spinner_scale);
		enableProxy = findViewById(R.id.settings_enable_proxy);
		enableAuth = findViewById(R.id.settings_enable_auth);
		locationSpinner = findViewById(R.id.spinner_woeid);
		enableAuthTxt = findViewById(R.id.settings_enable_auth_descr);
		colorButtons[COLOR_BACKGROUND] = findViewById(R.id.color_background);
		colorButtons[COLOR_TEXT] = findViewById(R.id.color_text);
		colorButtons[COLOR_WINDOW] = findViewById(R.id.color_window);
		colorButtons[COLOR_HIGHLIGHT] = findViewById(R.id.highlight_color);
		colorButtons[COLOR_CARD] = findViewById(R.id.color_card);
		colorButtons[COLOR_ICON] = findViewById(R.id.color_icon);
		colorButtons[COLOR_REPOST] = findViewById(R.id.color_rt);
		colorButtons[COLOR_FAVORITE] = findViewById(R.id.color_fav);
		colorButtons[COLOR_FOLLOW_REQUEST] = findViewById(R.id.color_f_req);
		colorButtons[COLOR_FOLLOWING] = findViewById(R.id.color_follow);
		proxyAddr = findViewById(R.id.edit_proxy_address);
		proxyPort = findViewById(R.id.edit_proxy_port);
		proxyUser = findViewById(R.id.edit_proxyuser);
		proxyPass = findViewById(R.id.edit_proxypass);
		list_size = findViewById(R.id.settings_list_size);
		root = findViewById(R.id.settings_layout);

		toolbar.setTitle(R.string.title_settings);
		setSupportActionBar(toolbar);

		settings = GlobalSettings.getInstance(this);
		locationAdapter = new LocationAdapter(settings);
		locationAdapter.addItem(settings.getTrendLocation());
		locationSpinner.setAdapter(locationAdapter);
		locationSpinner.setSelected(false);
		fontAdapter = new FontAdapter(settings);
		scaleAdapter = new ScaleAdapter(settings);
		fontSelector.setAdapter(fontAdapter);
		scaleSelector.setAdapter(scaleAdapter);
		fontSelector.setSelection(settings.getFontIndex(), false);
		scaleSelector.setSelection(settings.getScaleIndex(), false);
		fontSelector.setSelected(false);
		scaleSelector.setSelected(false);

		AppStyles.setTheme(root, settings.getBackgroundColor());
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());

		confirmDialog = new ConfirmDialog(this);
		appInfo = new InfoDialog(this);
		license = new LicenseDialog(this);

		if (!settings.isLoggedIn()) {
			trend_card.setVisibility(GONE);
			user_card.setVisibility(GONE);
		}
		if (!settings.isProxyEnabled()) {
			proxyAddr.setVisibility(GONE);
			proxyPort.setVisibility(GONE);
			proxyUser.setVisibility(GONE);
			proxyPass.setVisibility(GONE);
			enableAuth.setVisibility(GONE);
			enableAuthTxt.setVisibility(GONE);
		} else if (!settings.isProxyAuthSet()) {
			proxyUser.setVisibility(GONE);
			proxyPass.setVisibility(GONE);
		}
		if (settings.likeEnabled()) {
			colorButtons[COLOR_FAVORITE].setText(R.string.settings_color_like);
		} else {
			colorButtons[COLOR_FAVORITE].setText(R.string.settings_color_fav);
		}
		toggleImg.setCheckedImmediately(settings.imagesEnabled());
		toggleAns.setCheckedImmediately(settings.replyLoadingEnabled());
		enablePreview.setCheckedImmediately(settings.linkPreviewEnabled());
		toolbarOverlap.setCheckedImmediately(settings.toolbarOverlapEnabled());
		enableLike.setCheckedImmediately(settings.likeEnabled());
		enableTwitterAlt.setCheckedImmediately(settings.twitterAltSet());
		enableStatusIcons.setCheckedImmediately(settings.statusIndicatorsEnabled());
		enableProxy.setCheckedImmediately(settings.isProxyEnabled());
		enableAuth.setCheckedImmediately(settings.isProxyAuthSet());
		proxyAddr.setText(settings.getProxyHost());
		proxyPort.setText(settings.getProxyPort());
		proxyUser.setText(settings.getProxyUser());
		proxyPass.setText(settings.getProxyPass());
		list_size.setText(Integer.toString(settings.getListSize()));
		listSizeSelector.setProgress(settings.getListSize() / 10 - 1);
		setButtonColors();

		for (Button button : colorButtons)
			button.setOnClickListener(this);
		logout.setOnClickListener(this);
		delButton.setOnClickListener(this);
		toggleImg.setOnCheckedChangeListener(this);
		toggleAns.setOnCheckedChangeListener(this);
		enableLike.setOnCheckedChangeListener(this);
		enableTwitterAlt.setOnCheckedChangeListener(this);
		enableStatusIcons.setOnCheckedChangeListener(this);
		enablePreview.setOnCheckedChangeListener(this);
		enableProxy.setOnCheckedChangeListener(this);
		enableAuth.setOnCheckedChangeListener(this);
		toolbarOverlap.setOnCheckedChangeListener(this);
		fontSelector.setOnItemSelectedListener(this);
		scaleSelector.setOnItemSelectedListener(this);
		listSizeSelector.setOnSeekBarChangeListener(this);
		confirmDialog.setConfirmListener(this);
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
			super.onBackPressed();
		} else {
			confirmDialog.show(ConfirmDialog.WRONG_PROXY);
		}
	}


	@Override
	protected void onDestroy() {
		if (locationAsync != null && locationAsync.getStatus() == RUNNING)
			locationAsync.cancel(true);
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.settings, m);
		AppStyles.setMenuIconColor(m, settings.getIconColor());
		return super.onCreateOptionsMenu(m);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.settings_info) {
			appInfo.show();
		} else if (item.getItemId() == R.id.settings_licenses) {
			license.show();
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onConfirm(int type, boolean rememberChoice) {
		// confirm log out
		if (type == ConfirmDialog.APP_LOG_OUT) {
			// remove account from database
			AccountDatabase accountDB = new AccountDatabase(this);
			accountDB.removeLogin(settings.getLogin().getId());
			settings.setLogin(null, true);
			setResult(RETURN_APP_LOGOUT);
			finish();
		}
		// confirm delete app data and cache
		else if (type == ConfirmDialog.DELETE_APP_DATA) {
			DatabaseAdapter.deleteDatabase(this);
			setResult(RETURN_DATA_CLEARED);
		}
		// confirm leaving without saving proxy changes
		else if (type == ConfirmDialog.WRONG_PROXY) {
			// exit without saving proxy settings
			finish();
		}
	}


	@Override
	public void onClick(View v) {
		// delete database
		if (v.getId() == R.id.delete_db) {
			confirmDialog.show(ConfirmDialog.DELETE_APP_DATA);
		}
		// logout from twitter
		else if (v.getId() == R.id.logout) {
			confirmDialog.show(ConfirmDialog.APP_LOG_OUT);
		}
		// set background color
		else if (v.getId() == R.id.color_background) {
			mode = COLOR_BACKGROUND;
			color = settings.getBackgroundColor();
			setColor(color, false);
		}
		// set font color
		else if (v.getId() == R.id.color_text) {
			mode = COLOR_TEXT;
			color = settings.getFontColor();
			setColor(color, false);
		}
		// set popup color
		else if (v.getId() == R.id.color_window) {
			mode = COLOR_WINDOW;
			color = settings.getPopupColor();
			setColor(color, false);
		}
		// set highlight color
		else if (v.getId() == R.id.highlight_color) {
			mode = COLOR_HIGHLIGHT;
			color = settings.getHighlightColor();
			setColor(color, false);
		}
		// set card color
		else if (v.getId() == R.id.color_card) {
			mode = COLOR_CARD;
			color = settings.getCardColor();
			setColor(color, true);
		}
		// set icon color
		else if (v.getId() == R.id.color_icon) {
			mode = COLOR_ICON;
			color = settings.getIconColor();
			setColor(color, false);
		}
		// set repost icon color
		else if (v.getId() == R.id.color_rt) {
			mode = COLOR_REPOST;
			color = settings.getRepostIconColor();
			setColor(color, false);
		}
		// set favorite icon color
		else if (v.getId() == R.id.color_fav) {
			mode = COLOR_FAVORITE;
			color = settings.getFavoriteIconColor();
			setColor(color, false);
		}
		// set follow icon color
		else if (v.getId() == R.id.color_f_req) {
			mode = COLOR_FOLLOW_REQUEST;
			color = settings.getFollowPendingColor();
			setColor(color, false);
		}
		// set follow icon color
		else if (v.getId() == R.id.color_follow) {
			mode = COLOR_FOLLOWING;
			color = settings.getFollowIconColor();
			setColor(color, false);
		}
	}


	@Override
	public void onDismiss(DialogInterface d) {
		if (d == color_dialog_selector) {
			switch (mode) {
				case COLOR_BACKGROUND:
					settings.setBackgroundColor(color);
					fontAdapter.notifyDataSetChanged();
					scaleAdapter.notifyDataSetChanged();
					if (settings.isLoggedIn()) {
						locationAdapter.notifyDataSetChanged();
					}
					AppStyles.setTheme(root, settings.getBackgroundColor());
					setButtonColors();
					break;

				case COLOR_TEXT:
					settings.setFontColor(color);
					fontAdapter.notifyDataSetChanged();
					scaleAdapter.notifyDataSetChanged();
					if (settings.isLoggedIn()) {
						locationAdapter.notifyDataSetChanged();
					}
					AppStyles.setTheme(root, settings.getBackgroundColor());
					setButtonColors();
					break;

				case COLOR_WINDOW:
					settings.setPopupColor(color);
					AppStyles.setColorButton(colorButtons[COLOR_WINDOW], color);
					break;

				case COLOR_HIGHLIGHT:
					settings.setHighlightColor(color);
					AppStyles.setColorButton(colorButtons[COLOR_HIGHLIGHT], color);
					break;

				case COLOR_CARD:
					settings.setCardColor(color);
					fontAdapter.notifyDataSetChanged();
					scaleAdapter.notifyDataSetChanged();
					if (settings.isLoggedIn()) {
						locationAdapter.notifyDataSetChanged();
					}
					AppStyles.setTheme(root, settings.getBackgroundColor());
					setButtonColors();
					break;

				case COLOR_ICON:
					settings.setIconColor(color);
					invalidateOptionsMenu();
					AppStyles.setTheme(root, settings.getBackgroundColor());
					setButtonColors();
					break;

				case COLOR_REPOST:
					settings.setRepostIconColor(color);
					AppStyles.setColorButton(colorButtons[COLOR_REPOST], color);
					break;

				case COLOR_FAVORITE:
					settings.setFavoriteIconColor(color);
					AppStyles.setColorButton(colorButtons[COLOR_FAVORITE], color);
					break;

				case COLOR_FOLLOW_REQUEST:
					settings.setFollowPendingColor(color);
					AppStyles.setColorButton(colorButtons[COLOR_FOLLOW_REQUEST], color);
					break;

				case COLOR_FOLLOWING:
					settings.setFollowIconColor(color);
					AppStyles.setColorButton(colorButtons[COLOR_FOLLOWING], color);
					break;
			}
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton c, boolean checked) {
		// toggle image loading
		if (c.getId() == R.id.toggleImg) {
			settings.setImageLoad(checked);
		}
		// toggle automatic answer load
		else if (c.getId() == R.id.toggleAns) {
			settings.setAnswerLoad(checked);
		}
		// enable toolbar overlap
		else if (c.getId() == R.id.settings_toolbar_ov) {
			settings.setToolbarOverlap(checked);
		}
		// enable like
		else if (c.getId() == R.id.enable_like) {
			settings.enableLike(checked);
			if (checked) {
				colorButtons[COLOR_FAVORITE].setText(R.string.settings_color_like);
			} else {
				colorButtons[COLOR_FAVORITE].setText(R.string.settings_color_fav);
			}
		}
		// enable alternative Twitter service
		else if (c.getId() == R.id.settings_enable_twitter_alt) {
			settings.setTwitterAlt(checked);
		}
		// enable status indicators
		else if (c.getId() == R.id.enable_status_indicators) {
			settings.enableStatusIndicators(checked);
		}
		// enable link preview
		else if (c.getId() == R.id.settings_enable_prev) {
			settings.setLinkPreview(checked);
		}
		// enable proxy settings
		else if (c.getId() == R.id.settings_enable_proxy) {
			if (checked) {
				proxyAddr.setVisibility(VISIBLE);
				proxyPort.setVisibility(VISIBLE);
				enableAuth.setVisibility(VISIBLE);
				enableAuthTxt.setVisibility(VISIBLE);
			} else {
				proxyAddr.setVisibility(GONE);
				proxyPort.setVisibility(GONE);
				enableAuthTxt.setVisibility(GONE);
				enableAuth.setVisibility(GONE);
				enableAuth.setChecked(false);
			}
		}
		//enable proxy authentication
		else if (c.getId() == R.id.settings_enable_auth) {
			if (checked) {
				proxyUser.setVisibility(VISIBLE);
				proxyPass.setVisibility(VISIBLE);
			} else {
				proxyUser.setVisibility(GONE);
				proxyPass.setVisibility(GONE);
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
			AppStyles.setFontStyle(root);
			if (settings.isLoggedIn()) {
				locationAdapter.notifyDataSetChanged();
			}
		}
		// Font scale spinner
		else if (parent.getId() == R.id.spinner_scale) {
			settings.setScaleIndex(position);
			AppStyles.setFontStyle(root);
			Toast.makeText(this, R.string.info_restart_app_on_change, Toast.LENGTH_SHORT).show();
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
	public void setLocationData(List<Location> data) {
		locationAdapter.replaceItems(data);
		int position = locationAdapter.indexOf(settings.getTrendLocation());
		if (position > 0)
			locationSpinner.setSelection(position, false);
		locationSpinner.setOnItemSelectedListener(this);
	}

	/**
	 * called when an error occurs
	 *
	 * @param err exception from twitter
	 */
	public void onError(ConnectionException err) {
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
	 * check app settings if they are correct and save them
	 * wrong settings will be skipped
	 *
	 * @return true if settings are saved successfully
	 */
	private boolean saveConnectionSettings() {
		boolean checkPassed = true;
		// check if proxy settings are correct
		if (enableProxy.isChecked()) {
			checkPassed = proxyAddr.length() > 0 && proxyPort.length() > 0;
			// check IP address
			if (checkPassed) {
				Matcher ipMatch = Patterns.IP_ADDRESS.matcher(proxyAddr.getText());
				checkPassed = ipMatch.matches();
			}
			// check Port number
			if (checkPassed) {
				int port = 0;
				String portStr = proxyPort.getText().toString();
				if (!portStr.isEmpty()) {
					port = Integer.parseInt(portStr);
				}
				checkPassed = port > 0 && port < 65536;
			}
			// check user login
			if (enableAuth.isChecked() && checkPassed) {
				checkPassed = proxyUser.length() > 0 && proxyPass.length() > 0;
			}
			// save settings if correct
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
			settings.setIgnoreProxyWarning(false);
		}
		return checkPassed;
	}
}