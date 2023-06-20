package org.nuclearfog.twidda.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Filter;
import org.nuclearfog.twidda.ui.adapter.holder.FilterHolder;
import org.nuclearfog.twidda.ui.adapter.holder.OnHolderClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class FilterAdapter extends Adapter<FilterHolder> implements OnHolderClickListener {



	private List<Filter> items = new ArrayList<>();


	public FilterAdapter() {

	}


	@NonNull
	@Override
	public FilterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new FilterHolder(parent, this);
	}


	@Override
	public void onBindViewHolder(@NonNull FilterHolder holder, int position) {
		holder.setData(items.get(position));
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {

	}


	@Override
	public boolean onPlaceholderClick(int index) {
		return false;
	}
}