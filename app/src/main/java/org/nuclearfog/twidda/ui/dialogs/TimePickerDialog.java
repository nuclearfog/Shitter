package org.nuclearfog.twidda.ui.dialogs;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.joda.time.DateTime;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Dialog used to show a date and time picker
 *
 * @author nuclearfog
 */
public class TimePickerDialog extends DialogFragment implements OnClickListener {

	/**
	 *
	 */
	private static final String TAG = "TimePickerDialog";

	/**
	 * Bundle key used to set/restore selected time
	 * value type is long
	 */
	private static final String KEY_TIME = "picker_time";

	private TimePicker timePicker;
	private DatePicker datePicker;

	/**
	 *
	 */
	public TimePickerDialog() {
		setStyle(STYLE_NO_TITLE, R.style.DefaultDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_timepicker, container, false);
		datePicker = view.findViewById(R.id.dialog_timepicker_date);
		timePicker = view.findViewById(R.id.dialog_timepicker_time);
		Button confirm = view.findViewById(R.id.dialog_timepicker_confirm);
		Button cancel = view.findViewById(R.id.dialog_timepicker_remove);

		GlobalSettings settings = GlobalSettings.get(requireContext());
		AppStyles.setTheme((ViewGroup) view, settings.getPopupColor());
		timePicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
		datePicker.setFirstDayOfWeek(Calendar.getInstance().getFirstDayOfWeek());

		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			long time = savedInstanceState.getLong(KEY_TIME);
			DateTime selectedTime;
			if (time != 0L) {
				selectedTime = new DateTime(time);
			} else {
				selectedTime = new DateTime();
			}
			datePicker.updateDate(selectedTime.getYear(), selectedTime.getMonthOfYear() - 1, selectedTime.getDayOfMonth());
			timePicker.setCurrentHour(selectedTime.getHourOfDay());
			timePicker.setCurrentMinute(selectedTime.getMinuteOfHour());
		}

		confirm.setOnClickListener(this);
		cancel.setOnClickListener(this);
		return view;
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		GregorianCalendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(),
				datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
		Date selectedDate = calendar.getTime();
		outState.putLong(KEY_TIME, selectedDate.getTime());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_timepicker_confirm) {
			if (timePicker.getVisibility() == View.INVISIBLE) {
				datePicker.setVisibility(View.INVISIBLE);
				timePicker.setVisibility(View.VISIBLE);
			} else {
				GregorianCalendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(),
						datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
				Date selectedDate = calendar.getTime();
				if (getParentFragment() instanceof TimeSelectedCallback) {
					((TimeSelectedCallback) getParentFragment()).onTimeSelected(selectedDate.getTime());
				}
				dismiss();
			}
		} else if (v.getId() == R.id.dialog_timepicker_remove) {
			if (getParentFragment() instanceof TimeSelectedCallback) {
				((TimeSelectedCallback) getParentFragment()).onTimeSelected(0L);
			}
			dismiss();
		}
	}

	/**
	 * show timepicker with default value
	 *
	 * @param time selected time or '0' to select the current time
	 */
	public static void show(Fragment fragment, long time) {
		if (fragment.isAdded()) {
			show(fragment.getChildFragmentManager(), time);
		}
	}

	/**
	 * show timepicker with default value
	 *
	 * @param time selected time or '0' to select the current time
	 */
	public static void show(FragmentActivity activity, long time) {
		show(activity.getSupportFragmentManager(), time);
	}

	/**
	 *
	 */
	private static void show(FragmentManager fm, long time) {
		Fragment dialogFragment = fm.findFragmentByTag(TAG);
		if (dialogFragment == null) {
			TimePickerDialog dialog = new TimePickerDialog();
			Bundle param = new Bundle();
			param.putLong(KEY_TIME, time);
			dialog.setArguments(param);
			dialog.show(fm, TAG);
		}
	}

	/**
	 * Callback used to set selected date
	 */
	public interface TimeSelectedCallback {

		/**
		 * set selected date time
		 *
		 * @param time selected date time or '0' if cancelled
		 */
		void onTimeSelected(long time);
	}
}