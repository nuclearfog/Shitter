package org.nuclearfog.twidda.ui.fragments;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.ui.activities.SearchActivity.KEY_SEARCH_QUERY;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.ui.adapter.TrendAdapter;
import org.nuclearfog.twidda.ui.adapter.TrendAdapter.TrendClickListener;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.TrendLoader;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.ui.activities.SearchActivity;

import java.util.List;

/**
 * Fragment class to show a list of trends
 *
 * @author nuclearfog
 */
public class TrendFragment extends ListFragment implements TrendClickListener {

	/**
	 * additional bundle key to set search string for hashtags
	 */
	public static final String KEY_HASHTAG_SEARCH = "trend_search_hashtags";

	private TrendLoader trendTask;
	private TrendAdapter adapter;

	private String search = "";


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		adapter = new TrendAdapter(settings, this);
		setAdapter(adapter);
		Bundle args = getArguments();
		if (args != null) {
			search = args.getString(KEY_HASHTAG_SEARCH, "");
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		if (trendTask == null) {
			load();
			setRefresh(true);
		}
	}


	@Override
	protected void onReset() {
		adapter = new TrendAdapter(settings, this);
		setAdapter(adapter);
		load();
		setRefresh(true);
	}


	@Override
	public void onDestroy() {
		if (trendTask != null && trendTask.getStatus() == RUNNING)
			trendTask.cancel(true);
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		load();
	}


	@Override
	public void onTrendClick(Trend trend) {
		if (!isRefreshing()) {
			Intent intent = new Intent(requireContext(), SearchActivity.class);
			String name = trend.getName();
			if (!name.startsWith("#") && !name.startsWith("\"") && !name.endsWith("\""))
				name = "\"" + name + "\"";
			intent.putExtra(KEY_SEARCH_QUERY, name);
			startActivity(intent);
		}
	}

	/**
	 * check if list is empty
	 *
	 * @return true if list is empty
	 */
	public boolean isEmpty() {
		return adapter.isEmpty();
	}

	/**
	 * set trend data to list
	 *
	 * @param data Trend data
	 */
	public void setData(@NonNull List<Trend> data) {
		adapter.replaceItems(data);
		setRefresh(false);
	}

	/**
	 * called from {@link TrendLoader} if an error occurs
	 */
	public void onError(@Nullable ConnectionException exception) {
		String message = ErrorHandler.getErrorMessage(requireContext(), exception);
		Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
		setRefresh(false);
	}

	/**
	 * load content into the list
	 */
	private void load() {
		trendTask = new TrendLoader(this);
		trendTask.execute(search);
	}
}