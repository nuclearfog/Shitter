package org.nuclearfog.twidda.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AnnouncementAction;
import org.nuclearfog.twidda.backend.async.AnnouncementLoader;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.Announcement;
import org.nuclearfog.twidda.model.Reaction;
import org.nuclearfog.twidda.model.lists.Announcements;
import org.nuclearfog.twidda.ui.adapter.recyclerview.AnnouncementAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.AnnouncementAdapter.OnAnnouncementClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

/**
 * ListFragment used to show instance announcements
 *
 * @author nuclearfog
 */
public class AnnouncementFragment extends ListFragment implements OnAnnouncementClickListener, OnConfirmListener {

	/**
	 * Bundle key used to save RecyclerView adapter content
	 */
	private static final String KEY_SAVE = "save-anncouncements";

	private AnnouncementAdapter adapter;
	private AnnouncementLoader announcementLoader;
	private AnnouncementAction announcementAction;
	private ConfirmDialog confirmDialog;

	private AsyncCallback<AnnouncementLoader.Result> announcementloader = this::onAnnouncementLoaded;
	private AsyncCallback<AnnouncementAction.Result> announcementResult = this::onAnnouncementResult;

	private long selectedId;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		adapter = new AnnouncementAdapter(this);
		announcementLoader = new AnnouncementLoader(requireContext());
		announcementAction = new AnnouncementAction(requireContext());
		confirmDialog = new ConfirmDialog(requireActivity(), this);
		setAdapter(adapter);

		if (savedInstanceState != null) {
			Object data_announcement = savedInstanceState.getSerializable(KEY_SAVE);
			if (data_announcement instanceof Announcements) {
				adapter.setItems((Announcements) data_announcement);
			}
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		if (adapter.isEmpty()) {
			announcementLoader.execute(null, announcementloader);
			setRefresh(true);
		}
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_SAVE, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onDestroy() {
		announcementLoader.cancel();
		announcementAction.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		announcementLoader.execute(null, announcementloader);
	}


	@Override
	protected void onReset() {
		adapter.clear();
		announcementLoader = new AnnouncementLoader(requireContext());
		announcementLoader.execute(null, announcementloader);
		setRefresh(true);
	}


	@Override
	public void onConfirm(int type, boolean remember) {
		if (type == ConfirmDialog.ANNOUNCEMENT_DISMISS) {
			AnnouncementAction.Param param = new AnnouncementAction.Param(AnnouncementAction.Param.MODE_DISMISS, selectedId);
			announcementAction.execute(param, announcementResult);
		}
	}


	@Override
	public void onAnnouncementClick(Announcement announcement) {
		// todo implement this
	}


	@Override
	public void onAnnouncementDismiss(Announcement announcement) {
		if (!confirmDialog.isShowing() && announcementAction.isIdle() && announcementLoader.isIdle()) {
			confirmDialog.show(ConfirmDialog.ANNOUNCEMENT_DISMISS);
			selectedId = announcement.getId();
		}
	}


	@Override
	public void onReactionClick(Reaction reaction) {
		// todo implement this
	}

	/**
	 *
	 */
	private void onAnnouncementLoaded(AnnouncementLoader.Result result) {
		if (result.announcements != null) {
			adapter.setItems(result.announcements);
		} else {
			Context context = getContext();
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
		}
		setRefresh(false);
	}

	/**
	 *
	 */
	private void onAnnouncementResult(AnnouncementAction.Result result) {
		Context context = getContext();
		if (context != null) {
			switch (result.mode) {
				case AnnouncementAction.Result.MODE_DISMISS:
					Toast.makeText(context, R.string.info_announcement_dismissed, Toast.LENGTH_SHORT).show();
					adapter.removeItem(selectedId);
					break;

				case AnnouncementAction.Result.MODE_ERROR:
					ErrorUtils.showErrorMessage(context, result.exception);
					break;
			}
		}
	}
}