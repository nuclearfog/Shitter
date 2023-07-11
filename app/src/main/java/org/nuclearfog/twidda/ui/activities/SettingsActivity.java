package org.nuclearfog.twidda.ui.activities;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.DatabaseAction;
import org.nuclearfog.twidda.backend.async.DatabaseAction.DatabaseParam;
import org.nuclearfog.twidda.backend.async.DatabaseAction.DatabaseResult;
import org.nuclearfog.twidda.backend.async.LocationLoader;
import org.nuclearfog.twidda.backend.async.LocationLoader.LocationLoaderResult;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.notification.PushSubscription;
import org.nuclearfog.twidda.ui.adapter.recyclerview.DropdownAdapter;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.InfoDialog;
import org.nuclearfog.twidda.ui.dialogs.LicenseDialog;
import org.nuclearfog.twidda.ui.dialogs.WebPushDialog;

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
	 * return code to recognize {@link MainActivity} that settings may changed
	 */
	public static final int RETURN_SETTINGS_CHANGED = 0xA3E8;

	public static final int RETURN_FONT_SCALE_CHANGED = 0x2636;

	private static final int REQUEST_PERMISSION_NOTIFICATION = 0x5889;

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
	private Configuration configuration;
	private DatabaseAction databaseAction;
	private LocationLoader locationLoader;

	private DropdownAdapter locationAdapter, fontAdapter, scaleAdapter;

	private Dialog color_dialog_selector, appInfo, license, pushDialog;
	private ConfirmDialog confirmDialog;

	private View enable_auth_label;
	private EditText proxy_address, proxy_port, proxy_user, proxy_pass;
	private SwitchButton enable_proxy, enable_auth, enablePush;
	private Spinner location_dropdown;
	private TextView list_size;
	private ViewGroup root;
	private Button[] colorButtons = new Button[COLOR_COUNT];

	@IntRange(from = 0, to = COLOR_COUNT - 1)
	private int mode = 0;
	private int color = 0;

	private List<Location> locations;

	private AsyncCallback<LocationLoaderResult> locationResult = this::onLocationResult;
	private AsyncCallback<DatabaseResult> databaseResult = this::onDatabaseResult;


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
		View push_label = findViewById(R.id.settings_enable_push_descr);
		SwitchButton toggleImg = findViewById(R.id.toggleImg);
		SwitchButton toolbarOverlap = findViewById(R.id.settings_toolbar_ov);
		SwitchButton enableLike = findViewById(R.id.enable_like);
		SwitchButton enableNitter = findViewById(R.id.settings_enable_twitter_alt);
		SwitchButton enableLocalTl = findViewById(R.id.settings_local_timeline);
		SwitchButton hideSensitive = findViewById(R.id.enable_status_hide_sensitive);
		SwitchButton enableStatusIcons = findViewById(R.id.enable_status_indicators);
		SeekBar listSizeSelector = findViewById(R.id.settings_list_seek);
		Spinner fontSelector = findViewById(R.id.spinner_font);
		Spinner scaleSelector = findViewById(R.id.spinner_scale);
		enablePush = findViewById(R.id.settings_enable_push);
		enable_proxy = findViewById(R.id.settings_enable_proxy);
		enable_auth = findViewById(R.id.settings_enable_auth);
		location_dropdown = findViewById(R.id.spinner_woeid);
		enable_auth_label = findViewById(R.id.settings_enable_auth_descr);
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
		proxy_address = findViewById(R.id.edit_proxy_address);
		proxy_port = findViewById(R.id.edit_proxy_port);
		proxy_user = findViewById(R.id.edit_proxyuser);
		proxy_pass = findViewById(R.id.edit_proxypass);
		list_size = findViewById(R.id.settings_list_size);
		root = findViewById(R.id.settings_layout);

		settings = GlobalSettings.get(this);
		configuration = settings.getLogin().getConfiguration();
		confirmDialog = new ConfirmDialog(this, this);
		appInfo = new InfoDialog(this);
		license = new LicenseDialog(this);
		pushDialog = new WebPushDialog(this);
		locationLoader = new LocationLoader(this);
		databaseAction = new DatabaseAction(this);
		fontAdapter = new DropdownAdapter(getApplicationContext());
		scaleAdapter = new DropdownAdapter(getApplicationContext());
		locationAdapter = new DropdownAdapter(getApplicationContext());

		toolbar.setTitle(R.string.title_settings);
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());

		fontAdapter.setFonts(GlobalSettings.FONT_TYPES);
		fontAdapter.setItems(GlobalSettings.FONT_NAMES);
		scaleAdapter.setItems(R.array.scales);
		locationAdapter.setItem(settings.getTrendLocation().getFullName());

		location_dropdown.setAdapter(locationAdapter);
		fontSelector.setAdapter(fontAdapter);
		scaleSelector.setAdapter(scaleAdapter);
		fontSelector.setSelection(settings.getFontIndex(), false);
		scaleSelector.setSelection(settings.getScaleIndex(), false);
		fontSelector.setSelected(false);
		scaleSelector.setSelected(false);
		location_dropdown.setSelected(false);

		if (configuration != Configuration.TWITTER1 && configuration != Configuration.TWITTER2) {
			enableLocalTl.setVisibility(View.VISIBLE);
			trend_card.setVisibility(View.GONE);
		}
		if (!configuration.isWebpushSupported()) {
			push_label.setVisibility(View.GONE);
			enablePush.setVisibility(View.GONE);
		}
		if (!settings.isLoggedIn()) {
			user_card.setVisibility(View.GONE);
			push_label.setVisibility(View.GONE);
			enablePush.setVisibility(View.GONE);
		}
		if (!settings.isProxyEnabled()) {
			proxy_address.setVisibility(View.GONE);
			proxy_port.setVisibility(View.GONE);
			proxy_user.setVisibility(View.GONE);
			proxy_pass.setVisibility(View.GONE);
			enable_auth.setVisibility(View.GONE);
			enable_auth_label.setVisibility(View.GONE);
		} else if (!settings.isProxyAuthSet()) {
			proxy_user.setVisibility(View.GONE);
			proxy_pass.setVisibility(View.GONE);
		}
		if (settings.likeEnabled()) {
			colorButtons[COLOR_FAVORITE].setText(R.string.settings_color_like);
		} else {
			colorButtons[COLOR_FAVORITE].setText(R.string.settings_color_fav);
		}
		toggleImg.setCheckedImmediately(settings.imagesEnabled());
		toolbarOverlap.setCheckedImmediately(settings.toolbarOverlapEnabled());
		enableLike.setCheckedImmediately(settings.likeEnabled());
		enableNitter.setCheckedImmediately(settings.twitterAltSet());
		enableLocalTl.setCheckedImmediately(settings.useLocalTimeline());
		hideSensitive.setCheckedImmediately(settings.hideSensitiveEnabled());
		enableStatusIcons.setCheckedImmediately(settings.statusIndicatorsEnabled());
		enablePush.setCheckedImmediately(settings.pushEnabled());
		enable_proxy.setCheckedImmediately(settings.isProxyEnabled());
		enable_auth.setCheckedImmediately(settings.isProxyAuthSet());
		proxy_address.setText(settings.getProxyHost());
		proxy_port.setText(settings.getProxyPort());
		proxy_user.setText(settings.getProxyUser());
		proxy_pass.setText(settings.getProxyPass());
		list_size.setText(Integer.toString(settings.getListSize()));
		listSizeSelector.setProgress(settings.getListSize() / 10 - 1);
		setButtonColors();

		for (Button button : colorButtons)
			button.setOnClickListener(this);
		logout.setOnClickListener(this);
		delButton.setOnClickListener(this);
		toggleImg.setOnCheckedChangeListener(this);
		enablePush.setOnCheckedChangeListener(this);
		enableLike.setOnCheckedChangeListener(this);
		enableNitter.setOnCheckedChangeListener(this);
		enableLocalTl.setOnCheckedChangeListener(this);
		enableStatusIcons.setOnCheckedChangeListener(this);
		hideSensitive.setOnCheckedChangeListener(this);
		enable_proxy.setOnCheckedChangeListener(this);
		enable_auth.setOnCheckedChangeListener(this);
		push_label.setOnClickListener(this);
		toolbarOverlap.setOnCheckedChangeListener(this);
		fontSelector.setOnItemSelectedListener(this);
		scaleSelector.setOnItemSelectedListener(this);
		listSizeSelector.setOnSeekBarChangeListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		setResult(RETURN_SETTINGS_CHANGED);
		if (configuration == Configuration.TWITTER1 || configuration == Configuration.TWITTER2) {
			if (location_dropdown.getCount() <= 1) {
				locationLoader.execute(null, locationResult);
			}
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
		locationLoader.cancel();
		databaseAction.cancel();
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
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_PERMISSION_NOTIFICATION) {
			if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				PushSubscription.subscripe(getApplicationContext());
				pushDialog.show();
			} else {
				enablePush.setChecked(false);
			}
		}
	}


	@Override
	public void onConfirm(int type) {
		// remove account from database
		if (type == ConfirmDialog.APP_LOG_OUT) {
			settings.setLogin(null, true);
			databaseAction.execute(new DatabaseParam(DatabaseParam.LOGOUT), databaseResult);
		}
		// confirm delete app data and cache
		else if (type == ConfirmDialog.DELETE_APP_DATA) {
			databaseAction.execute(new DatabaseParam(DatabaseParam.DELETE), databaseResult);
		}
		// confirm leaving without saving proxy changes
		else if (type == ConfirmDialog.WRONG_PROXY) {
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
			showColorPicker(color, false);
		}
		// set font color
		else if (v.getId() == R.id.color_text) {
			mode = COLOR_TEXT;
			color = settings.getTextColor();
			showColorPicker(color, false);
		}
		// set popup color
		else if (v.getId() == R.id.color_window) {
			mode = COLOR_WINDOW;
			color = settings.getPopupColor();
			showColorPicker(color, false);
		}
		// set highlight color
		else if (v.getId() == R.id.highlight_color) {
			mode = COLOR_HIGHLIGHT;
			color = settings.getHighlightColor();
			showColorPicker(color, false);
		}
		// set card color
		else if (v.getId() == R.id.color_card) {
			mode = COLOR_CARD;
			color = settings.getCardColor();
			showColorPicker(color, true);
		}
		// set icon color
		else if (v.getId() == R.id.color_icon) {
			mode = COLOR_ICON;
			color = settings.getIconColor();
			showColorPicker(color, false);
		}
		// set repost icon color
		else if (v.getId() == R.id.color_rt) {
			mode = COLOR_REPOST;
			color = settings.getRepostIconColor();
			showColorPicker(color, false);
		}
		// set favorite icon color
		else if (v.getId() == R.id.color_fav) {
			mode = COLOR_FAVORITE;
			color = settings.getFavoriteIconColor();
			showColorPicker(color, false);
		}
		// set follow icon color
		else if (v.getId() == R.id.color_f_req) {
			mode = COLOR_FOLLOW_REQUEST;
			color = settings.getFollowPendingColor();
			showColorPicker(color, false);
		}
		// set follow icon color
		else if (v.getId() == R.id.color_follow) {
			mode = COLOR_FOLLOWING;
			color = settings.getFollowIconColor();
			showColorPicker(color, false);
		}
		// show push configuration dialog
		else if (v.getId() == R.id.settings_enable_push_descr) {
			if (enablePush.isChecked()) {
				pushDialog.show();
			}
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
					AppStyles.setTheme(root);
					setButtonColors();
					break;

				case COLOR_TEXT:
					settings.setTextColor(color);
					fontAdapter.notifyDataSetChanged();
					scaleAdapter.notifyDataSetChanged();
					if (settings.isLoggedIn()) {
						locationAdapter.notifyDataSetChanged();
					}
					AppStyles.setTheme(root);
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
					AppStyles.setTheme(root);
					setButtonColors();
					break;

				case COLOR_ICON:
					settings.setIconColor(color);
					invalidateOptionsMenu();
					AppStyles.setTheme(root);
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
		// enable/disable local timeline (Mastodon)
		else if (c.getId() == R.id.settings_local_timeline) {
			settings.setLocalTimeline(checked);
		}
		// enable/disable push notification
		else if (c.getId() == R.id.settings_enable_push) {
			if (checked) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
					requestPermissions(new String[]{POST_NOTIFICATIONS}, REQUEST_PERMISSION_NOTIFICATION);
				} else {
					PushSubscription.subscripe(getApplicationContext());
					pushDialog.show();
				}
			} else {
				PushSubscription.unsubscripe(this);
			}
			settings.setPushEnabled(checked);
		}
		// enable proxy settings
		else if (c.getId() == R.id.settings_enable_proxy) {
			if (checked) {
				proxy_address.setVisibility(View.VISIBLE);
				proxy_port.setVisibility(View.VISIBLE);
				enable_auth.setVisibility(View.VISIBLE);
				enable_auth_label.setVisibility(View.VISIBLE);
			} else {
				proxy_address.setVisibility(View.GONE);
				proxy_port.setVisibility(View.GONE);
				enable_auth_label.setVisibility(View.GONE);
				enable_auth.setVisibility(View.GONE);
				enable_auth.setChecked(false);
			}
		}
		// enable proxy authentication
		else if (c.getId() == R.id.settings_enable_auth) {
			if (checked) {
				proxy_user.setVisibility(View.VISIBLE);
				proxy_pass.setVisibility(View.VISIBLE);
			} else {
				proxy_user.setVisibility(View.GONE);
				proxy_pass.setVisibility(View.GONE);
			}
		}
		// hide sensitive content
		else if (c.getId() == R.id.enable_status_hide_sensitive) {
			settings.hideSensitive(checked);
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// Trend location spinner
		if (parent.getId() == R.id.spinner_woeid) {
			settings.setTrendLocation(locations.get(position));
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
			AppStyles.updateFontScale(this);
			setResult(RETURN_FONT_SCALE_CHANGED);
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
	 * called from {@link DatabaseAction}
	 */
	private void onDatabaseResult(@NonNull DatabaseResult result) {
		switch (result.mode) {
			case DatabaseResult.DELETE:
				Toast.makeText(getApplicationContext(), R.string.info_database_cleared, Toast.LENGTH_SHORT).show();
				break;

			case DatabaseResult.LOGOUT:
				setResult(RETURN_APP_LOGOUT);
				finish();
				break;

			case DatabaseResult.ERROR:
				Toast.makeText(getApplicationContext(), R.string.error_database_cleared, Toast.LENGTH_SHORT).show();
				break;

		}
	}

	/**
	 * called from {@link LocationLoader}
	 *
	 * @param result result from {@link LocationLoader}
	 */
	private void onLocationResult(LocationLoaderResult result) {
		if (result.locations != null) {
			int position = -1;
			this.locations = result.locations;
			String[] items = new String[result.locations.size()];
			for (int i = 0; i < items.length; i++) {
				items[i] = result.locations.get(i).getFullName();
				if (items[i].equals(settings.getTrendLocation().getFullName())) {
					position = i;
				}
			}
			locationAdapter.setItems(items);
			// set item of a previously selection if exists
			if (position >= 0) {
				location_dropdown.setSelection(position, false);
			}
			// set listener after modifying content to prevent listener call
			location_dropdown.setOnItemSelectedListener(this);
		} else {
			ErrorUtils.showErrorMessage(this, result.exception);
		}
	}

	/**
	 * show color picker dialog with preselected color
	 *
	 * @param preColor    preselected color
	 * @param enableAlpha true to enable alpha slider
	 */
	private void showColorPicker(int preColor, boolean enableAlpha) {
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
		if (enable_proxy.isChecked()) {
			checkPassed = proxy_address.length() > 0 && proxy_port.length() > 0;
			// check IP address
			if (checkPassed) {
				Matcher ipMatch = Patterns.IP_ADDRESS.matcher(proxy_address.getText());
				checkPassed = ipMatch.matches();
			}
			// check Port number
			if (checkPassed) {
				int port = 0;
				String portStr = proxy_port.getText().toString();
				if (!portStr.isEmpty()) {
					port = Integer.parseInt(portStr);
				}
				checkPassed = port > 0 && port < 65536;
			}
			// check user login
			if (enable_auth.isChecked() && checkPassed) {
				checkPassed = proxy_user.length() > 0 && proxy_pass.length() > 0;
			}
			// save settings if correct
			if (checkPassed) {
				String proxyAddrStr = proxy_address.getText().toString();
				String proxyPortStr = proxy_port.getText().toString();
				String proxyUserStr = proxy_user.getText().toString();
				String proxyPassStr = proxy_pass.getText().toString();
				settings.setProxyServer(proxyAddrStr, proxyPortStr, proxyUserStr, proxyPassStr);
				settings.setProxyEnabled(true);
				settings.setProxyAuthSet(enable_auth.isChecked());
			}
		} else {
			settings.clearProxyServer();
		}
		return checkPassed;
	}
}