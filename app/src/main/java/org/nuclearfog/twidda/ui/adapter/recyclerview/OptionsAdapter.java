package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.PollOption;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.Optionholder;

import java.util.Set;
import java.util.TreeSet;

/**
 * RecyclerView adapter for poll options
 *
 * @author nuclearfog
 */
public class OptionsAdapter extends Adapter<Optionholder> implements OnHolderClickListener {

	private int totalVotes, limitVotes;

	private PollOption[] options = {};
	private Emoji[] emojis = {};
	private Set<Integer> selection = new TreeSet<>();


	@NonNull
	@Override
	public Optionholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new Optionholder(parent, this);
	}


	@Override
	public void onBindViewHolder(@NonNull Optionholder holder, int position) {
		holder.setContent(options[position], emojis, selection.contains(position), totalVotes);
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
	public boolean onPlaceholderClick(int index) {
		return false;
	}

	/**
	 * set poll information and options
	 *
	 * @param poll poll information
	 */
	public void addItems(Poll poll) {
		options = poll.getOptions();
		emojis = poll.getEmojis();
		for (int i = 0; i < options.length; i++) {
			PollOption option = options[i];
			if (option.isSelected()) {
				selection.add(i);
			}
		}
		if (poll.voted() || poll.closed()) {
			limitVotes = 0;
		} else if (poll.multipleChoiceEnabled()) {
			limitVotes = poll.getOptions().length;
		} else {
			limitVotes = 1;
		}
		totalVotes = poll.voteCount();
		notifyDataSetChanged();
	}

	/**
	 * @return an array of selection indexes
	 */
	public int[] getItemSelection() {
		int pos = 0;
		int[] result = new int[selection.size()];
		for (Integer index : selection)
			result[pos++] = index;
		return result;
	}
}