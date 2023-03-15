package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.PollUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.ui.adapter.EditOptionsAdapter;

/**
 * Dialog class used to show poll editor
 *
 * @author nuclearfog
 */
public class PollDialog extends Dialog implements OnClickListener {

	private EditOptionsAdapter optionAdapter;
	private SwitchButton multiple_choice, hide_votes;
	private Spinner timeUnitSelector;
	private EditText durationInput;

	private PollUpdateCallback callback;
	private PollUpdate poll;

	/**
	 *
	 */
	public PollDialog(@NonNull Context context, PollUpdateCallback callback) {
		super(context, R.style.PollDialog);
		this.callback = callback;
		setContentView(R.layout.dialog_poll);
		ViewGroup root = findViewById(R.id.dialog_poll_root);
		RecyclerView optionsList = findViewById(R.id.dialog_poll_option_list);
		Button confirm = findViewById(R.id.dialog_poll_create);
		View close = findViewById(R.id.dialog_poll_close);
		durationInput = findViewById(R.id.dialog_poll_duration_input);
		timeUnitSelector = findViewById(R.id.dialog_poll_duration_timeunit);
		multiple_choice = findViewById(R.id.dialog_poll_mul_choice);
		hide_votes = findViewById(R.id.dialog_poll_hide_total);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.timeunits, android.R.layout.simple_spinner_dropdown_item);
		timeUnitSelector.setAdapter(adapter);
		timeUnitSelector.setSelection(2);

		optionAdapter = new EditOptionsAdapter();
		optionsList.setAdapter(optionAdapter);
		AppStyles.setTheme(root);

		confirm.setOnClickListener(this);
		close.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_poll_create) {
			String durationStr = durationInput.getText().toString();
			int secondsDuration;
			if (durationStr.matches("\\d{1,3}")) {
				secondsDuration = Integer.parseInt(durationStr);
			} else {
				secondsDuration = 1;
			}
			switch (timeUnitSelector.getSelectedItemPosition()) {
				// minutes
				case 0:
					poll.setDuration(secondsDuration * 60);
					break;

				// hours
				case 1:
					poll.setDuration(secondsDuration * 3600);
					break;

				// days
				case 2:
					poll.setDuration(secondsDuration * 86400);
					break;
			}
			poll.setMultipleChoice(multiple_choice.isChecked());
			poll.hideVotes(hide_votes.isChecked());
			poll.setOptions(optionAdapter.getOptions());
			callback.onPollUpdate(poll);
			dismiss();
		} else if (v.getId() == R.id.dialog_poll_close) {
			dismiss();
		}
	}


	@Override
	public void show() {
	}

	/**
	 * show dialog
	 *
	 * @param poll previous poll information if any
	 */
	public void show(@Nullable PollUpdate poll) {
		if (!isShowing()) {
			if (poll != null) {
				optionAdapter.setOptions(poll.getOptions());
				multiple_choice.setCheckedImmediately(poll.multipleChoiceEnabled());
				hide_votes.setCheckedImmediately(poll.hideTotalVotes());
				this.poll = poll;
			} else {
				this.poll = new PollUpdate();
			}
			super.show();
		}
	}

	/**
	 * callback used to return poll information
	 */
	public interface PollUpdateCallback {

		/**
		 * @param update updated poll information
		 */
		void onPollUpdate(PollUpdate update);
	}
}