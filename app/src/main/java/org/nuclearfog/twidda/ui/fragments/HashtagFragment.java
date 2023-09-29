package org.nuclearfog.twidda.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.HashtagAction;
import org.nuclearfog.twidda.backend.async.HashtagAction.HashtagActionParam;
import org.nuclearfog.twidda.backend.async.HashtagAction.HashtagActionResult;
import org.nuclearfog.twidda.backend.async.HashtagLoader;
import org.nuclearfog.twidda.backend.async.HashtagLoader.HashtagLoaderParam;
import org.nuclearfog.twidda.backend.async.HashtagLoader.HashtagLoaderResult;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.Hashtag;
import org.nuclearfog.twidda.model.lists.Trends;
import org.nuclearfog.twidda.ui.activities.SearchActivity;
import org.nuclearfog.twidda.ui.adapter.recyclerview.HashtagAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.HashtagAdapter.OnHashtagClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;

import java.io.Serializable;

/**
 * Fragment class to show a list of trends
 *
 * @author nuclearfog
 */
public class HashtagFragment extends ListFragment implements OnHashtagClickListener, ActivityResultCallback<ActivityResult>, ConfirmDialog.OnConfirmListener {

	/**
	 * setup fragment to show popular trends of an instance/location
	 */
	public static final int MODE_POPULAR = 0x32105718;

	/**
	 * setup fragment to show hashtags relating to search
	 * requires {@link #KEY_SEARCH}
	 */
	public static final int MODE_SEARCH = 0x17210512;

	/**
	 * setup fragment to show hashtags followed by the current user
	 */
	public static final int MODE_FOLLOW = 0x50545981;

	/**
	 * setup fragment to view featured hashtags
	 */
	public static final int MODE_FEATURE = 0x16347583;

	/**
	 * key used to define what type of trends should be shown, see {@link #MODE_FOLLOW ,#MODE_POPULAR ,#KEY_FRAGMENT_TREND_SEARCH}
	 * value type is Integer
	 */
	public static final String KEY_MODE = "fragment_trend_mode";

	/**
	 * (optional) key to search for trends and hashtag matching a search string
	 * value type is String
	 */
	public static final String KEY_SEARCH = "fragment_trend_search";

	/**
	 * bundle key to add adapter items
	 * value type is {@link Trends}
	 */
	private static final String KEY_DATA = "fragment_trend_data";

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private AsyncCallback<HashtagActionResult> hashtagActionCallback = this::onHashtagActionResult;
	private AsyncCallback<HashtagLoaderResult> hashtagLoaderCallback = this::onHashtagLoaderResult;

	private HashtagLoader hashtagLoader;
	private HashtagAction hashtagAction;
	private HashtagAdapter adapter;
	private ConfirmDialog confirmDialog;

	private int mode = 0;
	private String search = "";
	private Hashtag selection;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		adapter = new HashtagAdapter(this);
		hashtagLoader = new HashtagLoader(requireContext());
		hashtagAction = new HashtagAction(requireContext());
		confirmDialog = new ConfirmDialog(requireActivity(), this);
		setAdapter(adapter);

