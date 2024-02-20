package org.nuclearfog.twidda.ui.fragments;

import android.content.Context;
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
import org.nuclearfog.twidda.backend.async.TagAction;
import org.nuclearfog.twidda.backend.async.TagLoader;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.Tag;
import org.nuclearfog.twidda.model.lists.Tags;
import org.nuclearfog.twidda.ui.activities.SearchActivity;
import org.nuclearfog.twidda.ui.adapter.recyclerview.TagAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.TagAdapter.OnTagClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;

/**
 * Fragment class to show a list of trends
 *
 * @author nuclearfog
 */
public class TagFragment extends ListFragment implements OnTagClickListener, ActivityResultCallback<ActivityResult>, ConfirmDialog.OnConfirmListener {

	/**
	 * setup fragment to show popular trends of an instance/location
	 */
	public static final int MODE_POPULAR = 0x32105718;

	/**
	 * setup fragment to show tags relating to search
	 * requires {@link #KEY_SEARCH}
	 */
	public static final int MODE_SEARCH = 0x17210512;

	/**
	 * setup fragment to show tags followed by the current user
	 */
	public static final int MODE_FOLLOW = 0x50545981;

	/**
	 * setup fragment to view featured tags
	 */
	public static final int MODE_FEATURE = 0x16347583;

	/**
	 * setup fragment to view suggestions for tags features
	 */
	public static final int MODE_SUGGESTIONS = 0x4422755;

	/**
	 * key used to define what type of trends should be shown, see {@link #MODE_FOLLOW ,#MODE_POPULAR ,#KEY_FRAGMENT_TREND_SEARCH}
	 * value type is Integer
	 */
	public static final String KEY_MODE = "fragment_trend_mode";

	/**
	 * (optional) key to search for trends and tags matching a search string
	 * value type is String
	 */
	public static final String KEY_SEARCH = "fragment_trend_search";

	/**
	 * bundle key to add adapter items
	 * value type is {@link Tags}
	 */
	private static final String KEY_DATA = "fragment_trend_data";

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private AsyncCallback<TagAction.Result> tagActionCallback = this::onTagActionResult;
	private AsyncCallback<TagLoader.Result> tagLoaderCallback = this::onTagLoaderResult;

	private TagLoader tagLoader;
	private TagAction tagAction;

	private TagAdapter adapter;

	private int mode = 0;
	private String search = "";
	private Tag selection;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		adapter = new TagAdapter(this);
		tagLoader = new TagLoader(requireContext());
		tagAction = new TagAction(requireContext());
		setAdapter(adapter, false);

