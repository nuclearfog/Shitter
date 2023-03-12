package org.nuclearfog.twidda.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.ui.adapter.holder.EditOptionsHolder;
import org.nuclearfog.twidda.ui.adapter.holder.EditOptionsHolder.OnOptionChangedListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * RecyclerView adapter used to show option items
 *
 * @author nuclearfog
 */
public class EditOptionsAdapter extends Adapter<EditOptionsHolder> implements OnOptionChangedListener {

	/**
	 * minimum option count
	 */
	private static final int MIN_OPTIONS = 2;

	/**
	 * maximum option count
	 */
	private static final int MAX_OPTIONS = 4;

	private LinkedList<String> options;

	/**
	 *
	 */
	public EditOptionsAdapter() {
		options = new LinkedList<>();
		for (int i = 0 ; i < MIN_OPTIONS ; i++)
			options.add("");
		options.add(null);
	}


	@NonNull
	@Override
	public EditOptionsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new EditOptionsHolder(parent, this);
	}


	@Override
	public void onBindViewHolder(@NonNull EditOptionsHolder holder, int position) {
		if (position < MIN_OPTIONS) {
			holder.setState(position, EditOptionsHolder.STATE_LOCKED);
		} else if (options.get(position) != null) {
			holder.setState(position, EditOptionsHolder.STATE_ACTIVE);
		} else {
			holder.setState(position, EditOptionsHolder.STATE_DISABLED);
		}
	}


	@Override
	public int getItemCount() {
		return options.size();
	}


	@Override
	public void onOptionAdd(int position) {
		if (options.size() < MAX_OPTIONS) {
			// add empty item
			options.add(position, "");
			notifyItemInserted(position);
			// update upper items
			notifyItemRangeChanged(position + 1, MAX_OPTIONS - position - 1);
		} else {
			options.set(position, "");
			notifyItemChanged(position);
		}
	}


	@Override
	public void onOptionRemove(int position) {
		if (position < MAX_OPTIONS - 1) {
			// remove item
			options.remove(position);
			notifyItemRemoved(position);
			// update upper items
			notifyItemRangeChanged(position + 1, MAX_OPTIONS - position - 1);
			// add placeholder item
			if (options.peekLast() != null) {
				options.add(null);
				notifyItemInserted(options.size());
			}
		} else {
			options.set(position, null);
			notifyItemChanged(position);
		}
	}


	@Override
	public void OnOptionChange(int position, String name) {
		options.set(position, name);
	}

	/**
	 * set option names
	 *
	 * @param newOptions list of option name strings
	 */
	public void setOptions(List<String> newOptions) {
		options.clear();
		options.addAll(newOptions);
		for (int i = options.size() ; i < MIN_OPTIONS; i++) {
			options.add("");
		}
		options.add(null);
		notifyDataSetChanged();
	}

	/**
	 * get option names
	 *
	 * @return list of option name strings
	 */
	public List<String> getOptions() {
		List<String> result = new ArrayList<>();
		for (String option : options) {
			if (option != null && !option.trim().isEmpty()) {
				result.add(option);
			}
		}
		return result;
	}
}