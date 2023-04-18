package org.nuclearfog.twidda.ui.adapter.holder;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.ui.adapter.OptionsAdapter;

/**
 * View holder for status polls
 *
 * @author nuclearfog
 */
public class PollHolder extends ViewHolder implements OnClickListener {

	private TextView votesCount, expiration;
	private Button voteButton;

	private OptionsAdapter adapter;
	private OnHolderClickListener listener;

	/**
	 *
	 */
	public PollHolder(ViewGroup parent, GlobalSettings settings, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_poll, parent, false));
		CardView cardBackground = (CardView) itemView;
		RecyclerView optionsList = itemView.findViewById(R.id.item_poll_options_list);
		voteButton = itemView.findViewById(R.id.item_poll_vote_button);
		votesCount = itemView.findViewById(R.id.item_poll_votes_count);
		expiration = itemView.findViewById(R.id.item_poll_expiration);
		adapter = new OptionsAdapter(settings);
		this.listener = listener;

		cardBackground.setCardBackgroundColor(settings.getCardColor());
		votesCount.setTextColor(settings.getTextColor());
		votesCount.setTypeface(settings.getTypeFace());
		expiration.setTextColor(settings.getTextColor());
		expiration.setTypeface(settings.getTypeFace());
		itemView.getLayoutParams().width = Resources.getSystem().getDisplayMetrics().widthPixels * 3 / 4;

		optionsList.setAdapter(adapter);
		optionsList.setItemAnimator(null); // disable animation
		optionsList.setLayoutManager(new LinearLayoutManager(parent.getContext(), LinearLayoutManager.VERTICAL, false));
		voteButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.item_poll_vote_button) {
			int pos = getLayoutPosition();
			if (pos != RecyclerView.NO_POSITION) {
				int[] selection = adapter.getItemSelection();
				if (selection.length > 0) {
					listener.onItemClick(pos, OnHolderClickListener.POLL_VOTE, selection);
				}
			}
		}
	}

	/**
	 * set view content
	 *
	 * @param poll poll information
	 */
	public void setContent(Poll poll) {
		if (poll.closed()) {
			expiration.setText(R.string.poll_finished);
			voteButton.setVisibility(View.GONE);
		} else {
			expiration.setText(StringUtils.formatExpirationTime(expiration.getResources(), poll.getEndTime()));
			if (poll.voted()) {
				voteButton.setVisibility(View.GONE);
			} else if (poll.getLimit() > 0) {
				voteButton.setVisibility(View.VISIBLE);
			}
		}
		votesCount.setText(votesCount.getResources().getQuantityString(R.plurals.poll_votes, poll.voteCount(), poll.voteCount()));
		adapter.addItems(poll);
	}
}