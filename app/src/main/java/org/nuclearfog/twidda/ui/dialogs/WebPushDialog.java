package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.PushUpdater;
import org.nuclearfog.twidda.backend.helper.update.PushUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.WebPush;
import org.nuclearfog.twidda.ui.adapter.listview.DropdownAdapter;

import java.io.Serializable;

/**
 * Web push configuration dialog
 *
 * @author nuclearfog
 */
public class WebPushDialog extends Dialog implements OnCheckedChangeListener, OnClickListener, OnItemSelectedListener, AsyncCallback<PushUpdater.Result> {

	private static final String KEY_SAVE = "push-update";

	private SwitchButton mention, repost, favorite, poll, follow, request, status_new, status_edit;
	private Spinner policySelector;

	private GlobalSettings settings;
	private PushUpdater pushUpdater;
	private DropdownAdapter adapter;

	private PushUpdate update;

	/**
	 *
	 */
	public WebPushDialog(Activity activity) {
		super(activity, R.style.DefaultDialog);
		adapter = new DropdownAdapter(activity.getApplicationContext());
		settings = GlobalSettings.get(getContext());
		pushUpdater = new PushUpdater(getContext());
		update = new PushUpdate(settings.getWebPush());
		adapter.setItems(R.array.push_policy);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.dialog_push);
		ViewGroup root = findViewById(R.id.dialog_push_root);
		Button apply_changes = findViewById(R.id.dialog_push_apply);
		mention = findViewById(R.id.dialog_push_mention);
		repost = findViewById(R.id.dialog_push_repost);
		favorite = findViewById(R.id.dialog_push_favorite);
		poll = findViewById(R.id.dialog_push_poll);
		follow = findViewById(R.id.dialog_push_follow);
		request = findViewById(R.id.dialog_push_follow_request);
		status_new = findViewById(R.id.dialog_push_new_status);
		status_edit = findViewById(R.id.dialog_push_edit_status);
		policySelector = findViewById(R.id.dialog_push_policy);

		AppStyles.setTheme(root);
		policySelector.setAdapter(adapter);

		mention.setOnCheckedChangeListener(this);
		repost.setOnCheckedChangeListener(this);
		favorite.setOnCheckedChangeListener(this);
		poll.setOnCheckedChangeListener(this);
		follow.setOnCheckedChangeListener(this);
		request.setOnCheckedChangeListener(this);
		status_new.setOnCheckedChangeListener(this);
		status_edit.setOnCheckedChangeListener(this);
		policySelector.setOnItemSelectedListener(this);
		apply_changes.setOnClickListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		mention.setCheckedImmediately(update.mentionsEnabled());
		repost.setCheckedImmediately(update.repostEnabled());
		favorite.setCheckedImmediately(update.favoriteEnabled());
		poll.setCheckedImmediately(update.pollEnabled());
		follow.setCheckedImmediately(update.followEnabled());
		request.setCheckedImmediately(update.followRequestEnabled());
		status_new.setCheckedImmediately(update.statusPostEnabled());
		status_edit.setCheckedImmediately(update.statusEditEnabled());
		if (update.getPolicy() == WebPush.POLICY_ALL) {
			policySelector.setSelection(0);
		} else if (update.getPolicy() == WebPush.POLICY_FOLLOWING) {
			policySelector.setSelection(1);
		} else if (update.getPolicy() == WebPush.POLICY_FOLLOWER) {
			policySelector.setSelection(2);
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
		if (data instanceof PushUpdate) {
			update = (PushUpdate) data;
		}
		super.onRestoreInstanceState(savedInstanceState);
	}


	@Override
	public void show() {
		if (!isShowing()) {
			super.show();
		}
	}


	@Override
	public void dismiss() {
		if (isShowing()) {
			super.dismiss();
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_push_apply) {
			if (pushUpdater.isIdle()) {
				// fix: setting host url if empty
				if (update.getHost().isEmpty()) {
					update.setHost(settings.getWebPush().getHost());
				}
				pushUpdater.execute(update, this);
				Toast.makeText(getContext(), R.string.info_webpush_update_progress, Toast.LENGTH_SHORT).show();
			}
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.dialog_push_mention) {
			update.setMentionsEnabled(isChecked);
		} else if (buttonView.getId() == R.id.dialog_push_repost) {
			update.setRepostEnabled(isChecked);
		} else if (buttonView.getId() == R.id.dialog_push_favorite) {
			update.setFavoriteEnabled(isChecked);
		} else if (buttonView.getId() == R.id.dialog_push_poll) {
			update.setPollEnabled(isChecked);
		} else if (buttonView.getId() == R.id.dialog_push_follow) {
			update.setFollowEnabled(isChecked);
		} else if (buttonView.getId() == R.id.dialog_push_follow_request) {
			update.setFollowRequestEnabled(isChecked);
		} else if (buttonView.getId() == R.id.dialog_push_new_status) {
			update.setStatusPostEnabled(isChecked);
		} else if (buttonView.getId() == R.id.dialog_push_edit_status) {
			update.setStatusEditEnabled(isChecked);
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getId() == R.id.dialog_push_policy) {
			if (isShowing()) {
				if (position == 0) {
					update.setPolicy(WebPush.POLICY_ALL);
				} else if (position == 1) {
					update.setPolicy(WebPush.POLICY_FOLLOWING);
				} else if (position == 2) {
					update.setPolicy(WebPush.POLICY_FOLLOWER);
				}
			}
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	@Override
	public void onResult(@NonNull PushUpdater.Result result) {
		if (result.push != null) {
			Toast.makeText(getContext(), R.string.info_webpush_update, Toast.LENGTH_SHORT).show();
			dismiss();
		} else if (result.exception != null) {
			ErrorUtils.showErrorMessage(getContext(), result.exception);
		}
	}
}