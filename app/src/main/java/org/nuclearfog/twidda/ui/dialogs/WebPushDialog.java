package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
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
import org.nuclearfog.twidda.backend.async.PushUpdater.PushUpdateResult;
import org.nuclearfog.twidda.backend.helper.update.PushUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.WebPush;

/**
 * Web push configuration dialog
 *
 * @author nuclearfog
 */
public class WebPushDialog extends Dialog implements OnCheckedChangeListener, OnClickListener, OnItemSelectedListener, AsyncCallback<PushUpdateResult> {

	private PushUpdater updater;
	private GlobalSettings settings;

	private PushUpdate update;

	/**
	 *
	 */
	public WebPushDialog(@NonNull Context context) {
		super(context, R.style.WebPushDialog);
		setContentView(R.layout.dialog_push);
		ViewGroup root = findViewById(R.id.dialog_push_root);
		SwitchButton mention = findViewById(R.id.dialog_push_mention);
		SwitchButton repost = findViewById(R.id.dialog_push_repost);
		SwitchButton favorite = findViewById(R.id.dialog_push_favorite);
		SwitchButton poll = findViewById(R.id.dialog_push_poll);
		SwitchButton follow = findViewById(R.id.dialog_push_follow);
		SwitchButton request = findViewById(R.id.dialog_push_follow_request);
		SwitchButton status_new = findViewById(R.id.dialog_push_new_status);
		SwitchButton status_edit = findViewById(R.id.dialog_push_edit_status);
		Button apply_changes = findViewById(R.id.dialog_push_apply);
		Spinner policySelector = findViewById(R.id.dialog_push_policy);

		settings = GlobalSettings.getInstance(context);
		updater = new PushUpdater(context);
		update = new PushUpdate(settings.getWebPush());
		mention.setCheckedImmediately(update.mentionsEnabled());
		repost.setCheckedImmediately(update.repostEnabled());
		favorite.setCheckedImmediately(update.favoriteEnabled());
		poll.setCheckedImmediately(update.pollEnabled());
		follow.setCheckedImmediately(update.followEnabled());
		request.setCheckedImmediately(update.followRequestEnabled());
		status_new.setCheckedImmediately(update.statusPostEnabled());
		status_edit.setCheckedImmediately(update.statusEditEnabled());
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.push_policy, android.R.layout.simple_spinner_dropdown_item);
		policySelector.setAdapter(adapter);
		switch (update.getPolicy()) {
			case WebPush.POLICY_ALL:
				policySelector.setSelection(0);
				break;

			case WebPush.POLICY_FOLLOWING:
				policySelector.setSelection(1);
				break;

			case WebPush.POLICY_FOLLOWER:
				policySelector.setSelection(2);
				break;
		}
		AppStyles.setTheme(root);

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
	public void show() {
		if (!isShowing()) {
			super.show();
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_push_apply) {
			if (updater.isIdle()) {
				updater.execute(update, this);
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
			switch (position) {
				case 0:
					update.setPolicy(WebPush.POLICY_ALL);
					break;

				case 1:
					update.setPolicy(WebPush.POLICY_FOLLOWING);
					break;

				case 2:
					update.setPolicy(WebPush.POLICY_FOLLOWER);
					break;
			}
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	@Override
	public void onResult(@NonNull PushUpdateResult result) {
		if (result.push != null) {
			Toast.makeText(getContext(), R.string.info_webpush_update, Toast.LENGTH_SHORT).show();
			settings.setWebPush(result.push);
			dismiss();
		}
	}
}