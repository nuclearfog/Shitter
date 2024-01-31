package org.nuclearfog.twidda.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * Dialog used to add a description to a media object
 *
 * @author nuclearfog
 */
public class DescriptionDialog extends DialogFragment implements OnClickListener {

	/**
	 *
	 */
	private static final String TAG = "DescriptionDialog";

	/**
	 * bundle key used to restore description
	 * value type is String
	 */
	private static final String KEY_DESCR ="description_save";

	private EditText descriptionEdit;

	/**
	 *
	 */
	public DescriptionDialog() {
		setStyle(STYLE_NO_TITLE, R.style.DefaultDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_description, container, false);
		View applyButton = view.findViewById(R.id.dialog_description_apply);
		descriptionEdit = view.findViewById(R.id.dialog_description_input);
		GlobalSettings settings = GlobalSettings.get(requireContext());

		if (savedInstanceState != null) {
			String description = savedInstanceState.getString(KEY_DESCR, "");
			descriptionEdit.setText(description);
		}
		AppStyles.setTheme((ViewGroup) view, settings.getPopupColor());

		applyButton.setOnClickListener(this);
		return view;
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putString(KEY_DESCR, descriptionEdit.getText().toString());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_description_apply) {
			String description = descriptionEdit.getText().toString();
			if (getActivity() instanceof DescriptionCallback) {
				((DescriptionCallback) getActivity()).onDescriptionSet(description);
			}
			dismiss();
		}
	}

	/**
	 * show description dialog
	 *
	 * @param activity activity from which to show the dialog
	 */
	public static void show(FragmentActivity activity) {
		Fragment dialogFragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
		if (dialogFragment == null) {
			DescriptionDialog dialog = new DescriptionDialog();
			dialog.show(activity.getSupportFragmentManager(), TAG);
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