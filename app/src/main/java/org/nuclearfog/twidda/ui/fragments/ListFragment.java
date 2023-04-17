package org.nuclearfog.twidda.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.RefreshDelay;
import org.nuclearfog.twidda.backend.utils.RefreshDelay.RefreshCallback;
import org.nuclearfog.twidda.config.GlobalSettings;

import java.util.Random;

/**
 * this fragment class hosts a list view inside a swipe view
 * superclass for all list fragments
 *
 * @author nuclearfog
 */
public abstract class ListFragment extends Fragment implements OnRefreshListener, RefreshCallback {

	/**
	 * delay to enable SwipeRefreshLayout
	 */
	private static final int REFRESH_DELAY_MS = 1000;

	private static final Random rand = new Random();

	private RecyclerView list;
	private SwipeRefreshLayout reload;
	protected GlobalSettings settings;

	private boolean isRefreshing = false;
	private long sessionId = rand.nextLong();


	@Override
	public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle b) {
		list = new RecyclerView(requireContext());
		list.setLayoutManager(new LinearLayoutManager(requireContext()));
		reload = new SwipeRefreshLayout(requireContext());
		reload.setOnRefreshListener(this);
		reload.addView(list);

		settings = GlobalSettings.getInstance(requireContext());
		AppStyles.setSwipeRefreshColor(reload, settings);
		return reload;
	}


	@Override
	public final void onRefresh() {
		onReload();
	}


	@Override
	public void onRefreshDelayed() {
		if (isRefreshing && !reload.isRefreshing()) {
			reload.setRefreshing(true);
		}
	}

	/**
	 * enables or disables swipe layout
	 *
	 * @param enable true to enable swipe view delayed, false to stop immediately
	 */
	protected void setRefresh(boolean enable) {
		isRefreshing = enable;
		if (enable) {
			reload.postDelayed(new RefreshDelay(this), REFRESH_DELAY_MS);
			reload.setEnabled(false);
		} else {
			reload.setRefreshing(false);
			reload.setEnabled(true);
		}
	}

	/**
	 * check if swipe refresh is active
	 *
	 * @return true if swipe view is active
	 */
	protected boolean isRefreshing() {
		return isRefreshing || reload.isRefreshing();
	}

	/**
	 * set list adapter
	 *
	 * @param adapter adapter for the list
	 */
	protected void setAdapter(Adapter<? extends ViewHolder> adapter) {
		list.setAdapter(adapter);
	}

	/**
	 * called to reset all data
	 */
	public void reset() {
		// check if fragment is initialized
		if (reload != null && list != null && settings != null) {
			// reset colors
			AppStyles.setSwipeRefreshColor(reload, settings);
			list.setBackgroundColor(settings.getBackgroundColor());
			// force redrawing list to apply colors
			list.setAdapter(list.getAdapter());
			onReset();
		}
	}

	/**
	 * called when this tab is deselected
	 */
	public void onTabChange() {
		if (list != null) {
			list.smoothScrollToPosition(0);
		}
	}

	/**
	 * get session fragment ID
	 *
	 * @return unique session ID
	 */
	public long getSessionId() {
		return sessionId;
	}

	/**
	 * called when swipe refresh is active
	 */
	protected abstract void onReload();

	/**
	 * called to reset all data
	 */
	protected abstract void onReset();
}