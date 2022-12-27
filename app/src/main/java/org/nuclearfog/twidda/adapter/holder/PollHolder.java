package org.nuclearfog.twidda.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.OptionsAdapter;
import org.nuclearfog.twidda.adapter.OptionsAdapter.OnOptionClickListener;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Poll;

import java.text.NumberFormat;


public class PollHolder extends ViewHolder implements OnOptionClickListener {

	private static final NumberFormat NUM_FORMAT = NumberFormat.getIntegerInstance();

	private TextView votesCount;

	private OptionsAdapter adapter;

	@Nullable
	private OnPollOptionClickListener listener;


	public PollHolder(ViewGroup parent, GlobalSettings settings) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_poll, parent, false));

		CardView cardBackground = (CardView) itemView;
		RecyclerView optionsList = itemView.findViewById(R.id.item_poll_options_list);
		votesCount = itemView.findViewById(R.id.item_poll_votes_count);

		cardBackground.setCardBackgroundColor(settings.getCardColor());
		votesCount.setTextColor(settings.getFontColor());
		itemView.getLayoutParams().width = parent.getMeasuredHeight() * 2;

		adapter = new OptionsAdapter(settings, this);
		optionsList.setAdapter(adapter);
		optionsList.setLayoutManager(new LinearLayoutManager(parent.getContext(), LinearLayoutManager.VERTICAL, false));

	}


	@Override
	public void onOptionClick(int index) {
		int pos = getLayoutPosition();
		if (pos != RecyclerView.NO_POSITION && listener != null) {
			listener.onPollOptionClick(pos, index);
		}
	}


	public void setContent(Poll poll) {
		votesCount.setText(R.string.poll_total_votes);
		votesCount.append(NUM_FORMAT.format(poll.voteCount()));
		adapter.addAll(poll);
	}


	public void setOnPollOptionClickListener(OnPollOptionClickListener listener) {
		this.listener = listener;
	}


	public interface OnPollOptionClickListener {

		void onPollOptionClick(int position, int selection);
	}
}