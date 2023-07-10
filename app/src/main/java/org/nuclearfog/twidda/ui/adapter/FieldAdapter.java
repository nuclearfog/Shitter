package org.nuclearfog.twidda.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.model.lists.Fields;
import org.nuclearfog.twidda.ui.adapter.holder.FieldHolder;

/**
 * @author nuclearfog
 */
public class FieldAdapter extends Adapter<FieldHolder> {

	private Fields fields = new Fields();

	public FieldAdapter() {

	}


	@NonNull
	@Override
	public FieldHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new FieldHolder(parent);
	}


	@Override
	public void onBindViewHolder(@NonNull FieldHolder holder, int position) {
		holder.setContent(fields.get(position));
	}


	@Override
	public int getItemCount() {
		return fields.size();
	}


	public void replaceItems(Fields fields) {
		this.fields.clear();
		this.fields.addAll(fields);
		notifyDataSetChanged();
	}


	public Fields getItems() {
		return new Fields(fields);
	}
}