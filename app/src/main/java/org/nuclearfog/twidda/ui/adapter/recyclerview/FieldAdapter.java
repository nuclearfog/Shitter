package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.backend.utils.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.model.lists.Fields;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.FieldHolder;

import java.util.Collections;

/**
 * RecyclerView adapter used to show a list of {@link org.nuclearfog.twidda.model.Field}
 *
 * @author nuclearfog
 */
public class FieldAdapter extends Adapter<FieldHolder> implements OnTagClickListener {

	private OnLinkClickListener listener;

	private Fields items = new Fields();

	/**
	 *
	 */
	public FieldAdapter(OnLinkClickListener listener) {
		this.listener = listener;
	}


	@NonNull
	@Override
	public FieldHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new FieldHolder(parent, this);
	}


	@Override
	public void onBindViewHolder(@NonNull FieldHolder holder, int position) {
		holder.setContent(items.get(position));
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public void onTagClick(String tag) {
	}


	@Override
	public void onLinkClick(String link) {
		listener.onLinkClick(link);
	}

	/**
	 * replace all existing items with new ones
	 *
	 * @param newFields items to insert
	 */
	public void setItems(Fields newFields) {
		items.clear();
		items.addAll(newFields);
		Collections.sort(items);
		notifyDataSetChanged();
	}

	/**
	 * clear all items
	 */
	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}

	/**
	 * @return true if adapter doesn't contain any items
	 */
	public boolean isEmpty() {
		return items.isEmpty();
	}

	/**
	 * get all items
	 *
	 * @return Field list
	 */
	public Fields getItems() {
		return new Fields(items);
	}

	/**
	 * Click listener for url
	 */
	public interface OnLinkClickListener {

		/**
		 * called on url click
		 *
		 * @param url url string
		 */
		void onLinkClick(String url);
	}
}