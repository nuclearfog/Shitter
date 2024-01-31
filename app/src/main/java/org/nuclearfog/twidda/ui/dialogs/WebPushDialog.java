package org.nuclearfog.twidda.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

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

/**
 * Web push configuration dialog
 *
 * @author nuclearfog
 */
public class WebPushDialog extends DialogFragment implements OnCheckedChangeListener, OnClickListener, OnItemSelectedListener, AsyncCallback<PushUpdater.Result> {

	private static final String TAG = "WebPushDialog";

	private static final String KEY_SAVE = "push-update";

	private static final String KEY_PUSH = "push-save";

	private GlobalSettings settings;
	private PushUpdater pushUpdater;
	private DropdownAdapter adapter;

	private PushUpdate update;

	/**
	 *
	 */
	public WebPushDialog() {
		setStyle(STYLE_NO_TITLE, R.style.DefaultDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_push, container, false);
		Button apply_changes = view.findViewById(R.id.dialog_push_apply);
		SwitchButton mention = view.findViewById(R.id.dialog_push_mention);
		SwitchButton repost = view.findViewById(R.id.dialog_push_repost);
		SwitchButton favorite = view.findViewById(R.id.dialog_push_favorite);
		SwitchButton poll = view.findViewById(R.id.dialog_push_poll);
		SwitchButton follow = view.findViewById(R.id.dialog_push_follow);
		SwitchButton request = view.findViewById(R.id.dialog_push_follow_request);
		SwitchButton status_new = view.findViewById(R.id.dialog_push_new_status);
		SwitchButton status_edit = view.findViewById(R.id.dialog_push_edit_status);
		Spinner policySelector = view.findViewById(R.id.dialog_push_policy);

		AppStyles.setTheme((ViewGroup) view);
		policySelector.setAdapter(adapter);
		adapter = new DropdownAdapter(requireContext());
		settings = GlobalSettings.get(requireContext());
		pushUpdater = new PushUpdater(requireContext());
		update = new PushUpdate(settings.getWebPush());
		adapter.setItems(R.array.push_policy);

		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_PUSH);
			if (data instanceof PushUpdate) {
				update = (PushUpdate) data;
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
		}
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
		return view;
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outstate) {
		outstate.putSerializable(KEY_SAVE, update);
		super.onSaveInstanceState(outstate);
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
			if (position == 0) {
				update.setPolicy(WebPush.POLICY_ALL);
			} else if (position == 1) {
				update.setPolicy(WebPush.POLICY_FOLLOWING);
			} else if (position == 2) {
				update.setPolicy(WebPush.POLICY_FOLLOWER);
			}
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	@Override
	public void onResult(@NonNull PushUpdater.Result result) {
		Context context = getContext();
		if (result.push != null) {
			if (context != null)
				Toast.makeText(context, R.string.info_webpush_update, Toast.LENGTH_SHORT).show();
			settings.setPushEnabled(true);
			dismiss();
		} else if (result.exception != null) {
			if (context != null)
				ErrorUtils.showErrorMessage(context, result.exception);
			settings.setPushEnabled(false);
		}
	}

	/**
	 *
	 */
	public static void show(FragmentActivity activity) {
		Fragment dialogFragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
		if (dialogFragment == null) {
			WebPushDialog dialog = new WebPushDialog();
			dialog.show(activity.getSupportFragmentManager(), TAG);
		}
	}
}