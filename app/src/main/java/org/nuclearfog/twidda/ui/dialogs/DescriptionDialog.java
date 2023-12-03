package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * Dialog used to add a description to a media object
 *
 * @author nuclearfog
 */
public class DescriptionDialog extends Dialog implements OnClickListener {

	private DescriptionCallback callback;
	private GlobalSettings settings;

	private EditText descriptionEdit;

	/**
	 *
	 */
	public DescriptionDialog(Activity activity, DescriptionCallback callback) {
		super(activity, R.style.DefaultDialog);
		settings = GlobalSettings.get(activity);
		this.callback = callback;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_description);
		ViewGroup root = findViewById(R.id.dialog_description_root);
		View applyButton = findViewById(R.id.dialog_description_apply);
		descriptionEdit = findViewById(R.id.dialog_description_input);
		AppStyles.setTheme(root, settings.getPopupColor());

		applyButton.setOnClickListener(this);
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
		if (v.getId() == R.id.dialog_description_apply) {
			String description = descriptionEdit.getText().toString();
			callback.onDescriptionSet(description);
			dismiss();
		}
	}

	/**
	 * callback interface used to send result back to activity
	 */
	public interface DescriptionCallback {

		/**
		 * called if a new description is set
		 */
		void onDescriptionSet(String description);
	}
}