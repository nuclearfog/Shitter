package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.StatusUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * status editor preference dialog used to set additional status information
 *
 * @author nuclearfog
 */
public class StatusPreferenceDialog extends Dialog implements OnCheckedChangeListener, OnItemSelectedListener {

	private StatusUpdate statusUpdate;

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
		Spinner visibilitySelector = findViewById(R.id.dialog_status_visibility);
		View statusVisibility = findViewById(R.id.dialog_status_visibility_container);
		View statusSpoiler = findViewById(R.id.dialog_status_spoiler_container);
		GlobalSettings settings = GlobalSettings.getInstance(context);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.visibility, android.R.layout.simple_spinner_dropdown_item);
		visibilitySelector.setAdapter(adapter);
		visibilitySelector.setSelection(0, false);
		visibilitySelector.setSelected(false);
		AppStyles.setTheme(rootView);
		if (!settings.getLogin().getConfiguration().statusVisibilitySupported()) {
			statusVisibility.setVisibility(View.GONE);
		}
		if (!settings.getLogin().getConfiguration().statusSpoilerSupported()) {
			statusSpoiler.setVisibility(View.GONE);
		}
		sensitiveCheck.setOnCheckedChangeListener(this);
		spoilerCheck.setOnCheckedChangeListener(this);
		visibilitySelector.setOnItemSelectedListener(this);
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
		if (parent.getId() == R.id.dialog_status_visibility) {
			switch (position) {
				case 0:
					statusUpdate.setVisibility(StatusUpdate.PUBLIC);
					break;

				case 1:
					statusUpdate.setVisibility(StatusUpdate.PRIVATE);
					break;

				case 2:
					statusUpdate.setVisibility(StatusUpdate.DIRECT);
					break;

				case 3:
					statusUpdate.setVisibility(StatusUpdate.UNLISTED);
					break;
			}
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}
}