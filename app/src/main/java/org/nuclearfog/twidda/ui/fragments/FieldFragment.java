package org.nuclearfog.twidda.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.lists.Fields;
import org.nuclearfog.twidda.ui.adapter.FieldAdapter;

public class FieldFragment extends ListFragment {

	private FieldAdapter adapter;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		adapter = new FieldAdapter();
		setAdapter(adapter);
		disableSwipe();
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
	}


	@Override
	protected void onReload() {
	}


	@Override
	protected void onReset() {
	}


	public void setItems(Fields items) {
		if (adapter != null) {
			adapter.replaceItems(items);
		}
	}
}