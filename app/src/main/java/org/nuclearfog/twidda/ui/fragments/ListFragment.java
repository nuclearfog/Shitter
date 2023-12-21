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

import org.nuclearfog.twidda.R;
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

	/**
	 * used by {@link ItemViewModel} to notify {@link ListFragment} subclasses to scroll the lists to the first position
	 */
	public static final String NOTIFY_SCROLL_TOP = "refresh";

	/**
	 * used by {@link ItemViewModel} to notify {@link ListFragment} subclassed that settings may have changed
	 */
	public static final String NOTIFY_CHANGED = "settings_changed";

	/**
	 * delay to enable SwipeRefreshLayout
	 */
	private static final int REFRESH_DELAY_MS = 1000;

	private RecyclerView list;
	private SwipeRefreshLayout reload;

	private LinearLayoutManager layoutManager;
	private ItemViewModel viewModel;
	protected GlobalSettings settings;

	private boolean enableSwipe = true;
	private boolean isRefreshing = false;


	@Override
	public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle b) {
		View root = inflater.inflate(R.layout.fragment_list, parent, false);
		list = root.findViewById(R.id.fragment_list);
		reload = root.findViewById(R.id.fragment_refresh);
		settings = GlobalSettings.get(requireContext());
		layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
		viewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);

		list.setLayoutManager(layoutManager);
		AppStyles.setSwipeRefreshColor(reload, settings);
		viewModel.getSelectedItem().observe(getViewLifecycleOwner(), this);

		reload.setOnRefreshListener(this);
		return root;
	}


	@Override
	public void onDestroyView() {
		viewModel.getSelectedItem().removeObserver(this);
		super.onDestroyView();
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
				if (layoutManager.getItemCount() > 0) {
					if (layoutManager.getReverseLayout()) {
						list.smoothScrollToPosition(layoutManager.getItemCount() - 1);
					} else {
						list.smoothScrollToPosition(0);
					}
				}
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
	 * @param reverse true to reverse the list
	 */
	protected void setAdapter(Adapter<? extends ViewHolder> adapter, boolean reverse) {
		layoutManager.setReverseLayout(reverse);
		layoutManager.setStackFromEnd(reverse);
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

	/**
	 * View model used by Activities to communicate with {@link ListFragment} subclasses
	 */
	public static class ItemViewModel extends ViewModel {

		private final MutableLiveData<String> selectedItem = new MutableLiveData<>();

		/**
		 * send notification to {@link ListFragment} subclasses
		 *
		 * @param s notification type {@link ListFragment#NOTIFY_CHANGED,#ListFragment#NOTIFY_SCROLL_TOP}
		 */
		public void notify(String s) {
			selectedItem.setValue(s);
		}

		/**
		 *
		 */
		public LiveData<String> getSelectedItem() {
			return selectedItem;
		}
	}
}