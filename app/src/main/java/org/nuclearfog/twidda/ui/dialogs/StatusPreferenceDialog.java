package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.update.StatusUpdate;
import org.nuclearfog.twidda.backend.helper.update.UserUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.adapter.listview.DropdownAdapter;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * status editor preference dialog used to set additional status information
 *
 * @author nuclearfog
 */
public class StatusPreferenceDialog extends Dialog implements OnCheckedChangeListener, OnItemSelectedListener, OnClickListener, TimePickerDialog.TimeSelectedCallback {

	private Spinner visibilitySelector, languageSelector;
	private SwitchButton sensitiveCheck, spoilerCheck;
	private TextView scheduleText;

	private DropdownAdapter visibility_adapter, language_adapter;
	private TimePickerDialog timePicker;
	private GlobalSettings settings;
	private String[] languageCodes;

	@Nullable
	private StatusUpdate statusUpdate;
	@Nullable
	private UserUpdate userUpdate;

	/**
	 * create dialog to set user preferences
	 *
	 * @param userUpdate user update holder
	 */
	public StatusPreferenceDialog(Activity activity, @NonNull UserUpdate userUpdate) {
		this(activity);
		this.userUpdate = userUpdate;
	}

	/**
	 * create dialog to set status preferences
	 *
	 * @param statusUpdate status update holder
	 */
	public StatusPreferenceDialog(Activity activity,  @NonNull StatusUpdate statusUpdate) {
		this(activity);
		this.statusUpdate = statusUpdate;
	}

	/**
	 *
	 */
	private StatusPreferenceDialog(Activity activity) {
		super(activity, R.style.DefaultDialog);
		visibility_adapter = new DropdownAdapter(activity.getApplicationContext());
		language_adapter = new DropdownAdapter(activity.getApplicationContext());
		timePicker = new TimePickerDialog(activity, this);
		settings = GlobalSettings.get(getContext());
		// initialize language selector
		Map<String, String> languages = new TreeMap<>();
		languages.put("", "");
		Locale[] locales = Locale.getAvailableLocales();
		for (Locale locale : locales) {
			languages.put(locale.getDisplayLanguage(), locale.getLanguage());
		}
		languageCodes = languages.values().toArray(new String[0]);
		language_adapter.setItems(languages.keySet().toArray(new String[0]));
		visibility_adapter.setItems(R.array.visibility);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_status);
		ViewGroup rootView = findViewById(R.id.dialog_status_root);
		View statusVisibility = findViewById(R.id.dialog_status_visibility_container);
		View statusSpoiler = findViewById(R.id.dialog_status_spoiler_container);
		Button timePicker = findViewById(R.id.dialog_status_time_picker);
		languageSelector = findViewById(R.id.dialog_status_language);
		visibilitySelector = findViewById(R.id.dialog_status_visibility);
		sensitiveCheck = findViewById(R.id.dialog_status_sensitive);
		spoilerCheck = findViewById(R.id.dialog_status_spoiler);
		scheduleText = findViewById(R.id.dialog_status_time_set);

		AppStyles.setTheme(rootView, settings.getPopupColor());
		languageSelector.setAdapter(language_adapter);
		languageSelector.setSelection(0, false);
		languageSelector.setSelected(false);
		visibilitySelector.setAdapter(visibility_adapter);
		visibilitySelector.setSelection(0, false);
		visibilitySelector.setSelected(false);

