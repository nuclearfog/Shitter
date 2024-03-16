package org.nuclearfog.twidda.ui.activities;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.DatabaseAction;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.notification.PushSubscription;
import org.nuclearfog.twidda.ui.adapter.listview.DropdownAdapter;
import org.nuclearfog.twidda.ui.dialogs.ColorPickerDialog;
import org.nuclearfog.twidda.ui.dialogs.ColorPickerDialog.OnColorSelectedListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.InfoDialog;
import org.nuclearfog.twidda.ui.dialogs.LicenseDialog;
import org.nuclearfog.twidda.ui.dialogs.WebPushDialog;

import java.util.regex.Matcher;

/**
 * Settings Activity class.
 *
 * @author nuclearfog
 */
public class SettingsActivity extends AppCompatActivity implements OnClickListener, OnSeekBarChangeListener,
		OnCheckedChangeListener, OnItemSelectedListener, OnConfirmListener, OnColorSelectedListener {

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
	private static final int COLOR_COUNT = 9;
	// app colors
	private static final int COLOR_BACKGROUND = 0;
	private static final int COLOR_TEXT = 1;
	private static final int COLOR_WINDOW = 2;
	private static final int COLOR_HIGHLIGHT = 3;
	private static final int COLOR_CARD = 4;
	private static final int COLOR_ICON = 5;
	private static final int COLOR_REPOST = 6;
	private static final int COLOR_FAVORITE = 7;
	private static final int COLOR_FOLLOWING = 8;

	private GlobalSettings settings;
	private DatabaseAction databaseAction;

	private DropdownAdapter fontAdapter, scaleAdapter;

	private View enable_auth_label;
	private EditText proxy_address, proxy_port, proxy_user, proxy_pass;
	private SwitchButton enable_proxy, enable_auth, enablePush;
	private TextView list_size;
	private ViewGroup root;
	private Button[] colorButtons = new Button[COLOR_COUNT];

	private AsyncCallback<DatabaseAction.Result> databaseResult = this::onDatabaseResult;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.page_settings);
		Button delButton = findViewById(R.id.page_settings_button_delete_data);
		Button logout = findViewById(R.id.page_settings_button_logout);
		Toolbar toolbar = findViewById(R.id.page_settings_toolbar);
		View user_card = findViewById(R.id.page_settings_card_data);
		View push_label = findViewById(R.id.page_settings_enable_push_label);
		SwitchButton toggleImg = findViewById(R.id.page_settings_enable_images);
		SwitchButton toolbarOverlap = findViewById(R.id.page_settings_toolbar_collapse);
		SwitchButton enableLike = findViewById(R.id.page_settings_enable_like);
		SwitchButton hideSensitive = findViewById(R.id.page_settings_sensitive_enable);
		SwitchButton enableStatusIcons = findViewById(R.id.page_settings_enable_status_indicators);
		SwitchButton enableFloatingButton = findViewById(R.id.page_settings_enable_floating_button);
		SwitchButton chronologicalTimeline = findViewById(R.id.page_settings_chronological_timeline_sw);
		SeekBar listSizeSelector = findViewById(R.id.page_settings_list_seek);
		Spinner fontSelector = findViewById(R.id.page_settings_font_selector);
		Spinner scaleSelector = findViewById(R.id.page_settings_textscale_selector);
		Spinner publicTimelineSelector = findViewById(R.id.page_settings_public_timeline_selector);
		enablePush = findViewById(R.id.page_settings_enable_push);
		enable_proxy = findViewById(R.id.page_settings_enable_proxy);
		enable_auth = findViewById(R.id.page_settings_enable_proxyauth);
		enable_auth_label = findViewById(R.id.page_settings_enable_proxyauth_label);
		colorButtons[COLOR_BACKGROUND] = findViewById(R.id.page_settings_color_background);
		colorButtons[COLOR_TEXT] = findViewById(R.id.page_settings_color_text);
		colorButtons[COLOR_WINDOW] = findViewById(R.id.page_settings_color_window);
		colorButtons[COLOR_HIGHLIGHT] = findViewById(R.id.page_settings_highlight_color);
		colorButtons[COLOR_CARD] = findViewById(R.id.page_settings_color_card);
		colorButtons[COLOR_ICON] = findViewById(R.id.page_settings_color_icon);
		colorButtons[COLOR_REPOST] = findViewById(R.id.page_settings_color_repost);
		colorButtons[COLOR_FAVORITE] = findViewById(R.id.page_settings_color_favorite);
		colorButtons[COLOR_FOLLOWING] = findViewById(R.id.page_settings_color_follow);
		proxy_address = findViewById(R.id.page_settings_input_proxyaddress);
		proxy_port = findViewById(R.id.page_settings_input_proxyport);
		proxy_user = findViewById(R.id.page_settings_input_proxyuser);
		proxy_pass = findViewById(R.id.page_settings_input_proxypass);
		list_size = findViewById(R.id.page_settings_list_seek_value);
		root = findViewById(R.id.page_settings_root);

		settings = GlobalSettings.get(this);
		Configuration configuration = settings.getLogin().getConfiguration();
		databaseAction = new DatabaseAction(this);
		fontAdapter = new DropdownAdapter(getApplicationContext());
		scaleAdapter = new DropdownAdapter(getApplicationContext());

		toolbar.setTitle(R.string.menu_open_settings);
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());

		DropdownAdapter publicTimelineAdapter = new DropdownAdapter(this);
		fontAdapter.setFonts(GlobalSettings.FONT_TYPES);
		fontAdapter.setItems(GlobalSettings.FONT_NAMES);
		scaleAdapter.setItems(R.array.scales);
		publicTimelineAdapter.setItems(R.array.public_timelines);

		publicTimelineSelector.setAdapter(publicTimelineAdapter);
		fontSelector.setAdapter(fontAdapter);
		scaleSelector.setAdapter(scaleAdapter);
		fontSelector.setSelection(settings.getFontIndex(), false);
		scaleSelector.setSelection(settings.getScaleIndex(), false);
		fontSelector.setSelected(false);
		scaleSelector.setSelected(false);

		if (!configuration.isPublicTimelinesupported()) {
			publicTimelineSelector.setVisibility(View.GONE);
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
		if (settings.getPublicTimeline().equals(GlobalSettings.TIMELINE_COMBINED)) {
			publicTimelineSelector.setSelection(0);
		} else if (settings.getPublicTimeline().equals(GlobalSettings.TIMELINE_LOCAL)) {
			publicTimelineSelector.setSelection(1);
		} else if (settings.getPublicTimeline().equals(GlobalSettings.TIMELINE_REMOTE)) {
			publicTimelineSelector.setSelection(2);
		}
		toggleImg.setCheckedImmediately(settings.imagesEnabled());
		toolbarOverlap.setCheckedImmediately(settings.toolbarOverlapEnabled());
		enableLike.setCheckedImmediately(settings.likeEnabled());
		hideSensitive.setCheckedImmediately(settings.hideSensitiveEnabled());
		enableStatusIcons.setCheckedImmediately(settings.statusIndicatorsEnabled());
		enableFloatingButton.setCheckedImmediately(settings.floatingButtonEnabled());
		chronologicalTimeline.setChecked(settings.chronologicalTimelineEnabled());
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
		chronologicalTimeline.setOnCheckedChangeListener(this);
		enableStatusIcons.setOnCheckedChangeListener(this);
		hideSensitive.setOnCheckedChangeListener(this);
		enableFloatingButton.setOnCheckedChangeListener(this);
		enable_proxy.setOnCheckedChangeListener(this);
		enable_auth.setOnCheckedChangeListener(this);
		push_label.setOnClickListener(this);
		toolbarOverlap.setOnCheckedChangeListener(this);
		fontSelector.setOnItemSelectedListener(this);
		scaleSelector.setOnItemSelectedListener(this);
		publicTimelineSelector.setOnItemSelectedListener(this);
		listSizeSelector.setOnSeekBarChangeListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		setResult(RETURN_SETTINGS_CHANGED);
	}


	@Override
	public void onBackPressed() {
		if (saveConnectionSettings()) {
			super.onBackPressed();
		} else {
			ConfirmDialog.show(this, ConfirmDialog.WRONG_PROXY, null);
		}
	}


	@Override
	protected void onDestroy() {
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
			InfoDialog.show(this);
		} else if (item.getItemId() == R.id.settings_licenses) {
			LicenseDialog.show(this);
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_PERMISSION_NOTIFICATION) {
			if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				PushSubscription.subscripe(getApplicationContext());
				WebPushDialog.show(this);
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
			databaseAction.execute(new DatabaseAction.Param(DatabaseAction.Param.LOGOUT), databaseResult);
		}
		// confirm delete app data and cache
		else if (type == ConfirmDialog.DELETE_APP_DATA) {
			databaseAction.execute(new DatabaseAction.Param(DatabaseAction.Param.DELETE), databaseResult);
		}
		// confirm leaving without saving proxy changes
		else if (type == ConfirmDialog.WRONG_PROXY) {
			finish();
		}
	}


	@Override
	public void onClick(View v) {
		// delete database
		if (v.getId() == R.id.page_settings_button_delete_data) {
			ConfirmDialog.show(this, ConfirmDialog.DELETE_APP_DATA, null);
		}
		// logout
		else if (v.getId() == R.id.page_settings_button_logout) {
			ConfirmDialog.show(this, ConfirmDialog.APP_LOG_OUT, null);
		}
		// show push configuration dialog
		else if (v.getId() == R.id.page_settings_enable_push_label) {
			if (enablePush.isChecked()) {
				WebPushDialog.show(this);
			}
		}
		// set background color
		else if (v.getId() == R.id.page_settings_color_background) {
			int color = settings.getBackgroundColor();
			ColorPickerDialog.show(this, color, COLOR_BACKGROUND, false);
		}
		// set font color
		else if (v.getId() == R.id.page_settings_color_text) {
			int color = settings.getTextColor();
			ColorPickerDialog.show(this, color, COLOR_TEXT, false);
		}
		// set popup color
		else if (v.getId() == R.id.page_settings_color_window) {
			int color = settings.getPopupColor();
			ColorPickerDialog.show(this, color, COLOR_WINDOW, false);
		}
		// set highlight color
		else if (v.getId() == R.id.page_settings_highlight_color) {
			int color = settings.getHighlightColor();
			ColorPickerDialog.show(this, color, COLOR_HIGHLIGHT, false);
		}
		// set card color
		else if (v.getId() == R.id.page_settings_color_card) {
			int color = settings.getCardColor();
			ColorPickerDialog.show(this, color, COLOR_CARD, true);
		}
		// set icon color
		else if (v.getId() == R.id.page_settings_color_icon) {
			int color = settings.getIconColor();
			ColorPickerDialog.show(this, color, COLOR_ICON, false);
		}
		// set repost icon color
		else if (v.getId() == R.id.page_settings_color_repost) {
			int color = settings.getRepostIconColor();
			ColorPickerDialog.show(this, color, COLOR_REPOST, false);
		}
		// set favorite icon color
		else if (v.getId() == R.id.page_settings_color_favorite) {
			int color = settings.getFavoriteIconColor();
			ColorPickerDialog.show(this, color, COLOR_FAVORITE, false);
		}
		// set follow icon color
		else if (v.getId() == R.id.page_settings_color_follow) {
			int color = settings.getFollowIconColor();
			ColorPickerDialog.show(this, color, COLOR_FOLLOWING, false);
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton c, boolean checked) {
		// toggle image loading
		if (c.getId() == R.id.page_settings_enable_images) {
			settings.setImageLoad(checked);
		}
		// enable toolbar overlap
		else if (c.getId() == R.id.page_settings_toolbar_collapse) {
			settings.setToolbarOverlap(checked);
		}
		// enable like
		else if (c.getId() == R.id.page_settings_enable_like) {
			settings.enableLike(checked);
			if (checked) {
				colorButtons[COLOR_FAVORITE].setText(R.string.settings_color_like);
			} else {
				colorButtons[COLOR_FAVORITE].setText(R.string.settings_color_fav);
			}
		}
		// enable status indicators
		else if (c.getId() == R.id.page_settings_enable_status_indicators) {
			settings.enableStatusIndicators(checked);
		}
		// enable floating button
		else if (c.getId() == R.id.page_settings_enable_floating_button) {
			settings.enableFloatingButton(checked);
		}
		// enable/disable push notification
		else if (c.getId() == R.id.page_settings_enable_push) {
			if (checked) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
					requestPermissions(new String[]{POST_NOTIFICATIONS}, REQUEST_PERMISSION_NOTIFICATION);
				} else {
					PushSubscription.subscripe(getApplicationContext());
					WebPushDialog.show(this);
				}
			} else {
				PushSubscription.unsubscripe(this);
			}
			settings.setPushEnabled(checked);
		}
		// enable proxy settings
		else if (c.getId() == R.id.page_settings_enable_proxy) {
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
		else if (c.getId() == R.id.page_settings_enable_proxyauth) {
			if (checked) {
				proxy_user.setVisibility(View.VISIBLE);
				proxy_pass.setVisibility(View.VISIBLE);
			} else {
				proxy_user.setVisibility(View.GONE);
				proxy_pass.setVisibility(View.GONE);
			}
		}
		// hide sensitive content
		else if (c.getId() == R.id.page_settings_sensitive_enable) {
			settings.hideSensitive(checked);
		}
		// use chronological ordered timeline
		else if (c.getId() == R.id.page_settings_chronological_timeline_sw) {
			settings.enableChronologicalTimeline(checked);
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// Font type spinner
		if (parent.getId() == R.id.page_settings_font_selector) {
			settings.setFontIndex(position);
			AppStyles.setFontStyle(root);
		}
		// Font scale spinner
		else if (parent.getId() == R.id.page_settings_textscale_selector) {
			settings.setScaleIndex(position);
			AppStyles.updateFontScale(this);
			setResult(RETURN_FONT_SCALE_CHANGED);
		} else if (parent.getId() == R.id.page_settings_public_timeline_selector) {
			if (position == 0) {
				settings.setPublicTimeline(GlobalSettings.TIMELINE_COMBINED);
			} else if (position == 1) {
				settings.setPublicTimeline(GlobalSettings.TIMELINE_LOCAL);
			} else if (position == 2) {
				settings.setPublicTimeline(GlobalSettings.TIMELINE_REMOTE);
			}
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	@Override
	public void onColorSelected(int type, int color) {
		switch (type) {
			case COLOR_BACKGROUND:
				settings.setBackgroundColor(color);
				fontAdapter.notifyDataSetChanged();
				scaleAdapter.notifyDataSetChanged();
				AppStyles.setTheme(root);
				setButtonColors();
				break;

			case COLOR_TEXT:
				settings.setTextColor(color);
				fontAdapter.notifyDataSetChanged();
				scaleAdapter.notifyDataSetChanged();
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

			case COLOR_FOLLOWING:
				settings.setFollowIconColor(color);
				AppStyles.setColorButton(colorButtons[COLOR_FOLLOWING], color);
				break;
		}
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
	private void onDatabaseResult(@NonNull DatabaseAction.Result result) {
		switch (result.action) {
			case DatabaseAction.Result.DELETE:
				Toast.makeText(getApplicationContext(), R.string.info_database_cleared, Toast.LENGTH_SHORT).show();
				break;

			case DatabaseAction.Result.LOGOUT:
				setResult(RETURN_APP_LOGOUT);
				finish();
				break;

			case DatabaseAction.Result.ERROR:
				Toast.makeText(getApplicationContext(), R.string.error_database_cleared, Toast.LENGTH_SHORT).show();
				break;

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
				settings.setProxyAuthSet(enable_auth.isChecked());
			}
		} else {
			settings.clearProxyServer();
		}
		return checkPassed;
	}
}