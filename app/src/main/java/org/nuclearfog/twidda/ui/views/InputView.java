package org.nuclearfog.twidda.ui.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * Custom EditText implementation with a text change listener
 *
 * @author nuclearfog
 */
public class InputView extends AppCompatEditText implements TextWatcher {

	@Nullable
	private OnTextChangeListener listener;

	/**
	 *
	 */
	public InputView(Context context) {
		this(context, null);
	}

	/**
	 *
	 */
	public InputView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}


	@Override
	public void afterTextChanged(Editable s) {
		if (listener != null) {
			listener.onTextChanged(this, s.toString());
		}
	}


	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}


	@Override
	public void addTextChangedListener(TextWatcher watcher) {
	}

	/**
	 * get text
	 *
	 * @return text string
	 */
	public String getInput() {
		Editable e = getText();
		if (e != null)
			return e.toString();
		return "";
	}

	/**
	 *
	 */
	public void setOnTextChangeListener(@Nullable OnTextChangeListener listener) {
		this.listener = listener;
	}

	/**
	 *
	 */
	public interface OnTextChangeListener {

		/**
		 * called after text changes
		 */
		void onTextChanged(InputView inputView, String text);
	}
}