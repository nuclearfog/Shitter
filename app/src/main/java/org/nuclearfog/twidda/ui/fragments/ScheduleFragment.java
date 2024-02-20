package org.nuclearfog.twidda.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.ScheduleAction;
import org.nuclearfog.twidda.backend.async.ScheduleLoader;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.ScheduledStatus;
import org.nuclearfog.twidda.model.lists.ScheduledStatuses;
import org.nuclearfog.twidda.ui.activities.ImageViewer;
import org.nuclearfog.twidda.ui.activities.VideoViewer;
import org.nuclearfog.twidda.ui.adapter.recyclerview.ScheduleAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.ScheduleAdapter.OnScheduleClickListener;
import org.nuclearfog.twidda.ui.dialogs.AudioPlayerDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.TimePickerDialog;
import org.nuclearfog.twidda.ui.dialogs.TimePickerDialog.TimeSelectedCallback;

/**
 * @author nuclearfog
 */
public class ScheduleFragment extends ListFragment implements OnScheduleClickListener, OnConfirmListener, TimeSelectedCallback {

	private static final String KEY_SAVE = "schedule_status_save";

	private static final int CLEAR_LIST = -1;

	private ScheduleAdapter adapter;
	private ScheduleLoader scheduleLoader;
	private ScheduleAction scheduleAction;

	@Nullable
	private ScheduledStatus selection;

	private AsyncCallback<ScheduleLoader.Result> loaderCallback = this::onLoaderResult;
	private AsyncCallback<ScheduleAction.Result> actionCallback = this::onActionResult;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		scheduleLoader = new ScheduleLoader(requireContext());
		scheduleAction = new ScheduleAction(requireContext());
		adapter = new ScheduleAdapter(this);
		setAdapter(adapter, false);

		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_SAVE);
			if (data instanceof ScheduledStatuses) {
				adapter.setItems((ScheduledStatuses) data);
			}
		}
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_SAVE, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onStart() {
		super.onStart();
		if (adapter.isEmpty()) {
			load(0L, 0L, CLEAR_LIST);
			setRefresh(true);
		}
	}


	@Override
	public void onDestroy() {
		scheduleLoader.cancel();
		scheduleAction.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		load(adapter.getTopItemId(), 0L, 0);
	}


	@Override
	protected void onReset() {
		adapter.clear();
		scheduleLoader = new ScheduleLoader(requireContext());
		scheduleAction = new ScheduleAction(requireContext());
		load(0L, 0L, CLEAR_LIST);
		setRefresh(true);
	}


	@Override
	public void onScheduleSelect(ScheduledStatus status) {
		if (!isRefreshing() && scheduleAction.isIdle()) {
			selection = status;
			TimePickerDialog.show(this, status.getPublishTime());
		}
	}


	@Override
	public void onScheduleRemove(ScheduledStatus status) {
		if (!isRefreshing() && scheduleAction.isIdle()) {
			if (ConfirmDialog.show(this, ConfirmDialog.SCHEDULE_REMOVE, null)) {
				selection = status;
			}
		}
	}


	@Override
	public void onMediaClick(Media media) {
		if (!isRefreshing()) {
			switch (media.getMediaType()) {
				case Media.PHOTO:
					Intent intent = new Intent(requireActivity(), ImageViewer.class);
					intent.putExtra(ImageViewer.KEY_IMAGE_DATA, media);
					requireActivity().startActivity(intent);
					break;

				case Media.GIF:
				case Media.VIDEO:
					intent = new Intent(requireActivity(), VideoViewer.class);
					intent.putExtra(VideoViewer.KEY_VIDEO_DATA, media);
					requireActivity().startActivity(intent);
					break;

				case Media.AUDIO:
					AudioPlayerDialog.show(this, Uri.parse(media.getUrl()));
					break;
			}
		}
	}


	@Override
	public boolean onPlaceholderClick(long min_id, long max_id, int position) {
		if (!isRefreshing() && scheduleLoader.isIdle()) {
			load(min_id, max_id, position);
			return true;
		}
		return false;
	}


	@Override
	public void onConfirm(int type) {
		if (type == ConfirmDialog.SCHEDULE_REMOVE) {
			if (selection != null) {
				ScheduleAction.Param param = new ScheduleAction.Param(ScheduleAction.Param.REMOVE, selection.getId(), 0L);
				scheduleAction.execute(param, actionCallback);
			}
		}
	}


	@Override
	public void onTimeSelected(long time) {
		if (selection != null && time != 0L) {
			ScheduleAction.Param param = new ScheduleAction.Param(ScheduleAction.Param.UPDATE, selection.getId(), time);
			scheduleAction.execute(param, actionCallback);
		}
	}

	/**
	 *
	 */
	private void onLoaderResult(ScheduleLoader.Result result) {
		if (result.statuses != null) {
			if (result.index == CLEAR_LIST) {
				adapter.setItems(result.statuses);
			} else {
				adapter.addItems(result.statuses, result.index);
			}
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
	private void onActionResult(ScheduleAction.Result result) {
		Context context = getContext();
		if (context != null) {
			if (result.mode == ScheduleAction.Result.REMOVE) {
				adapter.removeItem(result.id);
				Toast.makeText(context, R.string.info_schedule_removed, Toast.LENGTH_SHORT).show();
			} else if (result.mode == ScheduleAction.Result.UPDATE) {
				if (result.status != null) {
					adapter.updateItem(result.status);
					Toast.makeText(context, R.string.info_schedule_updated, Toast.LENGTH_SHORT).show();
				}
			} else if (result.mode == ScheduleAction.Result.ERROR) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
		}
	}

	/**
	 *
	 */
	private void load(long min_id, long max_id, int position) {
		ScheduleLoader.Param param = new ScheduleLoader.Param(min_id, max_id, position);
		scheduleLoader.execute(param, loaderCallback);
	}
}