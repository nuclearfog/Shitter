package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;

import java.util.regex.Pattern;

import top.defaults.colorpicker.ColorObserver;
import top.defaults.colorpicker.ColorPickerView;

/**
 * Dialog to show a color selector
 *
 * @author nuclearfog
 */
public class ColorPickerDialog extends Dialog implements OnClickListener, ColorObserver, TextWatcher {

	/**
	 * bundle key to save/restore color value
	 * value type is Integer
	 */
	private static final String KEY_COLOR = "color";

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
	private EditText hexCode;
	private ViewGroup root;

	private OnColorSelectedListener listener;
	private int type;

	/**
	 * @param listener callback listener to set color
	 */
	public ColorPickerDialog(Activity activity, OnColorSelectedListener listener) {
		super(activity, R.style.DefaultDialog);
		this.listener = listener;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_colorpicker);
		colorPickerView = findViewById(R.id.dialog_colorpicker_selector);
		root = findViewById(R.id.dialog_colorpicker_root);
		hexCode = findViewById(R.id.dialog_colorpicker_hex);
		View confirm = findViewById(R.id.dialog_colorpicker_ok);
		View cancel = findViewById(R.id.dialog_colorpicker_cancel);

		hexCode.addTextChangedListener(this);
		confirm.setOnClickListener(this);
		cancel.setOnClickListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		colorPickerView.subscribe(this);
	}


	@Override
	protected void onStop() {
		super.onStop();
		colorPickerView.unsubscribe(this);
	}


	@NonNull
	@Override
	public Bundle onSaveInstanceState() {
		Bundle bundle = super.onSaveInstanceState();
		int color = colorPickerView.getColor();
		bundle.putInt(KEY_COLOR, color);
		bundle.putInt(KEY_TYPE, type);
		return bundle;
	}


	@Override
	public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int color = savedInstanceState.getInt(KEY_COLOR);
		type = savedInstanceState.getInt(KEY_TYPE);
		colorPickerView.setInitialColor(color);
	}


	@Override
	public void show() {
		// using show(int, int) instead
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_colorpicker_ok) {
			listener.onColorSelected(type, colorPickerView.getColor());
			dismiss();
		} else if (v.getId() == R.id.dialog_colorpicker_cancel) {
			dismiss();
		}
	}


	@Override
	public void onColor(int color, boolean fromUser, boolean shouldPropagate) {
		// only handle user input
		if (fromUser) {
			hexCode.setText(String.format("%08X", color));
		}
	}

	/**
	 * @param color       start color
	 * @param type        color type
	 * @param enableAlpha true to enable alpha slider
	 */
	public void show(int color, int type, boolean enableAlpha) {
		if (!isShowing()) {
			super.show();
			this.type = type;
			colorPickerView.setInitialColor(color);
			colorPickerView.setEnabledAlpha(enableAlpha);
			hexCode.setText(String.format("%08X", color));
			AppStyles.setTheme(root);
			hexCode.setTypeface(Typeface.MONOSPACE);
		}
	}


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}


	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}


	@Override
	public void afterTextChanged(Editable s) {
		// only handle user input
		if (hexCode.hasFocus()) {
			String hex = s.toString();
			if (HEX_PATTERN.matcher(hex).matches()) {
				colorPickerView.setInitialColor(Integer.parseUnsignedInt(hex, 16));
			}
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