package org.nuclearfog.twidda.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.ui.adapter.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.holder.Optionholder;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Poll;

/**
 * RecyclerView adapter for poll options
 *
 * @author nuclearfog
 */
public class OptionsAdapter extends RecyclerView.Adapter<Optionholder> implements OnHolderClickListener {

	private Poll.Option[] options = {};
	private int totalVotes = 1;

	private GlobalSettings settings;
	private OnOptionClickListener listener;
	private boolean enableVote;

	/**
	 *
	 */
	public OptionsAdapter(GlobalSettings settings, OnOptionClickListener listener) {
		// currently Twitter doesn't support vote over API
		enableVote = settings.getLogin().getConfiguration().voteEnabled();
		this.settings = settings;
		this.listener = listener;
	}


	@NonNull
	@Override
	public Optionholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new Optionholder(parent, settings, this);
	}


	@Override
	public void onBindViewHolder(@NonNull Optionholder holder, int position) {
		holder.setContent(options[position], totalVotes);
	}


	@Override
	public int getItemCount() {
		return options.length;
	}


	@Override
	public void onItemClick(int pos, int type, int... extras) {
		if (enableVote) {
			if (type == OnHolderClickListener.POLL_OPTION) {
				listener.onOptionClick(pos);
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
		this.options = poll.getOptions();
		totalVotes = poll.voteCount();
		notifyDataSetChanged();
	}

	/**
	 * Listener for poll options
	 */
	public interface OnOptionClickListener {

		/**
		 * called on poll option select
		 *
		 * @param index index of the poll option
		 */
		void onOptionClick(int index);
	}
}