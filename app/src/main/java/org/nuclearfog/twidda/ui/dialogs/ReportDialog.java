package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.ReportUpdater;
import org.nuclearfog.twidda.backend.async.RuleLoader;
import org.nuclearfog.twidda.backend.helper.update.ReportUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.lists.Rules;
import org.nuclearfog.twidda.ui.adapter.listview.DropdownAdapter;
import org.nuclearfog.twidda.ui.adapter.listview.RuleAdapter;

import java.io.Serializable;

/**
 * User/Status report dialog
 *
 * @author nuclearfog
 */
public class ReportDialog extends Dialog implements OnClickListener {

	private static final String KEY_SAVE = "reportupdate-data";

	private AsyncCallback<ReportUpdater.Result> reportResult = this::onReportResult;
	private AsyncCallback<Rules> rulesResult = this::onRulesLoaded;

	private DropdownAdapter selectorAdapter;
	private RuleAdapter ruleAdapter;
	private RuleLoader ruleLoader;
	private ReportUpdater reportUpdater;
	private GlobalSettings settings;

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
		selectorAdapter = new DropdownAdapter(activity.getApplicationContext());
		reportUpdater = new ReportUpdater(activity.getApplicationContext());
		ruleLoader = new RuleLoader(activity.getApplicationContext());
		ruleAdapter = new RuleAdapter(activity.getApplicationContext());
		settings = GlobalSettings.get(getContext());
		selectorAdapter.setItems(R.array.reports);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_report);
		ViewGroup rootView = findViewById(R.id.dialog_report_root);
		View reportButton = findViewById(R.id.dialog_report_apply);
		ListView ruleSelector = findViewById(R.id.dialog_report_rule_selector);
		reportCategory = findViewById(R.id.dialog_report_category);
		textTitle = findViewById(R.id.dialog_report_title);
		switchForward = findViewById(R.id.dialog_report_switch_forward);
		editDescription = findViewById(R.id.dialog_report_description);

		AppStyles.setTheme(rootView, settings.getPopupColor());
		reportCategory.setAdapter(selectorAdapter);
		ruleSelector.setAdapter(ruleAdapter);
		reportButton.setOnClickListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		if (ruleAdapter.isEmpty() && ruleLoader.isIdle()) {
			ruleLoader.execute(null, rulesResult);
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
				update.setRuleIds(ruleAdapter.getSelectedIds());
				update.setComment(editDescription.getText().toString());
				update.setForward(switchForward.isChecked());
				reportUpdater.execute(update, reportResult);
				Toast.makeText(getContext(), R.string.info_report_submit, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * show this dialog
	 *
	 * @param userId   Id of the user to report to instance
	 * @param statusId additional status IDs
	 */
	public void show(long userId, long... statusId) {
		if (!isShowing()) {
			super.show();
			update = new ReportUpdate(userId);
			if (statusId.length > 0) {
				update.setStatusIds(statusId);
				textTitle.setText(R.string.dialog_report_title_status);
			} else {
				textTitle.setText(R.string.dialog_report_title_user);
			}
		}
	}

	/**
	 * callback used by {@link ReportUpdater}
	 */
	private void onReportResult(@NonNull ReportUpdater.Result result) {
		if (result.exception == null) {
			if (update != null && update.getStatusIds().length > 0) {
				Toast.makeText(getContext(), R.string.info_status_reported, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getContext(), R.string.info_user_reported, Toast.LENGTH_SHORT).show();
			}
			dismiss();
		} else {
			ErrorUtils.showErrorMessage(getContext(), result.exception);
		}
	}

	/**
	 * callback for {@link RuleLoader}
	 */
	private void onRulesLoaded(@NonNull Rules rules) {
		ruleAdapter.setItems(rules);
	}
}