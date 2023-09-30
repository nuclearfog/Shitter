package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.StatusFilterAction;
import org.nuclearfog.twidda.backend.helper.update.FilterUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Filter;
import org.nuclearfog.twidda.ui.adapter.listview.DropdownAdapter;

import java.io.Serializable;

/**
 * Filter create dialog
 *
 * @author nuclearfog
 */
public class FilterDialog extends Dialog implements OnClickListener, OnCheckedChangeListener, AsyncCallback<StatusFilterAction.Result> {

	private static final String KEY_SAVE = "filterupdate-save";

	private SwitchButton sw_home, sw_notification, sw_public, sw_user, sw_thread, sw_hide;
	private EditText txt_title, txt_keywords, txt_duration;
	private Spinner timeunit;
	private TextView title;

	private DropdownAdapter timeUnitAdapter;
	private StatusFilterAction filterAction;
	private FilterDialogCallback callback;
	private FilterUpdate update;
	private GlobalSettings settings;

	/**
	 *
	 */
	public FilterDialog(Activity activity, FilterDialogCallback callback) {
		super(activity, R.style.DefaultDialog);
		this.callback = callback;
		update = new FilterUpdate();
		settings = GlobalSettings.get(activity);
		filterAction = new StatusFilterAction(activity);
		timeUnitAdapter = new DropdownAdapter(activity.getApplicationContext());
		timeUnitAdapter.setItems(R.array.timeunits);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_filter);
		ViewGroup root = findViewById(R.id.dialog_filter_root);
		Button btn_create = findViewById(R.id.dialog_filter_create);
		sw_hide = findViewById(R.id.dialog_filter_switch_hide);
		title = findViewById(R.id.dialog_filter_title_dialog);
		sw_home = findViewById(R.id.dialog_filter_switch_home);
		sw_notification = findViewById(R.id.dialog_filter_switch_notification);
		sw_public = findViewById(R.id.dialog_filter_switch_public);
		sw_user = findViewById(R.id.dialog_filter_switch_user);
		sw_thread = findViewById(R.id.dialog_filter_switch_thread);
		txt_title = findViewById(R.id.dialog_filter_name);
		txt_keywords = findViewById(R.id.dialog_filter_keywords);
		timeunit = findViewById(R.id.dialog_filter_timeunit);
		txt_duration = findViewById(R.id.dialog_filter_time);

		timeunit.setAdapter(timeUnitAdapter);
		AppStyles.setTheme(root, settings.getPopupColor());

