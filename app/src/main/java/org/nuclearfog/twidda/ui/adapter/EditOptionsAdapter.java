package org.nuclearfog.twidda.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

/**
 *
 */
public class EditOptionsAdapter extends Adapter {


	public EditOptionsAdapter(Context context) {}


	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return null;
	}


	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

	}


	@Override
	public int getItemCount() {
		return 0;
	}


	public interface OnOptionChangedListener {


	}
}