		Bundle args = getArguments();
		if (args != null) {
			search = args.getString(KEY_SEARCH, "");
			mode = args.getInt(KEY_MODE, 0);
		}
		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_DATA);
			if (data instanceof Tags) {
				adapter.setItems((Tags) data);
			}
		}
		if (mode == MODE_FOLLOW || mode == MODE_FEATURE) {
			adapter.enableDelete();
		}
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_DATA, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onStart() {
		super.onStart();
		if (adapter.isEmpty()) {
			setRefresh(true);
			load(TagLoader.Param.NO_CURSOR, TagAdapter.CLEAR_LIST);
		}
	}


	@Override
	public void onDestroy() {
		tagLoader.cancel();
		tagAction.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReset() {
		adapter.clear();
		tagLoader = new TagLoader(requireContext());
		tagAction = new TagAction(requireContext());
		load(TagLoader.Param.NO_CURSOR, TagAdapter.CLEAR_LIST);
		setRefresh(true);
	}


	@Override
	protected void onReload() {
		load(TagLoader.Param.NO_CURSOR, TagAdapter.CLEAR_LIST);
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		if (result.getResultCode() == SearchActivity.RETURN_TREND) {
			if (result.getData() != null) {
				Object data = result.getData().getSerializableExtra(SearchActivity.KEY_DATA);
				if (data instanceof Tag) {
					Tag update = (Tag) data;
					// remove tag if unfollowed
					if (mode == MODE_FOLLOW && !update.following()) {
						adapter.removeItem(update);
					}
				}
			}
		}
	}


	@Override
	public void onTagClick(Tag tag, int action) {
		if (!isRefreshing()) {
			if (action == OnTagClickListener.SELECT) {
				if (!isRefreshing()) {
					Intent intent = new Intent(requireContext(), SearchActivity.class);
					String name = tag.getName();
					if (!name.startsWith("#") && !name.startsWith("\"") && !name.endsWith("\"")) {
						name = "\"" + name + "\"";
						intent.putExtra(SearchActivity.KEY_QUERY, name);
					} else {
						intent.putExtra(SearchActivity.KEY_DATA, tag);
					}
					activityResultLauncher.launch(intent);
				}
			} else if (action == OnTagClickListener.REMOVE) {
				if (tagAction.isIdle()) {
					boolean created = false;
					if (mode == MODE_FEATURE) {
						created = ConfirmDialog.show(this, ConfirmDialog.UNFEATURE_TAG, null);
					} else if (mode == MODE_FOLLOW) {
						created = ConfirmDialog.show(this, ConfirmDialog.UNFOLLOW_TAG, null);
					}
					if (created) {
						selection = tag;
					}
				}
			}
		}
	}


	@Override
	public boolean onPlaceholderClick(long cursor, int index) {
		if (!isRefreshing() && tagLoader.isIdle()) {
			load(cursor, index);
			return true;
		}
		return false;
	}


	@Override
	public void onConfirm(int type) {
		if (selection != null) {
			if (type == ConfirmDialog.UNFOLLOW_TAG) {
				TagAction.Param param = new TagAction.Param(TagAction.Param.UNFOLLOW, selection.getName(), selection.getId());
				tagAction.execute(param, tagActionCallback);
			} else if (type == ConfirmDialog.UNFEATURE_TAG) {
				TagAction.Param param = new TagAction.Param(TagAction.Param.UNFEATURE, selection.getName(), selection.getId());
				tagAction.execute(param, tagActionCallback);
			}
		}
	}

	/**
	 * callback for {@link TagAction}
	 */
	private void onTagActionResult(@NonNull TagAction.Result result) {
		Context context = getContext();
		if (context != null) {
			if (result.mode == TagAction.Result.UNFEATURE) {
				Toast.makeText(context, R.string.info_tag_unfeatured, Toast.LENGTH_SHORT).show();
				adapter.removeItem(result.tag);
			} else if (result.mode == TagAction.Result.UNFOLLOW) {
				Toast.makeText(context, R.string.info_tag_unfollowed, Toast.LENGTH_SHORT).show();
				adapter.removeItem(result.tag);
			} else if (result.mode == TagAction.Result.ERROR) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
		}
	}

	/**
	 * callback for {@link TagLoader}
	 */
	private void onTagLoaderResult(@NonNull TagLoader.Result result) {
		if (result.mode == TagLoader.Result.ERROR) {
			Context context = getContext();
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
			adapter.disableLoading();
		} else {
			adapter.addItems(result.tags, result.index);
		}
		setRefresh(false);
	}

	/**
	 * load content into the list
	 */
	private void load(long cursor, int index) {
		TagLoader.Param param;
		switch (mode) {
			case MODE_POPULAR:
				if (adapter.isEmpty()) {
					param = new TagLoader.Param(TagLoader.Param.POPULAR_OFFLINE, index, search, cursor);
				} else {
					param = new TagLoader.Param(TagLoader.Param.POPULAR_ONLINE, index, search, cursor);
				}
				tagLoader.execute(param, tagLoaderCallback);
				break;

			case MODE_FOLLOW:
				param = new TagLoader.Param(TagLoader.Param.FOLLOWING, index, search, cursor);
				tagLoader.execute(param, tagLoaderCallback);
				break;

			case MODE_FEATURE:
				param = new TagLoader.Param(TagLoader.Param.FEATURING, index, search, cursor);
				tagLoader.execute(param, tagLoaderCallback);
				break;

			case MODE_SEARCH:
				param = new TagLoader.Param(TagLoader.Param.SEARCH, index, search, cursor);
				tagLoader.execute(param, tagLoaderCallback);
				break;

			case MODE_SUGGESTIONS:
				param = new TagLoader.Param(TagLoader.Param.SUGGESTIONS, index, search, cursor);
				tagLoader.execute(param, tagLoaderCallback);
				break;

		}
	}
}