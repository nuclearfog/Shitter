package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.StatusFilterAction;
import org.nuclearfog.twidda.backend.async.StatusFilterAction.FilterActionParam;
import org.nuclearfog.twidda.backend.async.StatusFilterAction.FilterActionResult;
import org.nuclearfog.twidda.backend.helper.update.FilterUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.Filter;

/**
 * Filter update dialog
 *
 * @author nuclearfog
 */
public class FilterDialog extends Dialog implements OnClickListener, OnCheckedChangeListener, AsyncCallback<FilterActionResult> {

	private SwitchButton sw_home, sw_notification, sw_public, sw_user, sw_thread;
	private EditText txt_title, txt_keywords;
	private TextView title;

	private StatusFilterAction filterAction;

	private FilterDialogCallback callback;
	private FilterUpdate update = new FilterUpdate();

	/**
	 *
	 */
	public FilterDialog(Activity activity, FilterDialogCallback callback) {
		super(activity, R.style.FilterDialog);
		this.callback = callback;
		setContentView(R.layout.dialog_filter);

		ViewGroup root = findViewById(R.id.dialog_filter_root);
		Button btn_create = findViewById(R.id.dialog_filter_create);
		title = findViewById(R.id.dialog_filter_title_dialog);
		sw_home = findViewById(R.id.dialog_filter_switch_home);
		sw_notification = findViewById(R.id.dialog_filter_switch_notification);
		sw_public = findViewById(R.id.dialog_filter_switch_public);
		sw_user = findViewById(R.id.dialog_filter_switch_user);
		sw_thread = findViewById(R.id.dialog_filter_switch_thread);
		txt_title = findViewById(R.id.dialog_filter_name);
		txt_keywords = findViewById(R.id.dialog_filter_keywords);

		filterAction = new StatusFilterAction(activity);
		AppStyles.setTheme(root);

		btn_create.setOnClickListener(this);
		sw_home.setOnCheckedChangeListener(this);
		sw_notification.setOnCheckedChangeListener(this);
		sw_public.setOnCheckedChangeListener(this);
		sw_user.setOnCheckedChangeListener(this);
		sw_thread.setOnCheckedChangeListener(this);
	}


	@Override
	public void show() {
	}


	@Override
	public void dismiss() {
		filterAction.cancel();
		super.dismiss();
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_filter_create) {
			update.setTitle(txt_title.getText().toString());
			update.setKeywords(txt_keywords.getText().toString().split("\n"));
			FilterActionParam param = new FilterActionParam(FilterActionParam.UPDATE, 0L, update);
			filterAction.execute(param, this);
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
		}
	}


	@Override
	public void onResult(@NonNull FilterActionResult result) {
		if (result.mode == FilterActionResult.UPDATE) {
			Toast.makeText(getContext(), R.string.info_filter_created, Toast.LENGTH_SHORT).show();
			if (result.filter != null)
				callback.onFilterUpdated(result.filter);
			dismiss();
		} else if (result.mode == FilterActionResult.ERROR) {
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
			super.show();
			// update an existing filter
			if (filter != null) {
				update = new FilterUpdate(filter);
				sw_home.setCheckedImmediately(filter.filterHome());
				sw_notification.setCheckedImmediately(filter.filterNotifications());
				sw_public.setCheckedImmediately(filter.filterPublic());
				sw_user.setCheckedImmediately(filter.filterUserTimeline());
				sw_thread.setCheckedImmediately(filter.filterThreads());
				txt_title.setText(filter.getTitle());

				if (filter.getKeywords().length > 0) {
					StringBuilder keywordsStr = new StringBuilder();
					for (Filter.Keyword keyword : filter.getKeywords()) {
						if (keyword.isOneWord()) {
							keywordsStr.append("\"").append(keyword.getKeyword()).append("\"");
						} else {
							keywordsStr.append(keyword.getKeyword());
						}
						keywordsStr.append('\n');
					}
					keywordsStr.deleteCharAt(keywordsStr.length() - 1);
					txt_keywords.setText(keywordsStr);
				} else {
					txt_keywords.setText("");
				}
				title.setText(R.string.dialog_filter_update);
			}
			// create new filter
			else {
				update = new FilterUpdate();
				sw_home.setCheckedImmediately(false);
				sw_notification.setCheckedImmediately(false);
				sw_public.setCheckedImmediately(false);
				sw_user.setCheckedImmediately(false);
				sw_thread.setCheckedImmediately(false);
				txt_title.setText("");
				txt_keywords.setText("");
				title.setText(R.string.dialog_filter_create);
			}
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