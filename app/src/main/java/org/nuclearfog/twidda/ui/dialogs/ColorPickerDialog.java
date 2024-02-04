package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.ui.views.InputView;
import org.nuclearfog.twidda.ui.views.InputView.OnTextChangeListener;

import java.util.regex.Pattern;

import top.defaults.colorpicker.ColorObserver;
import top.defaults.colorpicker.ColorPickerView;

/**
 * Dialog to show a color selector
 *
 * @author nuclearfog
 */
public class ColorPickerDialog extends DialogFragment implements OnClickListener, OnTextChangeListener, ColorObserver {

	/**
	 *
	 */
	private static final String TAG = "ColorPickerDialog";

	/**
	 * bundle key to save/restore color value
	 * value type is Integer
	 */
	private static final String KEY_COLOR = "color";

	/**
	 * bundle key to save/restore alpha state
	 * value type is boolean
	 */
	private static final String KEY_ALPHA = "alpha";

	/**
	 * bundle key to save/restore color type
	 * value type is Integer
	 */
	private static final String KEY_TYPE = "mode";

	/**
	 * pattern used to check rgb hex input
	 */
	private static final Pattern HEX_PATTERN = Pattern.compile("[0123456789ABCDEFabcdef]{1,8}");

	private ColorPickerView colorPickerView;
	private InputView hecCodeInput;

	private boolean enableAlpha;
	private int colorType;

	/**
	 *
	 */
	public ColorPickerDialog() {
		setStyle(STYLE_NO_TITLE, R.style.DefaultDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.dialog_colorpicker, container, false);
		colorPickerView = root.findViewById(R.id.dialog_colorpicker_selector);
		root = root.findViewById(R.id.dialog_colorpicker_root);
		hecCodeInput = root.findViewById(R.id.dialog_colorpicker_hex);
		View confirm = root.findViewById(R.id.dialog_colorpicker_ok);
		View cancel = root.findViewById(R.id.dialog_colorpicker_cancel);

		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			int color = savedInstanceState.getInt(KEY_COLOR);
			enableAlpha = savedInstanceState.getBoolean(KEY_ALPHA);
			colorType = savedInstanceState.getInt(KEY_TYPE);
			colorPickerView.setInitialColor(color);
			hecCodeInput.setText(String.format("%08X", color));
			colorPickerView.setEnabledAlpha(enableAlpha);
		}
		AppStyles.setTheme((ViewGroup) root);
		hecCodeInput.setTypeface(Typeface.MONOSPACE);

		hecCodeInput.setOnTextChangeListener(this);
		confirm.setOnClickListener(this);
		cancel.setOnClickListener(this);
		colorPickerView.subscribe(this);
		return root;
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		int color = colorPickerView.getColor();
		outState.putInt(KEY_COLOR, color);
		outState.putInt(KEY_TYPE, colorType);
		outState.putBoolean(KEY_ALPHA, enableAlpha);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onDestroyView() {
		colorPickerView.unsubscribe(this);
		super.onDestroyView();
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_colorpicker_ok) {
			Activity activity = getActivity();
			if (activity instanceof OnColorSelectedListener)
				((OnColorSelectedListener) activity).onColorSelected(colorType, colorPickerView.getColor());
			dismiss();
		} else if (v.getId() == R.id.dialog_colorpicker_cancel) {
			dismiss();
		}
	}


	@Override
	public void onColor(int color, boolean fromUser, boolean shouldPropagate) {
		// only handle user input
		if (fromUser) {
			hecCodeInput.setText(String.format("%08X", color));
		}
	}


	@Override
	public void onTextChanged(InputView inputView, String text) {
		if (inputView.getId() == R.id.dialog_colorpicker_hex) {
			if (HEX_PATTERN.matcher(text).matches()) {
				int color = Integer.parseUnsignedInt(text, 16);
				if (!enableAlpha)
					color |= 0xff000000;
				colorPickerView.setInitialColor(color);
			}
		}
	}

	/**
	 * set color picker values and show dialog
	 *
	 * @param activity    parent activity
	 * @param color       predefined color
	 * @param type        type of color
	 * @param enableAlpha true to enable alpha slider
	 */
	public static void show(FragmentActivity activity, int color, int type, boolean enableAlpha) {
		Fragment dialogFragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
		if (dialogFragment == null) {
			ColorPickerDialog dialog = new ColorPickerDialog();
			Bundle args = new Bundle();
			args.putInt(KEY_COLOR, color);
			args.putInt(KEY_TYPE, type);
			args.putBoolean(KEY_ALPHA, enableAlpha);
			dialog.setArguments(args);
			dialog.show(activity.getSupportFragmentManager(), TAG);
		}
	}

	/**
	 * callback listener to set selected color
	 */
	public interface OnColorSelectedListener {

		/**
		 * called to set selected color
		 *
		 * @param type  color type
		 * @param color color value
		 */
		void onColorSelected(int type, int color);
	}
}