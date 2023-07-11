package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.update.PollUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.model.Instance;
import org.nuclearfog.twidda.ui.adapter.recyclerview.DropdownAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.EditOptionsAdapter;

import java.io.Serializable;
import java.util.List;

/**
 * Dialog class used to show poll editor
 *
 * @author nuclearfog
 */
public class PollDialog extends Dialog implements OnClickListener {

	private static final String KEY_SAVE = "pollupdate-save";

	private EditOptionsAdapter optionAdapter;
	private DropdownAdapter timeUnitAdapter;
	private PollUpdateCallback callback;

	private SwitchButton multiple_choice, hide_votes;
	private Spinner timeUnitSelector;
	private EditText durationInput;

	@Nullable
	private Instance instance;
	private PollUpdate poll;

	/**
	 *
	 */
	public PollDialog(Activity activity, PollUpdateCallback callback) {
		super(activity, R.style.DefaultDialog);
		this.callback = callback;
		optionAdapter = new EditOptionsAdapter();
		timeUnitAdapter = new DropdownAdapter(activity.getApplicationContext());
		timeUnitAdapter.setItems(R.array.timeunits);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_poll);
		ViewGroup root = findViewById(R.id.dialog_poll_root);
		RecyclerView optionsList = findViewById(R.id.dialog_poll_option_list);
		Button confirm = findViewById(R.id.dialog_poll_create);
		View close = findViewById(R.id.dialog_poll_close);
		durationInput = findViewById(R.id.dialog_poll_duration_input);
		timeUnitSelector = findViewById(R.id.dialog_poll_duration_timeunit);
		multiple_choice = findViewById(R.id.dialog_poll_mul_choice);
		hide_votes = findViewById(R.id.dialog_poll_hide_total);

		optionsList.setAdapter(optionAdapter);
		timeUnitSelector.setAdapter(timeUnitAdapter);
		timeUnitSelector.setSelection(2);
		AppStyles.setTheme(root);

		confirm.setOnClickListener(this);
		close.setOnClickListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		optionAdapter.replaceItems(poll.getOptions());
		multiple_choice.setCheckedImmediately(poll.multipleChoiceEnabled());
		hide_votes.setCheckedImmediately(poll.hideTotalVotes());
		if (poll.getDuration() > 86400000L) {
			durationInput.setText(Long.toString(Math.round(poll.getDuration() / 86400000d)));
			timeUnitSelector.setSelection(2);
		} else if (poll.getDuration() > 3600000L) {
			durationInput.setText(Long.toString(Math.round(poll.getDuration() / 3600000d)));
			timeUnitSelector.setSelection(1);
		} else if (poll.getDuration() > 60000L) {
			durationInput.setText(Long.toString(Math.round(poll.getDuration() / 60000d)));
			timeUnitSelector.setSelection(0);
		}
	}


	@NonNull
	@Override
	public Bundle onSaveInstanceState() {
		Bundle bundle = super.onSaveInstanceState();
		bundle.putSerializable(KEY_SAVE, poll);
		return bundle;
	}


	@Override
	public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		Serializable data = savedInstanceState.getSerializable(KEY_SAVE);
		if (data instanceof PollUpdate) {
			poll = (PollUpdate) data;
		}
		super.onRestoreInstanceState(savedInstanceState);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_poll_create) {
			String durationStr = durationInput.getText().toString();
			int duration;
			if (durationStr.matches("\\d{1,3}")) {
				duration = Integer.parseInt(durationStr);
			} else {
				duration = 1;
			}
			if (timeUnitSelector.getSelectedItemPosition() == 0)
				duration *= 60;
			else if (timeUnitSelector.getSelectedItemPosition() == 1)
				duration *= 3600;
			else if (timeUnitSelector.getSelectedItemPosition() == 2)
				duration *= 86400;
			if (instance != null && duration < instance.getMinPollDuration()) {
				Toast.makeText(getContext(), R.string.error_duration_time_low, Toast.LENGTH_SHORT).show();
			} else if (instance != null && duration > instance.getMaxPollDuration()) {
				Toast.makeText(getContext(), R.string.error_duration_time_high, Toast.LENGTH_SHORT).show();
			} else if (poll != null) {
				List<String> options = optionAdapter.getItems();
				for (String option : options) {
					if (option.trim().isEmpty()) {
						Toast.makeText(getContext(), R.string.error_poll_option_missing, Toast.LENGTH_SHORT).show();
						return;
					}
				}
				poll.setDuration(duration);
				poll.setMultipleChoice(multiple_choice.isChecked());
				poll.hideVotes(hide_votes.isChecked());
				poll.setOptions(optionAdapter.getItems());
				callback.onPollUpdate(poll);
				dismiss();
			}
		} else if (v.getId() == R.id.dialog_poll_close) {
			dismiss();
		}
	}


	@Override
	public void show() {
		// using show(PollUpdate) instead
	}


	@Override
	public void dismiss() {
		if (isShowing()) {
			super.dismiss();
		}
	}

	/**
	 * show dialog
	 *
	 * @param poll previous poll information if any
	 */
	public void show(@Nullable PollUpdate poll) {
		if (!isShowing()) {
			if (poll != null) {
				this.poll = poll;
			} else {
				this.poll = new PollUpdate();
			}
			super.show();
		}
	}

	/**
	 * set instance information
	 */
	public void setInstance(@Nullable Instance instance) {
		this.instance = instance;
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