package org.nuclearfog.twidda.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

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

/**
 * User/Status report dialog
 *
 * @author nuclearfog
 */
public class ReportDialog extends DialogFragment implements OnClickListener {

	/**
	 *
	 */
	private static final String TAG = "ReportDialog";

	/**
	 * Bundle key to set/restore report update
	 * value type is {@link ReportUpdate}
	 */
	private static final String KEY_REPORT = "reportupdate-data";

	private AsyncCallback<ReportUpdater.Result> reportResult = this::onReportResult;
	private AsyncCallback<Rules> rulesResult = this::onRulesLoaded;

	private DropdownAdapter selectorAdapter;
	private RuleAdapter ruleAdapter;
	private RuleLoader ruleLoader;
	private ReportUpdater reportUpdater;
	private GlobalSettings settings;

	private SwitchButton switchForward;
	private EditText editDescription;
	private Spinner reportCategory;

	private ReportUpdate reportUpdate = new ReportUpdate();

	/**
	 *
	 */
	public ReportDialog() {
		setStyle(STYLE_NO_TITLE, R.style.DefaultDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_report, container, false);
		View reportButton = view.findViewById(R.id.dialog_report_apply);
		ListView ruleSelector = view.findViewById(R.id.dialog_report_rule_selector);
		TextView textTitle = view.findViewById(R.id.dialog_report_title);
		reportCategory = view.findViewById(R.id.dialog_report_category);
		switchForward = view.findViewById(R.id.dialog_report_switch_forward);
		editDescription = view.findViewById(R.id.dialog_report_description);

		AppStyles.setTheme((ViewGroup) view, settings.getPopupColor());
		reportCategory.setAdapter(selectorAdapter);
		ruleSelector.setAdapter(ruleAdapter);
		reportButton.setOnClickListener(this);

		selectorAdapter = new DropdownAdapter(requireContext());
		reportUpdater = new ReportUpdater(requireContext());
		ruleLoader = new RuleLoader(requireContext());
		ruleAdapter = new RuleAdapter(requireContext());
		settings = GlobalSettings.get(requireContext());
		selectorAdapter.setItems(R.array.reports);

		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_REPORT);
			if (data instanceof ReportUpdate) {
				reportUpdate = (ReportUpdate) data;
				if (reportUpdate.getStatusIds().length > 0) {
					textTitle.setText(R.string.dialog_report_title_status);
				} else {
					textTitle.setText(R.string.dialog_report_title_user);
				}
			}
		}
		return view;
	}


	@Override
	public void onStart() {
		super.onStart();
		if (ruleAdapter.isEmpty() && ruleLoader.isIdle()) {
			ruleLoader.execute(null, rulesResult);
		}
	}


	@Override
	public void onDestroyView() {
		ruleLoader.cancel();
		super.onDestroyView();
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outstate) {
		outstate.putSerializable(KEY_REPORT, reportUpdate);
		super.onSaveInstanceState(outstate);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_report_apply) {
			if (reportUpdater.isIdle()) {
				if (reportCategory.getSelectedItemPosition() == 0) {
					reportUpdate.setCategory(ReportUpdate.CATEGORY_SPAM);
				} else if (reportCategory.getSelectedItemPosition() == 1) {
					reportUpdate.setCategory(ReportUpdate.CATEGORY_VIOLATION);
				} else {
					reportUpdate.setCategory(ReportUpdate.CATEGORY_OTHER);
				}
				reportUpdate.setRuleIds(ruleAdapter.getSelectedIds());
				reportUpdate.setComment(editDescription.getText().toString());
				reportUpdate.setForward(switchForward.isChecked());
				reportUpdater.execute(reportUpdate, reportResult);
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
	public static void show(FragmentActivity activity, long userId, long... statusId) {
		Fragment dialogFragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
		if (dialogFragment == null) {
			ReportUpdate update = new ReportUpdate(userId, statusId);
			ReportDialog dialog = new ReportDialog();
			Bundle param = new Bundle();
			param.putSerializable(KEY_REPORT, update);
			dialog.setArguments(param);
			dialog.show(activity.getSupportFragmentManager(), TAG);
		}
	}

	/**
	 * callback used by {@link ReportUpdater}
	 */
	private void onReportResult(@NonNull ReportUpdater.Result result) {
		Context context = getContext();
		if (result.exception == null) {
			if (context != null) {
				if (reportUpdate != null && reportUpdate.getStatusIds().length > 0) {
					Toast.makeText(context, R.string.info_status_reported, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, R.string.info_user_reported, Toast.LENGTH_SHORT).show();
				}
			}
			dismiss();
		} else {
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
		}
	}

	/**
	 * callback for {@link RuleLoader}
	 */
	private void onRulesLoaded(@NonNull Rules rules) {
		ruleAdapter.setItems(rules);
	}
}