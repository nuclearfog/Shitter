package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;


public class DescriptionDialog extends Dialog implements OnClickListener {

	private static final String KEY_SAVE = " description-save";

	private DescriptionCallback callback;

	private EditText descriptionEdit;


	public DescriptionDialog(Activity activity, DescriptionCallback callback) {
		super(activity, R.style.DefaultDialog);
		this.callback = callback;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_description);
		ViewGroup root = findViewById(R.id.dialog_description_root);
		View applyButton = findViewById(R.id.dialog_description_apply);
		descriptionEdit = findViewById(R.id.dialog_description_input);
		AppStyles.setTheme(root);

		applyButton.setOnClickListener(this);
	}


	@NonNull
	@Override
	public Bundle onSaveInstanceState() {
		String description = descriptionEdit.getText().toString();
		Bundle bundle = super.onSaveInstanceState();
		bundle.putString(KEY_SAVE, description);
		return bundle;
	}


	@Override
	public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		String description = savedInstanceState.getString(KEY_SAVE, "");
		descriptionEdit.setText(description);
		super.onRestoreInstanceState(savedInstanceState);
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


	public interface DescriptionCallback {

		void onDescriptionSet(String description);
	}
}