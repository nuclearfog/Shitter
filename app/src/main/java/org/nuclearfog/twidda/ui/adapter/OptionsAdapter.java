package org.nuclearfog.twidda.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.ui.adapter.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.holder.Optionholder;

import java.util.Set;
import java.util.TreeSet;

/**
 * RecyclerView adapter for poll options
 *
 * @author nuclearfog
 */
public class OptionsAdapter extends RecyclerView.Adapter<Optionholder> implements OnHolderClickListener {

	private Poll.Option[] options = {};
	private Set<Integer> selection;

	private int totalVotes = 1;
	private int limitVotes = 1;

	private GlobalSettings settings;

	/**
	 *
	 */
	public OptionsAdapter(GlobalSettings settings) {
		this.settings = settings;
		selection = new TreeSet<>();
	}


	@NonNull
	@Override
	public Optionholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new Optionholder(parent, settings, this);
	}


	@Override
	public void onBindViewHolder(@NonNull Optionholder holder, int position) {
		holder.setContent(options[position], selection.contains(position), totalVotes);
	}


	@Override
	public int getItemCount() {
		return options.length;
	}


	@Override
	public void onItemClick(int pos, int type, int... extras) {
		if (type == OnHolderClickListener.POLL_OPTION) {
			if (selection.contains(pos)) {
				selection.remove(pos);
				notifyItemChanged(pos);
			} else if (selection.size() < limitVotes) {
				selection.add(pos);
				notifyItemChanged(pos);
			}
		}
	}


	@Override
	public boolean onPlaceholderClick(int position) {
		return false;
	}

	/**
	 * set poll information and options
	 *
	 * @param poll poll information
	 */
	public void addAll(Poll poll) {
		options = poll.getOptions();
		for (int i = 0; i < options.length; i++) {
			Poll.Option option = options[i];
			if (option.selected()) {
				selection.add(i);
			}
		}
		totalVotes = poll.voteCount();
		limitVotes = poll.getLimit();
		notifyDataSetChanged();
	}

	/**
	 * @return a set of selection position
	 */
	public int[] getSelection() {
		int pos = 0;
		int[] result = new int[selection.size()];
		for (Integer index : selection)
			result[pos++] = index;
		return result;
	}
}