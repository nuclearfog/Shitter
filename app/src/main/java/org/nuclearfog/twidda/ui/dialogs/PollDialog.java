package org.nuclearfog.twidda.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.update.PollUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Instance;
import org.nuclearfog.twidda.ui.adapter.listview.DropdownAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.EditOptionsAdapter;

import java.util.List;

/**
 * Dialog class used to create/edit a poll
 *
 * @author nuclearfog
 */
public class PollDialog extends DialogFragment implements OnClickListener {

	/**
	 *
	 */
	private static final String TAG = "PolDialog";

	/**
	 * Bundle key used to set/restore poll information
	 * value type is {@link PollUpdate}
	 */
	private static final String KEY_POLL = "pollupdate-data";

	/**
	 * Bundle key used to set/restore instance information
	 * value type is {@link Instance}
	 */
	private static final String KEY_INSTANCE = "pollupdate-instance";

	private EditOptionsAdapter optionAdapter;

	private SwitchButton multiple_choice, hide_votes;
	private Spinner timeUnitSelector;
	private EditText durationInput;

	@Nullable
	private Instance instance;
	private PollUpdate pollUpdate = new PollUpdate();

	/**
	 *
	 */
	public PollDialog() {
		setStyle(STYLE_NO_TITLE, R.style.DefaultDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_poll, container, false);
		RecyclerView optionsList = view.findViewById(R.id.dialog_poll_option_list);
		Button confirm = view.findViewById(R.id.dialog_poll_create);
		Button remove = view.findViewById(R.id.dialog_poll_remove);
		View close = view.findViewById(R.id.dialog_poll_close);
		durationInput = view.findViewById(R.id.dialog_poll_duration_input);
		timeUnitSelector = view.findViewById(R.id.dialog_poll_duration_timeunit);
		multiple_choice = view.findViewById(R.id.dialog_poll_mul_choice);
		hide_votes = view.findViewById(R.id.dialog_poll_hide_total);

		optionAdapter = new EditOptionsAdapter();
		DropdownAdapter timeUnitAdapter = new DropdownAdapter(requireContext());
		timeUnitAdapter.setItems(R.array.timeunits);
		GlobalSettings settings = GlobalSettings.get(requireContext());

		optionsList.setAdapter(optionAdapter);
		timeUnitSelector.setAdapter(timeUnitAdapter);
		timeUnitSelector.setSelection(2);
		AppStyles.setTheme((ViewGroup) view, settings.getPopupColor());

		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			Object pollData = savedInstanceState.getSerializable(KEY_POLL);
			Object instanceData = savedInstanceState.getSerializable(KEY_INSTANCE);
			if (pollData instanceof PollUpdate) {
				pollUpdate = (PollUpdate) pollData;
			}
			if (instanceData instanceof Instance) {
				instance = (Instance) instanceData;
			}
		}
		optionAdapter.setItems(pollUpdate.getOptions());
		multiple_choice.setCheckedImmediately(pollUpdate.multipleChoiceEnabled());
		hide_votes.setCheckedImmediately(pollUpdate.hideTotalVotes());
		if (pollUpdate.getDuration() >= 86400) {
			durationInput.setText(Integer.toString(Math.round(pollUpdate.getDuration() / 86400.0f)));
			timeUnitSelector.setSelection(2);
		} else if (pollUpdate.getDuration() >= 3600) {
			durationInput.setText(Integer.toString(Math.round(pollUpdate.getDuration() / 3600.0f)));
			timeUnitSelector.setSelection(1);
		} else if (pollUpdate.getDuration() >= 60) {
			durationInput.setText(Integer.toString(Math.round(pollUpdate.getDuration() / 60.0f)));
			timeUnitSelector.setSelection(0);
		}

		confirm.setOnClickListener(this);
		remove.setOnClickListener(this);
		close.setOnClickListener(this);
		return view;
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_POLL, pollUpdate);
		super.onSaveInstanceState(outState);
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
			} else if (pollUpdate != null) {
				List<String> options = optionAdapter.getItems();
				for (String option : options) {
					if (option.trim().isEmpty()) {
						Toast.makeText(getContext(), R.string.error_poll_option_missing, Toast.LENGTH_SHORT).show();
						return;
					}
				}
				pollUpdate.setDuration(duration);
				pollUpdate.setMultipleChoice(multiple_choice.isChecked());
				pollUpdate.hideVotes(hide_votes.isChecked());
				pollUpdate.setOptions(optionAdapter.getItems());
				if (getActivity() instanceof PollUpdateCallback) {
					((PollUpdateCallback) getActivity()).onPollUpdate(pollUpdate);
				}
				dismiss();
			}
		} else if (v.getId() == R.id.dialog_poll_remove) {
			if (getActivity() instanceof PollUpdateCallback) {
				((PollUpdateCallback) getActivity()).onPollUpdate(null);
			}
			dismiss();
		} else if (v.getId() == R.id.dialog_poll_close) {
			dismiss();
		}
	}

	/**
	 * show dialog
	 *
	 * @param poll previous poll information if any
	 */
	public static void show(FragmentActivity activity, @Nullable PollUpdate poll, @Nullable Instance instance) {
		Fragment dialogFragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
		if (dialogFragment == null) {
			PollDialog dialog = new PollDialog();
			Bundle args = new Bundle();
			args.putSerializable(KEY_POLL, poll);
			args.putSerializable(KEY_INSTANCE, instance);
			dialog.setArguments(args);
			dialog.show(activity.getSupportFragmentManager(), TAG);
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