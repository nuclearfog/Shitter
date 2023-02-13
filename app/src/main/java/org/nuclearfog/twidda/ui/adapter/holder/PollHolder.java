package org.nuclearfog.twidda.ui.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.ui.adapter.OptionsAdapter;
import org.nuclearfog.twidda.ui.adapter.OptionsAdapter.OnOptionClickListener;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Poll;

/**
 * View holder for status polls
 *
 * @author nuclearfog
 */
public class PollHolder extends ViewHolder implements OnOptionClickListener {

	private TextView votesCount;

	private OptionsAdapter adapter;
	private OnHolderClickListener listener;

	/**
	 *
	 */
	public PollHolder(ViewGroup parent, GlobalSettings settings, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_poll, parent, false));
		CardView cardBackground = (CardView) itemView;
		RecyclerView optionsList = itemView.findViewById(R.id.item_poll_options_list);
		votesCount = itemView.findViewById(R.id.item_poll_votes_count);
		adapter = new OptionsAdapter(settings, this);
		this.listener = listener;

		cardBackground.setCardBackgroundColor(settings.getCardColor());
		votesCount.setTextColor(settings.getFontColor());
		votesCount.setTypeface(settings.getTypeFace());
		itemView.getLayoutParams().width = parent.getMeasuredHeight() * 2; // 2:1 ratio

		optionsList.setAdapter(adapter);
		optionsList.setLayoutManager(new LinearLayoutManager(parent.getContext(), LinearLayoutManager.VERTICAL, false));

	}


	@Override
	public void onOptionClick(int index) {
		int pos = getLayoutPosition();
		if (pos != RecyclerView.NO_POSITION) {
			listener.onItemClick(pos, OnHolderClickListener.POLL_ITEM, index);
		}
	}

	/**
	 * set view content
	 *
	 * @param poll poll information
	 */
	public void setContent(Poll poll) {
		if (poll.closed()) {
			votesCount.setText(R.string.poll_finished);
		} else {
			votesCount.setText(R.string.poll_total_votes);
		}
		votesCount.append(StringTools.NUMBER_FORMAT.format(poll.voteCount()));
		adapter.addAll(poll);
	}
}