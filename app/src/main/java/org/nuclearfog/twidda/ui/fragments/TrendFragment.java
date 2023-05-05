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
import org.nuclearfog.twidda.lists.Trends;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.ui.activities.SearchActivity;
import org.nuclearfog.twidda.ui.adapter.TrendAdapter;
import org.nuclearfog.twidda.ui.adapter.TrendAdapter.TrendClickListener;

import java.io.Serializable;

/**
 * Fragment class to show a list of trends
 *
 * @author nuclearfog
 */
public class TrendFragment extends ListFragment implements TrendClickListener, AsyncCallback<TrendResult> {

	/**
	 * additional bundle key to set search string for hashtags
	 * value type is String
	 */
	public static final String KEY_FRAGMENT_TREND_SEARCH = "trend_search_hashtags";

	/**
	 * bundle key to add adapter items
	 * value type is {@link Trend[]}
	 */
	private static final String KEY_FRAGMENT_TREND_DATA = "trend_data";


	private TrendLoader trendLoader;
	private TrendAdapter adapter;

	private String search = "";


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		adapter = new TrendAdapter(settings, this);
		trendLoader = new TrendLoader(requireContext());
		setAdapter(adapter);

		Bundle args = getArguments();
		if (args != null) {
			search = args.getString(KEY_FRAGMENT_TREND_SEARCH, "");
		}
		if (savedInstanceState != null) {
			Serializable data = savedInstanceState.getSerializable(KEY_FRAGMENT_TREND_DATA);
			if (data instanceof Trends) {
				adapter.addItems((Trends) data, TrendAdapter.CLEAR_LIST);
				return;
			}
		}
		setRefresh(true);
		load(TrendParameter.NO_CURSOR, TrendAdapter.CLEAR_LIST);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_FRAGMENT_TREND_DATA, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onDestroy() {
		trendLoader.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReset() {
		adapter.clear();
		setRefresh(true);
		load(TrendParameter.NO_CURSOR, TrendAdapter.CLEAR_LIST);
	}


	@Override
	protected void onReload() {
		load(TrendParameter.NO_CURSOR, TrendAdapter.CLEAR_LIST);
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
	public boolean onPlaceholderClick(long cursor, int index) {
		if (trendLoader.isIdle()) {
			load(cursor, index);
			return true;
		}
		return false;
	}


	@Override
	public void onResult(@NonNull TrendResult result) {
		if (result.trends != null) {
			adapter.addItems(result.trends, result.index);
		} else if (getContext() != null) {
			String message = ErrorHandler.getErrorMessage(getContext(), result.exception);
			Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
		}
		setRefresh(false);
	}

	/**
	 * load content into the list
	 */
	private void load(long cursor, int index) {
		TrendParameter param;
		if (!search.trim().isEmpty())
			param = new TrendParameter(TrendLoader.SEARCH, index, search, cursor);
		else if (adapter.isEmpty())
			param = new TrendParameter(TrendLoader.DATABASE,  index, search, cursor);
		else
			param = new TrendParameter(TrendLoader.ONLINE,  index, search, cursor);
		trendLoader.execute(param, this);
	}
}