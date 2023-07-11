package org.nuclearfog.twidda.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.utils.LinkUtils;
import org.nuclearfog.twidda.model.lists.Fields;
import org.nuclearfog.twidda.ui.adapter.recyclerview.FieldAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.FieldAdapter.OnLinkClickListener;

/**
 * User field list fragment
 *
 * @author nuclearfog
 */
public class FieldFragment extends ListFragment implements OnLinkClickListener {

	private static final String KEY_SAVE = "fields-save";

	private FieldAdapter adapter;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		adapter = new FieldAdapter(this);
		setAdapter(adapter);
		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_SAVE);
			if (data instanceof Fields) {
				adapter.replaceItems((Fields) data);
			}
		}
		disableSwipe();
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
	}


	@Override
	protected void onReset() {
	}

	/**
	 * set field items
	 *
	 * @param items new items to show in a list
	 */
	public void setItems(Fields items) {
		if (adapter != null) {
			adapter.replaceItems(items);
		}
	}
}