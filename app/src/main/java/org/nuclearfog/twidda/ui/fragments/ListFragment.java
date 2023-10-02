package org.nuclearfog.twidda.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
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

/**
 * this fragment class hosts a list view inside a swipe view
 * superclass for all list fragments
 *
 * @author nuclearfog
 */
public abstract class ListFragment extends Fragment implements OnRefreshListener, RefreshCallback, Observer<String> {

	public static final String NOTIFY_SCROLL_TOP = "refresh";
	public static final String NOTIFY_CHANGED = "settings_changed";

	/**
	 * delay to enable SwipeRefreshLayout
	 */
	private static final int REFRESH_DELAY_MS = 1000;

	private RecyclerView list;
	private SwipeRefreshLayout reload;
	protected GlobalSettings settings;

	private boolean enableSwipe = true;
	private boolean isRefreshing = false;


	@Override
	public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle b) {
		list = new RecyclerView(inflater.getContext());
		reload = new SwipeRefreshLayout(inflater.getContext());
		reload.addView(list);
		settings = GlobalSettings.get(requireContext());

		list.setLayoutManager(new LinearLayoutManager(requireContext()));
		AppStyles.setSwipeRefreshColor(reload, settings);

		ItemViewModel viewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
		viewModel.getSelectedItem().observe(getViewLifecycleOwner(), this);

		reload.setOnRefreshListener(this);
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


	@Override
	public void onChanged(String s) {
		switch (s) {
			case NOTIFY_CHANGED:
				// reset colors
				AppStyles.setSwipeRefreshColor(reload, settings);
				list.setBackgroundColor(settings.getBackgroundColor());
				// force redrawing list to apply colors
				list.setAdapter(list.getAdapter());
				onReset();
				break;

			case NOTIFY_SCROLL_TOP:
				list.smoothScrollToPosition(0);
				break;
		}
	}

	/**
	 * enables or disables swipe layout
	 *
	 * @param enable true to enable swipe view delayed, false to stop immediately
	 */
	protected void setRefresh(boolean enable) {
		if (enableSwipe) {
			isRefreshing = enable;
			if (enable) {
				reload.postDelayed(new RefreshDelay(this), REFRESH_DELAY_MS);
				reload.setEnabled(false);
			} else {
				reload.setRefreshing(false);
				reload.setEnabled(true);
			}
		}
	}

	/**
	 * check if swipe refresh is active
	 *
	 * @return true if swipe view is active
	 */
	protected boolean isRefreshing() {
		return enableSwipe && (isRefreshing || reload.isRefreshing());
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
	 * called when swipe refresh is active
	 */
	protected abstract void onReload();

	/**
	 * called to reset all data
	 */
	protected abstract void onReset();


	public static class ItemViewModel extends ViewModel {

		private final MutableLiveData<String> selectedItem = new MutableLiveData<>();

		public void notify(String s) {
			selectedItem.setValue(s);
		}

		public LiveData<String> getSelectedItem() {
			return selectedItem;
		}
	}
}