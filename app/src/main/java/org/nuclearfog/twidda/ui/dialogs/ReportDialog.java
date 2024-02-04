package org.nuclearfog.twidda.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

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
import org.nuclearfog.twidda.ui.views.InputView;
import org.nuclearfog.twidda.ui.views.InputView.OnTextChangeListener;

/**
 * User/Status report dialog
 *
 * @author nuclearfog
 */
public class ReportDialog extends DialogFragment implements OnClickListener, OnItemSelectedListener, OnCheckedChangeListener, OnTextChangeListener {

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

	private RuleAdapter ruleAdapter;
	private RuleLoader ruleLoader;
	private ReportUpdater reportUpdater;

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
		Spinner reportCategory = view.findViewById(R.id.dialog_report_category);
		CompoundButton switchForward = view.findViewById(R.id.dialog_report_switch_forward);
		InputView editDescription = view.findViewById(R.id.dialog_report_description);

		GlobalSettings settings = GlobalSettings.get(requireContext());
		DropdownAdapter selectorAdapter = new DropdownAdapter(requireContext());
		reportUpdater = new ReportUpdater(requireContext());
		ruleLoader = new RuleLoader(requireContext());
		ruleAdapter = new RuleAdapter(requireContext());
		selectorAdapter.setItems(R.array.reports);

		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_REPORT);
			if (data instanceof ReportUpdate) {
				reportUpdate = (ReportUpdate) data;
				ruleAdapter.setSelectedIds(reportUpdate.getRuleIds());
				if (reportUpdate.getStatusIds().length > 0) {
					textTitle.setText(R.string.dialog_report_title_status);
				} else {
					textTitle.setText(R.string.dialog_report_title_user);
				}
			}
		}
		AppStyles.setTheme((ViewGroup) view, settings.getPopupColor());
		reportCategory.setAdapter(selectorAdapter);
		reportCategory.setSelection(0);
		ruleSelector.setAdapter(ruleAdapter);

		reportButton.setOnClickListener(this);
		editDescription.setOnTextChangeListener(this);
		switchForward.setOnCheckedChangeListener(this);
		reportCategory.setOnItemSelectedListener(this);
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
	public void onSaveInstanceState(@NonNull Bundle outstate) {
		reportUpdate.setRuleIds(ruleAdapter.getSelectedIds());
		outstate.putSerializable(KEY_REPORT, reportUpdate);
		super.onSaveInstanceState(outstate);
	}


	@Override
	public void onDestroyView() {
		ruleLoader.cancel();
		super.onDestroyView();
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_report_apply) {
			if (reportUpdater.isIdle()) {
				reportUpdate.setRuleIds(ruleAdapter.getSelectedIds());
				reportUpdater.execute(reportUpdate, reportResult);
				Toast.makeText(getContext(), R.string.info_report_submit, Toast.LENGTH_SHORT).show();
			}
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.dialog_report_switch_forward) {
			reportUpdate.setForward(isChecked);
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getId() == R.id.dialog_report_category) {
			if (position == 0) {
				reportUpdate.setCategory(ReportUpdate.CATEGORY_SPAM);
			} else if (position == 1) {
				reportUpdate.setCategory(ReportUpdate.CATEGORY_VIOLATION);
			} else {
				reportUpdate.setCategory(ReportUpdate.CATEGORY_OTHER);
			}
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	@Override
	public void onTextChanged(InputView inputView, String text) {
		if (inputView.getId() == R.id.dialog_report_description) {
			reportUpdate.setComment(text);
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