		// enable/disable functions
		if (!settings.getLogin().getConfiguration().statusVisibilitySupported()) {
			statusVisibility.setVisibility(View.GONE);
		}
		if (!settings.getLogin().getConfiguration().statusSpoilerSupported()) {
			statusSpoiler.setVisibility(View.GONE);
		}
		if (userUpdate != null) {
			scheduleText.setVisibility(View.GONE);
			timePicker.setVisibility(View.GONE);
			statusSpoiler.setVisibility(View.GONE);
		}
		sensitiveCheck.setOnCheckedChangeListener(this);
		spoilerCheck.setOnCheckedChangeListener(this);
		languageSelector.setOnItemSelectedListener(this);
		visibilitySelector.setOnItemSelectedListener(this);
		timePicker.setOnClickListener(this);
	}


	@Override
	protected void onStart() {
		if (statusUpdate != null) {
			if (statusUpdate.getVisibility() == Status.VISIBLE_PUBLIC) {
				visibilitySelector.setSelection(0, false);
			} else if (statusUpdate.getVisibility() == Status.VISIBLE_PRIVATE) {
				visibilitySelector.setSelection(1, false);
			} else if (statusUpdate.getVisibility() == Status.VISIBLE_DIRECT) {
				visibilitySelector.setSelection(2, false);
			} else if (statusUpdate.getVisibility() == Status.VISIBLE_UNLISTED) {
				visibilitySelector.setSelection(3, false);
			}
			sensitiveCheck.setCheckedImmediately(statusUpdate.isSensitive());
			spoilerCheck.setCheckedImmediately(statusUpdate.isSpoiler());
			if (!statusUpdate.getLanguageCode().isEmpty()) {
				for (int i = 0; i < languageCodes.length; i++) {
					if (languageCodes[i].equals(statusUpdate.getLanguageCode())) {
						languageSelector.setSelection(i);
					}
				}
			}
		} else if (userUpdate != null) {
			if (userUpdate.getStatusVisibility() == Status.VISIBLE_PUBLIC) {
				visibilitySelector.setSelection(0, false);
			} else if (userUpdate.getStatusVisibility() == Status.VISIBLE_PRIVATE) {
				visibilitySelector.setSelection(1, false);
			} else if (userUpdate.getStatusVisibility() == Status.VISIBLE_DIRECT) {
				visibilitySelector.setSelection(2, false);
			} else if (userUpdate.getStatusVisibility() == Status.VISIBLE_UNLISTED) {
				visibilitySelector.setSelection(3, false);
			}
			sensitiveCheck.setCheckedImmediately(userUpdate.isSensitive());
			if (!userUpdate.getLanguageCode().isEmpty()) {
				for (int i = 0; i < languageCodes.length; i++) {
					if (languageCodes[i].equals(userUpdate.getLanguageCode())) {
						languageSelector.setSelection(i);
					}
				}
			}
		}
		super.onStart();
	}


	@Override
	public void show() {
		if (!isShowing()) {
			super.show();
		}
	}


	@Override
	public void dismiss() {
		if (isShowing()) {
			super.dismiss();
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_status_time_picker) {
			if (statusUpdate != null) {
				timePicker.show(statusUpdate.getScheduleTime());
			}
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.dialog_status_sensitive) {
			if (statusUpdate != null) {
				statusUpdate.setSensitive(isChecked);
			} else if (userUpdate != null) {
				userUpdate.setContentSensitive(isChecked);
			}
		} else if (buttonView.getId() == R.id.dialog_status_spoiler) {
			if (statusUpdate != null) {
				statusUpdate.setSpoiler(isChecked);
			}
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getId() == R.id.dialog_status_visibility) {
			if (statusUpdate != null) {
				if (position == 0) {
					statusUpdate.setVisibility(Status.VISIBLE_DEFAULT);
				} else if (position == 1) {
					statusUpdate.setVisibility(Status.VISIBLE_PUBLIC);
				} else if (position == 2) {
					statusUpdate.setVisibility(Status.VISIBLE_PRIVATE);
				} else if (position == 3) {
					statusUpdate.setVisibility(Status.VISIBLE_DIRECT);
				} else if (position == 4) {
					statusUpdate.setVisibility(Status.VISIBLE_UNLISTED);
				}
			} else if (userUpdate != null) {
				if (position == 0) {
					userUpdate.setStatusVisibility(Status.VISIBLE_DEFAULT);
				} else if (position == 1) {
					userUpdate.setStatusVisibility(Status.VISIBLE_PUBLIC);
				} else if (position == 2) {
					userUpdate.setStatusVisibility(Status.VISIBLE_PRIVATE);
				} else if (position == 3) {
					userUpdate.setStatusVisibility(Status.VISIBLE_DIRECT);
				} else if (position == 4) {
					userUpdate.setStatusVisibility(Status.VISIBLE_UNLISTED);
				}
			}
		} else if (parent.getId() == R.id.dialog_status_language) {
			if (position > 0) {
				if (statusUpdate != null) {
					statusUpdate.addLanguage(languageCodes[position]);
				} else if (userUpdate != null) {
					userUpdate.setLanguageCode(languageCodes[position]);
				}
			}
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	@Override
	public void onTimeSelected(long time) {
		if (statusUpdate != null) {
			statusUpdate.setScheduleTime(time);
		}
		if (time != 0L) {
			scheduleText.setText(new Date(time).toString());
		} else {
			scheduleText.setText("");
		}
	}
}