		btn_create.setOnClickListener(this);
		sw_home.setOnCheckedChangeListener(this);
		sw_notification.setOnCheckedChangeListener(this);
		sw_public.setOnCheckedChangeListener(this);
		sw_user.setOnCheckedChangeListener(this);
		sw_thread.setOnCheckedChangeListener(this);
		sw_hide.setOnCheckedChangeListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		if (update != null) {
			// update an existing filter
			if (update.getId() != 0L) {
				title.setText(R.string.dialog_filter_update);
				if (update.getKeywords().length > 0) {
					StringBuilder keywordsStr = new StringBuilder();
					for (String keyword : update.getKeywords()) {
						keywordsStr.append(keyword).append('\n');
					}
					// delete last newline symbol
					keywordsStr.deleteCharAt(keywordsStr.length() - 1);
					txt_keywords.setText(keywordsStr);
				} else {
					txt_keywords.setText("");
				}
			}
			// create new filter
			else {
				title.setText(R.string.dialog_filter_create);
				txt_keywords.setText("");
			}
			sw_home.setCheckedImmediately(update.filterHomeSet());
			sw_notification.setCheckedImmediately(update.filterNotificationSet());
			sw_public.setCheckedImmediately(update.filterPublicSet());
			sw_user.setCheckedImmediately(update.filterUserSet());
			sw_thread.setCheckedImmediately(update.filterThreadSet());
			sw_hide.setCheckedImmediately(update.getFilterAction() == Filter.ACTION_HIDE);
			txt_title.setText(update.getTitle());
			if (update.getExpirationTime() > 86400) {
				txt_duration.setText(Long.toString(Math.round(update.getExpirationTime() / 86400d)));
				timeunit.setSelection(2);
			} else if (update.getExpirationTime() > 3600) {
				txt_duration.setText(Long.toString(Math.round(update.getExpirationTime() / 3600d)));
				timeunit.setSelection(1);
			} else if (update.getExpirationTime() > 60) {
				txt_duration.setText(Long.toString(Math.round(update.getExpirationTime() / 60d)));
				timeunit.setSelection(0);
			} else {
				timeunit.setSelection(2);
			}
		}
	}


	@NonNull
	@Override
	public Bundle onSaveInstanceState() {
		Bundle bundle = super.onSaveInstanceState();
		bundle.putSerializable(KEY_SAVE, update);
		return bundle;
	}


	@Override
	public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		Serializable data = savedInstanceState.getSerializable(KEY_SAVE);
		if (data instanceof FilterUpdate) {
			update = (FilterUpdate) data;
		}
		super.onRestoreInstanceState(savedInstanceState);
	}


	@Override
	public void show() {
		// using show(filter) instead
	}


	@Override
	public void dismiss() {
		if (isShowing()) {
			filterAction.cancel();
			super.dismiss();
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_filter_create) {
			if (txt_title.length() == 0) {
				Toast.makeText(getContext(), R.string.error_empty_filter_title, Toast.LENGTH_SHORT).show();
			} else if (!sw_home.isChecked() && !sw_notification.isChecked() && !sw_public.isChecked() && !sw_user.isChecked() && !sw_thread.isChecked()) {
				Toast.makeText(getContext(), R.string.error_empty_filter_selection, Toast.LENGTH_SHORT).show();
			} else {
				String durationStr = txt_duration.getText().toString();
				int duration = 0;
				if (durationStr.matches("\\d{1,3}"))
					duration = Integer.parseInt(durationStr);
				if (timeunit.getSelectedItemPosition() == 0)
					duration *= 60;
				else if (timeunit.getSelectedItemPosition() == 1)
					duration *= 3600;
				else if (timeunit.getSelectedItemPosition() == 2)
					duration *= 86400;
				update.setExpirationTime(duration);
				if (txt_keywords.length() > 0)
					update.setKeywords(txt_keywords.getText().toString().split("\n"));
				update.setTitle(txt_title.getText().toString());
				StatusFilterAction.Param param = new StatusFilterAction.Param(StatusFilterAction.Param.UPDATE, 0L, update);
				filterAction.execute(param, this);
			}
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// ignore setCheckedImmediately() calls
		if (buttonView.isPressed()) {
			if (buttonView.getId() == R.id.dialog_filter_switch_home) {
				update.setFilterHome(sw_home.isChecked());
			} else if (buttonView.getId() == R.id.dialog_filter_switch_notification) {
				update.setFilterNotification(sw_notification.isChecked());
			} else if (buttonView.getId() == R.id.dialog_filter_switch_public) {
				update.setFilterPublic(sw_public.isChecked());
			} else if (buttonView.getId() == R.id.dialog_filter_switch_user) {
				update.setFilterUser(sw_user.isChecked());
			} else if (buttonView.getId() == R.id.dialog_filter_switch_thread) {
				update.setFilterThread(sw_thread.isChecked());
			} else if (buttonView.getId() == R.id.dialog_filter_switch_hide) {
				if (isChecked) {
					update.setFilterAction(Filter.ACTION_HIDE);
				} else {
					update.setFilterAction(Filter.ACTION_WARN);
				}
			}
		}
	}


	@Override
	public void onResult(@NonNull StatusFilterAction.Result result) {
		if (result.mode == StatusFilterAction.Result.UPDATE) {
			Toast.makeText(getContext(), R.string.info_filter_created, Toast.LENGTH_SHORT).show();
			if (result.filter != null)
				callback.onFilterUpdated(result.filter);
			dismiss();
		} else if (result.mode == StatusFilterAction.Result.ERROR) {
			ErrorUtils.showErrorMessage(getContext(), result.exception);
		}
	}

	/**
	 * create dialog window
	 *
	 * @param filter configuration of an existing filter to update
	 */
	public void show(@Nullable Filter filter) {
		if (!isShowing()) {
			if (filter != null) {
				update = new FilterUpdate(filter);
			} else {
				update = new FilterUpdate();
			}
			super.show();
		}
	}

	/**
	 * callback used to update filter
	 */
	public interface FilterDialogCallback {

		/**
		 * called when a filter is created/updated
		 *
		 * @param filter new filter
		 */
		void onFilterUpdated(Filter filter);
	}
}