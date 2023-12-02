package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
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

import top.defaults.colorpicker.ColorObserver;
import top.defaults.colorpicker.ColorPickerView;

/**
 * Dialog to show a color selector
 *
 * @author nuclearfog
 */
public class ColorPickerDialog extends Dialog implements OnClickListener, ColorObserver, TextWatcher {

	private static final String KEY_COLOR = "color";
	private static final String KEY_MODE = "mode";

	private ColorPickerView colorPickerView;
	private EditText hexCode;
	private ViewGroup root;

	private OnColorSelectedListener listener;
	private int mode;

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
		colorPickerView.subscribe(this);
		confirm.setOnClickListener(this);
		cancel.setOnClickListener(this);
	}


	@NonNull
	@Override
	public Bundle onSaveInstanceState() {
		Bundle bundle = super.onSaveInstanceState();
		bundle.putInt(KEY_COLOR, colorPickerView.getColor());
		bundle.putInt(KEY_MODE, mode);
		return bundle;
	}


	@Override
	public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mode = savedInstanceState.getInt(KEY_MODE);
		colorPickerView.setInitialColor(savedInstanceState.getInt(KEY_COLOR));
	}


	@Override
	public void show() {
		// using show(int, int) instead
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_colorpicker_ok) {
			listener.onColorSelected(mode, colorPickerView.getColor());
			dismiss();
		} else if (v.getId() == R.id.dialog_colorpicker_cancel) {
			dismiss();
		}
	}


	@Override
	public void onColor(int color, boolean fromUser, boolean shouldPropagate) {
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
			this.mode = type;
			colorPickerView.setInitialColor(color);
			colorPickerView.setEnabledAlpha(enableAlpha);
			hexCode.setText(String.format("%08X", color));
			AppStyles.setTheme(root);
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
			if (hex.matches("[0123456789ABCDEFabcdef]{1,8}")) {
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