package org.nuclearfog.twidda.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.UserLoader;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.backend.utils.LinkUtils;
import org.nuclearfog.twidda.model.lists.Fields;
import org.nuclearfog.twidda.ui.adapter.recyclerview.FieldAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.FieldAdapter.OnLinkClickListener;

/**
 * User field list fragment
 *
 * @author nuclearfog
 */
public class FieldFragment extends ListFragment implements OnLinkClickListener, AsyncCallback<UserLoader.Result> {

	public static final String KEY_ID = "user-id";

	private static final String KEY_SAVE = "fields-save";

	private UserLoader userLoader;
	private FieldAdapter adapter;

	private long id;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		userLoader = new UserLoader(requireContext());
		adapter = new FieldAdapter(this);

		Bundle param = getArguments();
		if (param != null) {
			id = param.getLong(KEY_ID);
		}
		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_SAVE);
			if (data instanceof Fields) {
				adapter.replaceItems((Fields) data);
			}
		}
		setAdapter(adapter);
		UserLoader.Param userParam = new UserLoader.Param(UserLoader.Param.ONLINE, id);
		userLoader.execute(userParam, this);
		setRefresh(true);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		Fields items = adapter.getItems();
		outState.putSerializable(KEY_SAVE, items);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onLinkClick(String url) {
		LinkUtils.openLink(requireActivity(), url);
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
		UserLoader.Param userParam = new UserLoader.Param(UserLoader.Param.ONLINE, id);
		userLoader.execute(userParam, this);
		setRefresh(true);
	}


	@Override
	public void onResult(@NonNull UserLoader.Result result) {
		if (result.mode == UserLoader.Result.ONLINE) {
			if (result.user != null) {
				Fields fields = new Fields(result.user.getFields());
				adapter.replaceItems(fields);
			} else {
				adapter.clear();
			}
		} else if (result.mode == UserLoader.Result.ERROR) {
			ErrorUtils.showErrorMessage(requireContext(), result.exception);
		}
		setRefresh(false);
	}
}