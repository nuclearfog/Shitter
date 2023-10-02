package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.model.lists.Fields;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.FieldHolder;

/**
 * RecyclerView adapter used to show a list of {@link org.nuclearfog.twidda.model.User.Field}
 *
 * @author nuclearfog
 */
public class FieldAdapter extends Adapter<FieldHolder> implements OnTagClickListener {

	private OnLinkClickListener listener;

	private Fields fields = new Fields();

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
		holder.setContent(fields.get(position));
	}


	@Override
	public int getItemCount() {
		return fields.size();
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
	 * @param fields items to insert
	 */
	public void replaceItems(Fields fields) {
		this.fields.clear();
		this.fields.addAll(fields);
		notifyDataSetChanged();
	}

	/**
	 * clear all items
	 */
	public void clear() {
		fields.clear();
		notifyDataSetChanged();
	}

	/**
	 * get all items
	 *
	 * @return Field list
	 */
	public Fields getItems() {
		return new Fields(fields);
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