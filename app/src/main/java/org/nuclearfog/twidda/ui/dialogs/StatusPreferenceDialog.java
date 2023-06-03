package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.update.StatusUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.adapter.DropdownAdapter;

import java.util.Locale;

/**
 * status editor preference dialog used to set additional status information
 *
 * @author nuclearfog
 */
public class StatusPreferenceDialog extends Dialog implements OnCheckedChangeListener, OnItemSelectedListener {

	private Spinner visibilitySelector;

	private StatusUpdate statusUpdate;

	private Locale[] languages;

	/**
	 * @param statusUpdate status information from status editor
	 */
	public StatusPreferenceDialog(Context context, StatusUpdate statusUpdate) {
		super(context, R.style.StatusDialog);
		this.statusUpdate = statusUpdate;
		setContentView(R.layout.dialog_status);
		ViewGroup rootView = findViewById(R.id.dialog_status_root);
		SwitchButton sensitiveCheck = findViewById(R.id.dialog_status_sensitive);
		SwitchButton spoilerCheck = findViewById(R.id.dialog_status_spoiler);
		View statusVisibility = findViewById(R.id.dialog_status_visibility_container);
		View statusSpoiler = findViewById(R.id.dialog_status_spoiler_container);
		Spinner languageSelector = findViewById(R.id.dialog_status_language);
		visibilitySelector = findViewById(R.id.dialog_status_visibility);
		GlobalSettings settings = GlobalSettings.getInstance(context);
		AppStyles.setTheme(rootView);

		DropdownAdapter visibility_adapter = new DropdownAdapter(context);
		DropdownAdapter language_adapter = new DropdownAdapter(context);
		languageSelector.setAdapter(language_adapter);
		languageSelector.setSelected(false);
		visibilitySelector.setAdapter(visibility_adapter);
		visibilitySelector.setSelection(0, false);
		visibilitySelector.setSelected(false);

		languages = Locale.getAvailableLocales();
		String[] language_names = new String[languages.length + 1];
		language_names[0] = context.getString(R.string.dialog_status_language_empty);
		for (int i = 0 ; i < languages.length ; i++) {
			language_names[i + 1] = languages[i].getDisplayLanguage() + " " + languages[i].getCountry();
		}
		language_adapter.setItems(language_names);

		if (!settings.getLogin().getConfiguration().statusVisibilitySupported()) {
			statusVisibility.setVisibility(View.GONE);
		}
		if (!settings.getLogin().getConfiguration().statusSpoilerSupported()) {
			statusSpoiler.setVisibility(View.GONE);
		}
		visibility_adapter.setItems(R.array.visibility);
		sensitiveCheck.setOnCheckedChangeListener(this);
		spoilerCheck.setOnCheckedChangeListener(this);
		languageSelector.setOnItemSelectedListener(this);
		visibilitySelector.setOnItemSelectedListener(this);
	}


	@Override
	public void show() {
		switch (statusUpdate.getVisibility()) {
			case Status.VISIBLE_PUBLIC:
				visibilitySelector.setSelection(0, false);
				break;

			case Status.VISIBLE_PRIVATE:
				visibilitySelector.setSelection(1, false);
				break;

			case Status.VISIBLE_DIRECT:
				visibilitySelector.setSelection(2, false);
				break;

			case Status.VISIBLE_UNLISTED:
				visibilitySelector.setSelection(3, false);
				break;
		}
		super.show();
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.dialog_status_sensitive) {
			statusUpdate.setSensitive(isChecked);
		} else if (buttonView.getId() == R.id.dialog_status_spoiler) {
			statusUpdate.setSpoiler(isChecked);
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (!isShowing())
			return;
		if (parent.getId() == R.id.dialog_status_visibility) {
			switch (position) {
				case 0:
					statusUpdate.setVisibility(Status.VISIBLE_PUBLIC);
					break;

				case 1:
					statusUpdate.setVisibility(Status.VISIBLE_PRIVATE);
					break;

				case 2:
					statusUpdate.setVisibility(Status.VISIBLE_DIRECT);
					break;

				case 3:
					statusUpdate.setVisibility(Status.VISIBLE_UNLISTED);
					break;
			}
		} else if (parent.getId() == R.id.dialog_status_language) {
			if (position > 0) {
				Locale language = languages[position - 1];
				statusUpdate.addLanguage(language.getLanguage());
			}
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}
}