		Bundle args = getArguments();
		if (args != null) {
			search = args.getString(KEY_SEARCH, "");
			mode = args.getInt(KEY_MODE, 0);
		}
		if (mode == MODE_FOLLOW || mode == MODE_FEATURE) {
			adapter.enableDelete();
		}
		if (savedInstanceState != null) {
			Serializable data = savedInstanceState.getSerializable(KEY_DATA);
			if (data instanceof Trends) {
				adapter.addItems((Trends) data, HashtagAdapter.CLEAR_LIST);
				return;
			}
		}
		setRefresh(true);
		load(HashtagLoaderParam.NO_CURSOR, HashtagAdapter.CLEAR_LIST);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_DATA, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onDestroy() {
		hashtagLoader.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReset() {
		adapter.clear();
		hashtagLoader = new HashtagLoader(requireContext());
		load(HashtagLoaderParam.NO_CURSOR, HashtagAdapter.CLEAR_LIST);
		setRefresh(true);
	}


	@Override
	protected void onReload() {
		load(HashtagLoaderParam.NO_CURSOR, HashtagAdapter.CLEAR_LIST);
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		if (result.getResultCode() == SearchActivity.RETURN_TREND) {
			if (result.getData() != null) {
				Serializable data = result.getData().getSerializableExtra(SearchActivity.KEY_DATA);
				if (data instanceof Hashtag) {
					Hashtag update = (Hashtag) data;
					// remove hashtag if unfollowed
					if (mode == MODE_FOLLOW && !update.following()) {
						adapter.removeItem(update);
					}
				}
			}
		}
	}


	@Override
	public void onHashtagClick(Hashtag hashtag, int action) {
		if (action == OnHashtagClickListener.SELECT) {
			if (!isRefreshing()) {
				Intent intent = new Intent(requireContext(), SearchActivity.class);
				String name = hashtag.getName();
				if (!name.startsWith("#") && !name.startsWith("\"") && !name.endsWith("\"")) {
					name = "\"" + name + "\"";
					intent.putExtra(SearchActivity.KEY_QUERY, name);
				} else {
					intent.putExtra(SearchActivity.KEY_DATA, hashtag);
				}
				activityResultLauncher.launch(intent);
			}
		} else if (action == OnHashtagClickListener.REMOVE) {
			if (!confirmDialog.isShowing()) {
				selection = hashtag;
				if (mode == MODE_FEATURE) {
					confirmDialog.show(ConfirmDialog.UNFEATURE_HASHTAG);
				} else if (mode == MODE_FOLLOW) {
					confirmDialog.show(ConfirmDialog.UNFOLLOW_HASHTAG);
				}
			}
		}
	}


	@Override
	public boolean onPlaceholderClick(long cursor, int index) {
		if (hashtagLoader.isIdle()) {
			load(cursor, index);
			return true;
		}
		return false;
	}


	@Override
	public void onConfirm(int type, boolean remember) {
		if (selection != null) {
			if (type == ConfirmDialog.UNFOLLOW_HASHTAG) {
				HashtagActionParam param = new HashtagActionParam(HashtagActionParam.UNFOLLOW, selection.getName(), selection.getId());
				hashtagAction.execute(param, hashtagActionCallback);
			} else if (type == ConfirmDialog.UNFEATURE_HASHTAG) {
				HashtagActionParam param = new HashtagActionParam(HashtagActionParam.UNFEATURE, selection.getName(), selection.getId());
				hashtagAction.execute(param, hashtagActionCallback);
			}
		}
	}

	/**
	 * callback for {@link HashtagAction}
	 */
	private void onHashtagActionResult(@NonNull HashtagActionResult result) {
		if (result.mode == HashtagActionResult.UNFEATURE) {
			Toast.makeText(requireContext(), R.string.info_hashtag_unfeatured, Toast.LENGTH_SHORT).show();
			adapter.removeItem(result.hashtag);
		} else if (result.mode == HashtagActionResult.UNFOLLOW) {
			Toast.makeText(requireContext(), R.string.info_hashtag_unfollowed, Toast.LENGTH_SHORT).show();
			adapter.removeItem(result.hashtag);
		} else if (result.mode == HashtagActionResult.ERROR) {
			ErrorUtils.showErrorMessage(requireContext(), result.exception);
		}
	}

	/**
	 * callback for {@link HashtagLoader}
	 */
	private void onHashtagLoaderResult(@NonNull HashtagLoaderResult result) {
		if (result.mode == HashtagLoaderResult.ERROR) {
			if (getContext() != null) {
				ErrorUtils.showErrorMessage(getContext(), result.exception);
			}
			adapter.disableLoading();
		} else {
			adapter.addItems(result.trends, result.index);
		}
		setRefresh(false);
	}

	/**
	 * load content into the list
	 */
	private void load(long cursor, int index) {
		HashtagLoaderParam param;
		switch (mode) {
			case MODE_POPULAR:
				if (adapter.isEmpty()) {
					param = new HashtagLoaderParam(HashtagLoaderParam.POPULAR_OFFLINE, index, search, cursor);
				} else {
					param = new HashtagLoaderParam(HashtagLoaderParam.POPULAR_ONLINE, index, search, cursor);
				}
				hashtagLoader.execute(param, hashtagLoaderCallback);
				break;

			case MODE_FOLLOW:
				param = new HashtagLoaderParam(HashtagLoaderParam.FOLLOWING, index, search, cursor);
				hashtagLoader.execute(param, hashtagLoaderCallback);
				break;

			case MODE_FEATURE:
				param = new HashtagLoaderParam(HashtagLoaderParam.FEATURING, index, search, cursor);
				hashtagLoader.execute(param, hashtagLoaderCallback);
				break;

			case MODE_SEARCH:
				param = new HashtagLoaderParam(HashtagLoaderParam.SEARCH, index, search, cursor);
				hashtagLoader.execute(param, hashtagLoaderCallback);
				break;
		}
	}
}