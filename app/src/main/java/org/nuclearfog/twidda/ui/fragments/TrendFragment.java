package org.nuclearfog.twidda.ui.fragments;

import static org.nuclearfog.twidda.ui.activities.SearchActivity.KEY_SEARCH_QUERY;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.TrendLoader;
import org.nuclearfog.twidda.backend.async.TrendLoader.TrendParameter;
import org.nuclearfog.twidda.backend.async.TrendLoader.TrendResult;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.ui.activities.SearchActivity;
import org.nuclearfog.twidda.ui.adapter.TrendAdapter;
import org.nuclearfog.twidda.ui.adapter.TrendAdapter.TrendClickListener;

/**
 * Fragment class to show a list of trends
 *
 * @author nuclearfog
 */
public class TrendFragment extends ListFragment implements TrendClickListener, AsyncCallback<TrendResult> {

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
		trendTask = new TrendLoader(requireContext());
		setAdapter(adapter);
		Bundle args = getArguments();
		if (args != null) {
			search = args.getString(KEY_HASHTAG_SEARCH, "");
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		if (adapter.isEmpty()) {
			load();
			setRefresh(true);
		}
	}


	@Override
	protected void onReset() {
		adapter = new TrendAdapter(settings, this);
		setAdapter(adapter);
		setRefresh(true);
		load();
	}


	@Override
	public void onDestroy() {
		trendTask.cancel();
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


	@Override
	public void onResult(TrendResult result) {
		setRefresh(false);
		if (result.trends != null) {
			adapter.replaceItems(result.trends);
		} else if (getContext() != null) {
			String message = ErrorHandler.getErrorMessage(getContext(), result.exception);
			Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
			setRefresh(false);
		}
	}

	/**
	 * load content into the list
	 */
	private void load() {
		TrendParameter param;
		if (!search.trim().isEmpty())
			param = new TrendParameter(TrendLoader.SEARCH, search);
		else if (adapter.isEmpty())
			param = new TrendParameter(TrendLoader.DATABASE, search);
		else
			param = new TrendParameter(TrendLoader.ONLINE, search);
		trendTask.execute(param, this);
	}
}