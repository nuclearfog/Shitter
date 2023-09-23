package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

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
public class TimePickerDialog extends Dialog implements OnClickListener {

	private TimePicker timePicker;
	private DatePicker datePicker;

	private TimeSelectedCallback callback;

	/**
	 * @param callback callback used to set selected date
	 */
	public TimePickerDialog(Activity activity, TimeSelectedCallback callback) {
		super(activity, R.style.DefaultDialog);
		this.callback = callback;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_timepicker);
		datePicker = findViewById(R.id.dialog_timepicker_date);
		timePicker = findViewById(R.id.dialog_timepicker_time);
		ViewGroup root = findViewById(R.id.dialog_timepicker_root);
		Button confirm = findViewById(R.id.dialog_timepicker_confirm);
		Button cancel = findViewById(R.id.dialog_timepicker_remove);

		GlobalSettings settings = GlobalSettings.get(getContext());
		AppStyles.setTheme(root, settings.getPopupColor());
		timePicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
		datePicker.setFirstDayOfWeek(Calendar.getInstance().getFirstDayOfWeek());

		confirm.setOnClickListener(this);
		cancel.setOnClickListener(this);
	}


	@Override
	public void show() {
		// using show(long) instead
	}


	@Override
	public void dismiss() {
		super.dismiss();
		datePicker.setVisibility(View.VISIBLE);
		timePicker.setVisibility(View.INVISIBLE);
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
				callback.onTimeSelected(selectedDate.getTime());
				if (isShowing())
					dismiss();
			}
		} else if (v.getId() == R.id.dialog_timepicker_remove) {
			callback.onTimeSelected(0L);
			if (isShowing())
				dismiss();
		}
	}

	/**
	 * show timepicker with default value
	 *
	 * @param time selected time or '0' to select the current time
	 */
	public void show(long time) {
		if (!isShowing()) {
			super.show();
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