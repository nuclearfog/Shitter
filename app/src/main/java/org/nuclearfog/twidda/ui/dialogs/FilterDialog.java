package org.nuclearfog.twidda.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

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
import org.nuclearfog.twidda.ui.views.InputView;
import org.nuclearfog.twidda.ui.views.InputView.OnTextChangeListener;

/**
 * Filter create dialog
 *
 * @author nuclearfog
 */
public class FilterDialog extends DialogFragment implements OnClickListener, OnCheckedChangeListener, OnTextChangeListener, AsyncCallback<StatusFilterAction.Result> {

	/**
	 *
	 */
	private static final String TAG = "FilterDialog";

	/**
	 * Bunlde key used to set/restore filter configuration
	 * value type is {@link Filter} or {@link FilterUpdate}
	 */
	private static final String KEY_FILTER = "filter_data";

	private Spinner timeunit;

	private StatusFilterAction filterAction;
	private FilterUpdate filterUpdate = new FilterUpdate();

	/**
	 *
	 */
	public FilterDialog() {
		setStyle(STYLE_NO_TITLE, R.style.DefaultDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_filter, container, false);
		Button btn_create = view.findViewById(R.id.dialog_filter_create);
		SwitchButton sw_hide = view.findViewById(R.id.dialog_filter_switch_hide);
		TextView title = view.findViewById(R.id.dialog_filter_title_dialog);
		InputView txt_title = view.findViewById(R.id.dialog_filter_name);
		InputView txt_keywords = view.findViewById(R.id.dialog_filter_keywords);
		InputView txt_duration = view.findViewById(R.id.dialog_filter_time);
		SwitchButton sw_home = view.findViewById(R.id.dialog_filter_switch_home);
		SwitchButton sw_notification = view.findViewById(R.id.dialog_filter_switch_notification);
		SwitchButton sw_public = view.findViewById(R.id.dialog_filter_switch_public);
		SwitchButton sw_user = view.findViewById(R.id.dialog_filter_switch_user);
		SwitchButton sw_thread = view.findViewById(R.id.dialog_filter_switch_thread);
		timeunit = view.findViewById(R.id.dialog_filter_timeunit);

		GlobalSettings settings = GlobalSettings.get(requireContext());
		DropdownAdapter timeUnitAdapter = new DropdownAdapter(requireContext());
		filterAction = new StatusFilterAction(requireContext());
		timeUnitAdapter.setItems(R.array.timeunits);
		timeunit.setAdapter(timeUnitAdapter);
		AppStyles.setTheme((ViewGroup) view, settings.getPopupColor());

		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_FILTER);
			if (data instanceof FilterUpdate) {
				filterUpdate = (FilterUpdate) data;
			} else if (data instanceof Filter) {
				filterUpdate = new FilterUpdate((Filter) data);
			}
		}
		if (filterUpdate.getId() == 0L) {
			title.setText(R.string.dialog_title_create_filter);
		} else {
			title.setText(R.string.dialog_filter_update);
			if (filterUpdate.getKeywords().length > 0) {
				StringBuilder keywordsStr = new StringBuilder();
				for (String keyword : filterUpdate.getKeywords())
					keywordsStr.append(keyword).append('\n');
				keywordsStr.deleteCharAt(keywordsStr.length() - 1);
				txt_keywords.setText(keywordsStr);
			}
		}
		sw_home.setCheckedImmediately(filterUpdate.filterHomeSet());
		sw_notification.setCheckedImmediately(filterUpdate.filterNotificationSet());
		sw_public.setCheckedImmediately(filterUpdate.filterPublicSet());
		sw_user.setCheckedImmediately(filterUpdate.filterUserSet());
		sw_thread.setCheckedImmediately(filterUpdate.filterThreadSet());
		sw_hide.setCheckedImmediately(filterUpdate.getFilterAction() == Filter.ACTION_HIDE);
		txt_title.setText(filterUpdate.getTitle());
		if (filterUpdate.getExpirationTime() > 86400) {
			txt_duration.setText(Long.toString(Math.round(filterUpdate.getExpirationTime() / 86400d)));
			timeunit.setSelection(2);
		} else if (filterUpdate.getExpirationTime() > 3600) {
			txt_duration.setText(Long.toString(Math.round(filterUpdate.getExpirationTime() / 3600d)));
			timeunit.setSelection(1);
		} else if (filterUpdate.getExpirationTime() > 60) {
			txt_duration.setText(Long.toString(Math.round(filterUpdate.getExpirationTime() / 60d)));
			timeunit.setSelection(0);
		} else {
			timeunit.setSelection(2);
		}
		btn_create.setOnClickListener(this);
		sw_home.setOnCheckedChangeListener(this);
		sw_notification.setOnCheckedChangeListener(this);
		sw_public.setOnCheckedChangeListener(this);
		sw_user.setOnCheckedChangeListener(this);
		sw_thread.setOnCheckedChangeListener(this);
		sw_hide.setOnCheckedChangeListener(this);
		txt_title.setOnTextChangeListener(this);
		txt_keywords.setOnTextChangeListener(this);
		txt_duration.setOnTextChangeListener(this);
		return view;
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_FILTER, filterUpdate);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_filter_create) {
			if (filterUpdate.getTitle().trim().isEmpty()) {
				Toast.makeText(requireContext(), R.string.error_empty_filter_title, Toast.LENGTH_SHORT).show();
			} else if (filterUpdate.getExpirationTime() == 0) {
				Toast.makeText(requireContext(), R.string.error_empty_filter_expiration, Toast.LENGTH_SHORT).show();
			} else if (!filterUpdate.filterHomeSet() && !filterUpdate.filterNotificationSet() && !filterUpdate.filterPublicSet()
					&& !filterUpdate.filterUserSet() && !filterUpdate.filterThreadSet()) {
				Toast.makeText(requireContext(), R.string.error_empty_filter_selection, Toast.LENGTH_SHORT).show();
			} else if (filterUpdate.getKeywords().length == 0) {
				Toast.makeText(requireContext(), R.string.error_empty_filter_keywords, Toast.LENGTH_SHORT).show();
			} else {
				StatusFilterAction.Param param = new StatusFilterAction.Param(StatusFilterAction.Param.UPDATE, 0L, filterUpdate);
				filterAction.execute(param, this);
			}
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// ignore setCheckedImmediately() calls
		if (buttonView.isPressed()) {
			if (buttonView.getId() == R.id.dialog_filter_switch_home) {
				filterUpdate.setFilterHome(isChecked);
			} else if (buttonView.getId() == R.id.dialog_filter_switch_notification) {
				filterUpdate.setFilterNotification(isChecked);
			} else if (buttonView.getId() == R.id.dialog_filter_switch_public) {
				filterUpdate.setFilterPublic(isChecked);
			} else if (buttonView.getId() == R.id.dialog_filter_switch_user) {
				filterUpdate.setFilterUser(isChecked);
			} else if (buttonView.getId() == R.id.dialog_filter_switch_thread) {
				filterUpdate.setFilterThread(isChecked);
			} else if (buttonView.getId() == R.id.dialog_filter_switch_hide) {
				if (isChecked) {
					filterUpdate.setFilterAction(Filter.ACTION_HIDE);
				} else {
					filterUpdate.setFilterAction(Filter.ACTION_WARN);
				}
			}
		}
	}


	@Override
	public void onTextChanged(InputView inputView, String text) {
		if (inputView.getId() == R.id.dialog_filter_name) {
			filterUpdate.setTitle(text);
		} else if (inputView.getId() == R.id.dialog_filter_keywords) {
			if (!text.isEmpty()) {
				filterUpdate.setKeywords(text.split("\n"));
			} else {
				filterUpdate.setKeywords(new String[0]);
			}
		} else if (inputView.getId() == R.id.dialog_filter_time) {
			int duration = 0;
			if (text.matches("\\d{1,3}"))
				duration = Integer.parseInt(text);
			if (timeunit.getSelectedItemPosition() == 0)
				duration *= 60;
			else if (timeunit.getSelectedItemPosition() == 1)
				duration *= 3600;
			else if (timeunit.getSelectedItemPosition() == 2)
				duration *= 86400;
			filterUpdate.setExpirationTime(duration);
		}
	}


	@Override
	public void onResult(@NonNull StatusFilterAction.Result result) {
		Context context = getContext();
		if (result.action == StatusFilterAction.Result.UPDATE) {
			if (context != null)
				Toast.makeText(context, R.string.info_filter_created, Toast.LENGTH_SHORT).show();
			if (getParentFragment() instanceof FilterDialogCallback)
				((FilterDialogCallback) getParentFragment()).onFilterUpdated(result.filter);
			else if (result.filter != null && getActivity() instanceof FilterDialogCallback)
				((FilterDialogCallback) getActivity()).onFilterUpdated(result.filter);
			dismiss();
		} else if (result.action == StatusFilterAction.Result.ERROR) {
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
		}
	}

	/**
	 *
	 */
	public static void show(Fragment fragment, Filter filter) {
		if (fragment.isAdded()) {
			show(fragment.getChildFragmentManager(), filter);
		}
	}

	/**
	 *
	 */
	public static void show(FragmentActivity activity) {
		show(activity.getSupportFragmentManager(), (Filter) null);
	}

	/**
	 *
	 */
	private static void show(FragmentManager fm, Filter filter) {
		Fragment dialogFragment = fm.findFragmentByTag(TAG);
		if (dialogFragment == null) {
			FilterDialog dialog = new FilterDialog();
			Bundle param = new Bundle();
			param.putSerializable(KEY_FILTER, filter);
			dialog.setArguments(param);
			dialog.show(fm, TAG);
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