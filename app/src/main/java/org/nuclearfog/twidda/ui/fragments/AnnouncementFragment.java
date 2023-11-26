package org.nuclearfog.twidda.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.async.AnnouncementLoader;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.Announcement;
import org.nuclearfog.twidda.model.lists.Announcements;
import org.nuclearfog.twidda.ui.adapter.recyclerview.AnnouncementAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.AnnouncementAdapter.OnAnnouncementClickListener;

/**
 * ListFragment used to show instance announcements
 *
 * @author nuclearfog
 */
public class AnnouncementFragment extends ListFragment implements OnAnnouncementClickListener {

	/**
	 * Bundle key used to save RecyclerView adapter content
	 */
	private static final String KEY_SAVE = "save-anncouncements";

	private AnnouncementAdapter adapter;
	private AnnouncementLoader announcementLoader;

	private AsyncCallback<AnnouncementLoader.Result> announcementResult = this::onAnnouncementResult;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		adapter = new AnnouncementAdapter(this);
		announcementLoader = new AnnouncementLoader(requireContext());
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
			announcementLoader.execute(null, announcementResult);
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
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		announcementLoader.execute(null, announcementResult);
	}


	@Override
	protected void onReset() {
		adapter.clear();
		announcementLoader = new AnnouncementLoader(requireContext());
		announcementLoader.execute(null, announcementResult);
		setRefresh(true);
	}


	@Override
	public void onAnnouncementClick(Announcement announcement) {
		// todo implement this
	}

	/**
	 *
	 */
	private void onAnnouncementResult(AnnouncementLoader.Result result) {
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
}