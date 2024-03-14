package org.nuclearfog.twidda.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.UserLoader;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.backend.utils.LinkUtils;
import org.nuclearfog.twidda.model.lists.Emojis;
import org.nuclearfog.twidda.model.lists.Fields;
import org.nuclearfog.twidda.ui.adapter.recyclerview.FieldAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.FieldAdapter.OnLinkClickListener;

/**
 * User field list fragment
 *
 * @author nuclearfog
 */
public class FieldFragment extends ListFragment implements OnLinkClickListener, AsyncCallback<UserLoader.Result> {

	/**
	 * Bundle key used to set user ID
	 * value type is Long
	 */
	public static final String KEY_ID = "user-id";

	/**
	 * Bundle key used to save/restore fields
	 * value type is {@link Fields}
	 */
	private static final String KEY_FIELDS = "fields-save";

	/**
	 * Bundle key used to save/restore emoji list
	 * value type is {@link Emojis}
	 */
	private static final String KEY_EMOJIS = "emojis-save";

	private UserLoader userLoader;
	private FieldAdapter adapter;

	private long id;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		userLoader = new UserLoader(requireContext());
		adapter = new FieldAdapter(this);
		setAdapter(adapter, false);

		Bundle param = getArguments();
		if (param != null) {
			id = param.getLong(KEY_ID);
		}
		if (savedInstanceState != null) {
			Object fieldData = savedInstanceState.getSerializable(KEY_FIELDS);
			Object emojiData = savedInstanceState.getSerializable(KEY_EMOJIS);
			if (fieldData instanceof Fields) {
				adapter.setItems((Fields) fieldData, (Emojis) emojiData);
			}
		}
		if (adapter.isEmpty()) {
			UserLoader.Param userParam = new UserLoader.Param(UserLoader.Param.LOCAL, id);
			userLoader.execute(userParam, this);
		}
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_FIELDS, adapter.getItems());
		outState.putSerializable(KEY_EMOJIS, adapter.getEmojis());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onDestroy() {
		userLoader.cancel();
		super.onDestroy();
	}


	@Override
	public void onLinkClick(String url) {
		if (!isRefreshing()) {
			Activity parent = getActivity();
			if (parent != null) {
				LinkUtils.redirectToBrowser(parent, url);
			}
		}
	}


	@Override
	protected void onReload() {
		UserLoader.Param userParam = new UserLoader.Param(UserLoader.Param.ONLINE, id);
		userLoader.execute(userParam, this);
	}


	@Override
	protected void onReset() {
		// reload adapter items
		adapter.clear();
		setRefresh(true);
		userLoader = new UserLoader(requireContext());
		UserLoader.Param userParam = new UserLoader.Param(UserLoader.Param.LOCAL, id);
		userLoader.execute(userParam, this);
	}


	@Override
	public void onResult(@NonNull UserLoader.Result result) {
		if (result.user != null) {
			Fields fields = new Fields(result.user.getFields());
			Emojis emojis = new Emojis(result.user.getEmojis());
			adapter.setItems(fields, emojis);
		} else {
			Context context = getContext();
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
		}
		setRefresh(false);
	}
}