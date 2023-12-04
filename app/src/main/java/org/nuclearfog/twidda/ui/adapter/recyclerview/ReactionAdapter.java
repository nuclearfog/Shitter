package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Reaction;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.ReactionHolder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * RecyclerView adapter used by {@link org.nuclearfog.twidda.ui.adapter.recyclerview.holder.AnnouncementHolder}
 *
 * @author nuclearfog
 */
public class ReactionAdapter extends Adapter<ReactionHolder> implements OnHolderClickListener {

	private OnReactionSelected listener;
	private List<Reaction> items = new LinkedList<>();
	private Emoji[] emojis = {};

	/**
	 *
	 */
	public ReactionAdapter(OnReactionSelected listener) {
		this.listener = listener;
	}


	@NonNull
	@Override
	public ReactionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ReactionHolder(parent, this);
	}


	@Override
	public void onBindViewHolder(@NonNull ReactionHolder holder, int position) {
		holder.setContent(items.get(position), emojis);
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		if (type == ANNOUNCEMENT_REACTION) {
			listener.onReactionClick(position);
		}
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		return false;
	}

	/**
	 * add adapter items
	 */
	public void setItems(Reaction[] reactions, Emoji[] emojis) {
		items.clear();
		Arrays.sort(reactions);
		items.addAll(Arrays.asList(reactions));
		this.emojis = Arrays.copyOf(emojis, emojis.length);
		notifyDataSetChanged();
	}

	/**
	 * callback for reaction select
	 */
	public interface OnReactionSelected {

		/**
		 * called when a reaction item was clicked
		 *
		 * @param index position of the reaction item
		 */
		void onReactionClick(int index);
	}
}