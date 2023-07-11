package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.ReportUpdater;
import org.nuclearfog.twidda.backend.async.ReportUpdater.ReportResult;
import org.nuclearfog.twidda.backend.helper.update.ReportUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.ui.adapter.recyclerview.DropdownAdapter;

import java.io.Serializable;

/**
 * User/Status report dialog
 *
 * @author nuclearfog
 */
public class ReportDialog extends Dialog implements OnClickListener, AsyncCallback<ReportResult> {

	private static final String KEY_SAVE = "reportupdate-data";

	private DropdownAdapter adapter;
	private ReportUpdater reportUpdater;

	private TextView textTitle;
	private SwitchButton switchForward;
	private EditText editDescription;
	private Spinner reportCategory;

	private ReportUpdate update;

	/**
	 *
	 */
	public ReportDialog(Activity activity) {
		super(activity, R.style.DefaultDialog);
		adapter = new DropdownAdapter(activity.getApplicationContext());
		reportUpdater = new ReportUpdater(activity.getApplicationContext());
		adapter.setItems(R.array.reports);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_report);
		ViewGroup root = findViewById(R.id.dialog_report_root);
		View reportButton = findViewById(R.id.dialog_report_apply);
		reportCategory = findViewById(R.id.dialog_report_category);
		textTitle = findViewById(R.id.dialog_report_title);
		switchForward = findViewById(R.id.dialog_report_switch_forward);
		editDescription = findViewById(R.id.dialog_report_description);
		AppStyles.setTheme(root);

		reportCategory.setAdapter(adapter);

		reportButton.setOnClickListener(this);
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
		if (data instanceof ReportUpdate) {
			update = (ReportUpdate) data;
		}
		super.onRestoreInstanceState(savedInstanceState);
	}


	@Override
	public void show() {
		// using show(long, long) instead
	}


	@Override
	public void dismiss() {
		if (isShowing()) {
			super.dismiss();
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_report_apply) {
			if (update != null && reportUpdater.isIdle()) {
				if (reportCategory.getSelectedItemPosition() == 0) {
					update.setCategory(ReportUpdate.CATEGORY_SPAM);
				} else if (reportCategory.getSelectedItemPosition() == 1) {
					update.setCategory(ReportUpdate.CATEGORY_VIOLATION);
				} else {
					update.setCategory(ReportUpdate.CATEGORY_OTHER);
				}
				update.setComment(editDescription.getText().toString());
				update.setForward(switchForward.isChecked());
				reportUpdater.execute(update, this);
			}
		}
	}


	@Override
	public void onResult(@NonNull ReportResult reportResult) {
		if (reportResult.reported) {
			if (update != null && update.getStatusIds().length > 0) {
				Toast.makeText(getContext(), R.string.info_status_reported, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getContext(), R.string.info_user_reported, Toast.LENGTH_SHORT).show();
			}
		} else {
			ErrorUtils.showErrorMessage(getContext(), reportResult.exception);
		}
	}

	/**
	 *
	 */
	public void show(long userId, long statusId) {
		if (!isShowing()) {
			super.show();
			update = new ReportUpdate(userId);
			if (statusId != 0L) {
				update.setStatusIds(new long[]{statusId});
				textTitle.setText(R.string.dialog_report_title_status);
			} else {
				textTitle.setText(R.string.dialog_report_title_user);
			}
		}
	}
}