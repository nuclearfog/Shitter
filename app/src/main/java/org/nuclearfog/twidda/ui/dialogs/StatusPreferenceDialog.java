package org.nuclearfog.twidda.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.update.StatusPreferenceUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.adapter.listview.DropdownAdapter;
import org.nuclearfog.twidda.ui.dialogs.TimePickerDialog.TimeSelectedCallback;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * status editor preference dialog used to set additional status information
 *
 * @author nuclearfog
 */
public class StatusPreferenceDialog extends DialogFragment implements OnCheckedChangeListener, OnItemSelectedListener, OnClickListener, TimeSelectedCallback {

	/**
	 *
	 */
	private static final String TAG = "StatusPreferenceDialog";

	/**
	 * Bundle key to set/restore status preference configuration
	 * value type is {@link StatusPreferenceUpdate}
	 */
	private static final String KEY_PREF = "pref-status";

	/**
	 * Bundle key to enable extra settings of the configuration
	 * value type is boolean
	 */
	private static final String KEY_EXT = "pref-extended";

	// index of the visibility spinner list (see R.array.visibility)
	private static final int IDX_VISIBILITY_DEFAULT = 0;
	private static final int IDX_VISIBILITY_PUBLIC = 1;
	private static final int IDX_VISIBILITY_PRIVATE = 2;
	private static final int IDX_VISIBILITY_DIRECT = 3;
	private static final int IDX_VISIBILITY_UNLISTED = 4;

	private TextView scheduleText;

	private DropdownAdapter language_adapter;

	private StatusPreferenceUpdate prefUpdate = new StatusPreferenceUpdate();

	/**
	 *
	 */
	public StatusPreferenceDialog() {
		setStyle(STYLE_NO_TITLE, R.style.StatusPrefDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_status, container, false);
		View statusVisibility = view.findViewById(R.id.dialog_status_visibility_container);
		View statusSpoiler = view.findViewById(R.id.dialog_status_spoiler_container);
		Button timePicker = view.findViewById(R.id.dialog_status_time_picker);
		Button okButton = view.findViewById(R.id.dialog_status_ok);
		Button cancelButton = view.findViewById(R.id.dialog_status_cancel);
		Spinner languageSelector = view.findViewById(R.id.dialog_status_language);
		Spinner visibilitySelector = view.findViewById(R.id.dialog_status_visibility);
		SwitchButton sensitiveCheck = view.findViewById(R.id.dialog_status_sensitive);
		SwitchButton spoilerCheck = view.findViewById(R.id.dialog_status_spoiler);
		scheduleText = view.findViewById(R.id.dialog_status_time_set);

		GlobalSettings settings = GlobalSettings.get(requireContext());
		DropdownAdapter visibility_adapter = new DropdownAdapter(requireContext());
		language_adapter = new DropdownAdapter(requireContext());

		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			boolean enable_extras = savedInstanceState.getBoolean(KEY_EXT);
			Object data = savedInstanceState.getSerializable(KEY_PREF);
			if (data instanceof StatusPreferenceUpdate) {
				prefUpdate = (StatusPreferenceUpdate) data;
			}
			if (!enable_extras) {
				scheduleText.setVisibility(View.GONE);
				timePicker.setVisibility(View.GONE);
				statusSpoiler.setVisibility(View.GONE);
			}
		}
		sensitiveCheck.setCheckedImmediately(prefUpdate.isSensitive());
		spoilerCheck.setCheckedImmediately(prefUpdate.isSpoiler());
		if (prefUpdate.getVisibility() == Status.VISIBLE_DEFAULT) {
			visibilitySelector.setSelection(IDX_VISIBILITY_DEFAULT, false);
		} else if (prefUpdate.getVisibility() == Status.VISIBLE_PUBLIC) {
			visibilitySelector.setSelection(IDX_VISIBILITY_PUBLIC, false);
		} else if (prefUpdate.getVisibility() == Status.VISIBLE_PRIVATE) {
			visibilitySelector.setSelection(IDX_VISIBILITY_PRIVATE, false);
		} else if (prefUpdate.getVisibility() == Status.VISIBLE_DIRECT) {
			visibilitySelector.setSelection(IDX_VISIBILITY_DIRECT, false);
		} else if (prefUpdate.getVisibility() == Status.VISIBLE_UNLISTED) {
			visibilitySelector.setSelection(IDX_VISIBILITY_UNLISTED, false);
		}
		if (!prefUpdate.getLanguage().isEmpty()) {
			// initialize language selector
			Map<String, String> languages = new TreeMap<>();
			languages.put("", "");
			Locale[] locales = Locale.getAvailableLocales();
			for (Locale locale : locales) {
				languages.put(locale.getDisplayLanguage(), locale.getLanguage());
			}
			String[] languageCodes = languages.values().toArray(new String[0]);
			language_adapter.setItems(languages.keySet().toArray(new String[0]));
			for (int i = 0; i < languageCodes.length; i++) {
				if (languageCodes[i].equals(prefUpdate.getLanguage())) {
					languageSelector.setSelection(i);
				}
			}
		}
		languageSelector.setAdapter(language_adapter);
		languageSelector.setSelection(0, false);
		languageSelector.setSelected(false);

