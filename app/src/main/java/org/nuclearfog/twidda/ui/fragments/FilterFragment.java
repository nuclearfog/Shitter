package org.nuclearfog.twidda.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.ui.adapter.FilterAdapter;

public class FilterFragment extends ListFragment {

	private FilterAdapter adapter;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		adapter = new FilterAdapter();
	}


	@Override
	protected void onReload() {
	}


	@Override
	protected void onReset() {
	}
}