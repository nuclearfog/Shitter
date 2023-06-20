package org.nuclearfog.twidda.ui.adapter.holder;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * ViewHolder implementation for {@link org.nuclearfog.twidda.ui.adapter.EditOptionsAdapter}
 *
 * @author nuclearfog
 */
public class EditOptionsHolder extends ViewHolder implements OnClickListener, TextWatcher {

	/**
	 * indicates that the item is locked and can't be removed
	 */
	public static final int STATE_LOCKED = 1;

	/**
	 * indicates that the item is activated & removable
	 */
	public static final int STATE_ACTIVE = 2;

	/**
	 * indicates that the item is disabled (placeholder)
	 */
	public static final int STATE_DISABLED = 3;


	private EditText option_name;
	private ImageButton option_button;

	private OnOptionChangedListener listener;
	private GlobalSettings settings;

	private int state = STATE_LOCKED;

	/**
	 *
	 */
	public EditOptionsHolder(ViewGroup parent, OnOptionChangedListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option_edit, parent, false));
		settings = GlobalSettings.get(parent.getContext());
		this.listener = listener;

		option_name = itemView.findViewById(R.id.item_option_edit_name);
		option_button = itemView.findViewById(R.id.item_option_edit_action);
		AppStyles.setTheme((ViewGroup) itemView);

		option_button.setOnClickListener(this);
		option_name.addTextChangedListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != RecyclerView.NO_POSITION) {
			if (v.getId() == R.id.item_option_edit_action) {
				switch (state) {
					case STATE_ACTIVE:
						listener.onOptionRemove(position);
						option_button.setImageResource(R.drawable.add);
						AppStyles.setButtonColor(option_button, settings.getIconColor());
						option_name.setEnabled(true);
						break;

					case STATE_DISABLED:
						listener.onOptionAdd(position);
						AppStyles.setButtonColor(option_button, settings.getIconColor());
						option_name.setEnabled(false);
						break;
				}
			}
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
		int position = getLayoutPosition();
		if (position != RecyclerView.NO_POSITION && option_name.hasFocus()) {
			listener.OnOptionChange(position, s.toString());
		}
	}

	/**
	 * set option description
	 *
	 * @param description descrition of the option
	 */
	public void setDescription(String description) {
		option_name.setText(description);
	}

	/**
	 * set option state and option hint
	 *
	 * @param position position of the item
	 * @param state    state to set {@link #STATE_LOCKED,#STATE_ACTIVE,#STATE_DISABLED}
	 */
	public void setState(int position, int state) {
		this.state = state;
		switch (state) {
			case STATE_LOCKED:
				option_name.setEnabled(true);
				option_button.setImageResource(R.drawable.circle);
				break;

			case STATE_ACTIVE:
				option_name.setEnabled(true);
				option_button.setImageResource(R.drawable.cross);
				break;

			case STATE_DISABLED:
				option_name.setEnabled(false);
				option_button.setImageResource(R.drawable.add);
				break;
		}
		AppStyles.setButtonColor(option_button, settings.getIconColor());
		String hint = option_name.getContext().getString(R.string.dialog_poll_option_edit_hint, position + 1);
		option_name.setHint(hint);
	}

	/**
	 * listener for option changes
	 */
	public interface OnOptionChangedListener {

		/**
		 * called when an option is added
		 *
		 * @param position position where to insert the new item
		 */
		void onOptionAdd(int position);

		/**
		 * called when an option is removed
		 *
		 * @param position position of the old item
		 */
		void onOptionRemove(int position);

		/**
		 * called when the option name changes
		 *
		 * @param position position of the item
		 * @param name     new option name
		 */
		void OnOptionChange(int position, String name);
	}
}