		visibility_adapter.setItems(R.array.visibility);
		visibilitySelector.setAdapter(visibility_adapter);
		visibilitySelector.setSelection(0, false);
		visibilitySelector.setSelected(false);

		AppStyles.setTheme((ViewGroup) view, settings.getPopupColor());
		if (!settings.getLogin().getConfiguration().statusVisibilitySupported()) {
			statusVisibility.setVisibility(View.GONE);
		}
		if (!settings.getLogin().getConfiguration().statusSpoilerSupported()) {
			statusSpoiler.setVisibility(View.GONE);
		}

		sensitiveCheck.setOnCheckedChangeListener(this);
		spoilerCheck.setOnCheckedChangeListener(this);
		languageSelector.setOnItemSelectedListener(this);
		visibilitySelector.setOnItemSelectedListener(this);
		timePicker.setOnClickListener(this);
		okButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		return view;
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_status_time_picker) {
			TimePickerDialog.show(this, prefUpdate.getScheduleTime());
		} else if (v.getId() == R.id.dialog_status_ok) {
			if (getActivity() instanceof PreferenceSetCallback) {
				((PreferenceSetCallback)getActivity()).onPreferenceSet(prefUpdate);
			}
			dismiss();
		} else if (v.getId() == R.id.dialog_status_cancel) {
			if (getActivity() instanceof PreferenceSetCallback) {
				((PreferenceSetCallback)getActivity()).onPreferenceSet(null);
			}
			dismiss();
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.dialog_status_sensitive) {
			prefUpdate.setSensitive(isChecked);
		} else if (buttonView.getId() == R.id.dialog_status_spoiler) {
			prefUpdate.setSpoiler(isChecked);
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getId() == R.id.dialog_status_visibility) {
			int visibility;
			switch (position) {
				default:
				case IDX_VISIBILITY_DEFAULT:
					visibility = Status.VISIBLE_DEFAULT;
					break;

				case IDX_VISIBILITY_PUBLIC:
					visibility = Status.VISIBLE_PUBLIC;
					break;

				case IDX_VISIBILITY_PRIVATE:
					visibility = Status.VISIBLE_PRIVATE;
					break;

				case IDX_VISIBILITY_DIRECT:
					visibility = Status.VISIBLE_DIRECT;
					break;

				case IDX_VISIBILITY_UNLISTED:
					visibility = Status.VISIBLE_UNLISTED;
					break;
			}
			prefUpdate.setVisibility(visibility);
		} else if (parent.getId() == R.id.dialog_status_language) {
			prefUpdate.setLanguage(language_adapter.getItem(position));
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	@Override
	public void onTimeSelected(long time) {
		prefUpdate.setScheduleTime(time);
		if (time != 0L) {
			scheduleText.setText(new Date(time).toString());
		} else {
			scheduleText.setText("");
		}
	}

	/**
	 * show status preference dialog
	 *
	 * @param enableExtras true to enable extra features (used for status)
	 */
	public static void show(FragmentActivity activity, StatusPreferenceUpdate update, boolean enableExtras) {
		Fragment dialogFragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
		if (dialogFragment == null) {
			StatusPreferenceDialog dialog = new StatusPreferenceDialog();
			Bundle param = new Bundle();
			param.putSerializable(KEY_PREF, update);
			param.putBoolean(KEY_EXT, enableExtras);
			dialog.setArguments(param);
			dialog.show(activity.getSupportFragmentManager(), TAG);
		}
	}

	/**
	 *
	 */
	public interface PreferenceSetCallback {

		void onPreferenceSet(StatusPreferenceUpdate update);
	}
}