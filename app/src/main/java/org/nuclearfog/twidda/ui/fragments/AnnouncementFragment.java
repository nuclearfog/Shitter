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
import org.nuclearfog.twidda.backend.async.ReactionUpdater;
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
	private ReactionUpdater reactionUpdater;

	private AsyncCallback<AnnouncementLoader.Result> announcementloader = this::onAnnouncementLoaded;
	private AsyncCallback<AnnouncementAction.Result> announcementResult = this::onAnnouncementResult;
	private AsyncCallback<ReactionUpdater.Result> reactionUpdateResult = this::onReactionResult;

	private long selectedId;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		adapter = new AnnouncementAdapter(this);
		announcementLoader = new AnnouncementLoader(requireContext());
		announcementAction = new AnnouncementAction(requireContext());
		reactionUpdater = new ReactionUpdater(requireContext());
		setAdapter(adapter, false);

		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_SAVE);
			if (data instanceof Announcements) {
				adapter.setItems((Announcements) data);
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
		reactionUpdater.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		announcementLoader.execute(null, announcementloader);
	}


	@Override
	protected void onReset() {
		adapter.clear();
		announcementAction = new AnnouncementAction(requireContext());
		reactionUpdater = new ReactionUpdater(requireContext());
		announcementLoader = new AnnouncementLoader(requireContext());
		announcementLoader.execute(null, announcementloader);
		setRefresh(true);
	}


	@Override
	public void onConfirm(int type) {
		if (type == ConfirmDialog.ANNOUNCEMENT_DISMISS) {
			AnnouncementAction.Param param = new AnnouncementAction.Param(AnnouncementAction.Param.DISMISS, selectedId);
			announcementAction.execute(param, announcementResult);
		}
	}


	@Override
	public void onAnnouncementDismiss(Announcement announcement) {
		if (announcementAction.isIdle() && announcementLoader.isIdle() && reactionUpdater.isIdle() && isAdded()) {
			if (ConfirmDialog.show(this, ConfirmDialog.ANNOUNCEMENT_DISMISS, null)) {
				selectedId = announcement.getId();
			}
		}
	}


	@Override
	public void onReactionClick(Announcement announcement, Reaction reaction) {
		if (reaction.isSelected()) {
			ReactionUpdater.Param param = new ReactionUpdater.Param(ReactionUpdater.Param.REMOVE, announcement.getId(), reaction.getName());
			reactionUpdater.execute(param, reactionUpdateResult);
		} else {
			ReactionUpdater.Param param = new ReactionUpdater.Param(ReactionUpdater.Param.ADD, announcement.getId(), reaction.getName());
			reactionUpdater.execute(param, reactionUpdateResult);
		}
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
		switch (result.action) {
			case AnnouncementAction.Result.DISMISS:
				if (context != null)
					Toast.makeText(context, R.string.info_announcement_dismissed, Toast.LENGTH_SHORT).show();
				adapter.removeItem(selectedId);
				break;

			case AnnouncementAction.Result.ERROR:
				if (context != null)
					ErrorUtils.showErrorMessage(context, result.exception);
				break;
		}
	}

	/**
	 *
	 */
	private void onReactionResult(ReactionUpdater.Result result) {
		Context context = getContext();
		switch (result.action) {
			case ReactionUpdater.Result.ADD:
				if (context != null)
					Toast.makeText(context, R.string.info_reaction_added, Toast.LENGTH_SHORT).show();
				break;

			case ReactionUpdater.Result.REMOVE:
				if (context != null)
					Toast.makeText(context, R.string.info_reaction_removed, Toast.LENGTH_SHORT).show();
				break;

			case ReactionUpdater.Result.ERROR:
				if (context != null)
					ErrorUtils.showErrorMessage(context, result.exception);
				break;
		}
	}
}