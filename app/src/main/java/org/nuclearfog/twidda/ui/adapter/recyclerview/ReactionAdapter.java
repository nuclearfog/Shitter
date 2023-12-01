package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.model.Reaction;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.ReactionHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author nuclearfog
 */
public class ReactionAdapter extends Adapter<ReactionHolder> implements OnHolderClickListener {

	private OnReactionSelected listener;
	private List<Reaction> items = new LinkedList<>();

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
		holder.setContent(items.get(position));
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
	public void setItems(Reaction[] reactions) {
		items.clear();
		Arrays.sort(reactions);
		items.addAll(Arrays.asList(reactions));
		notifyDataSetChanged();
	}

	/**
	 *
	 */
	public interface OnReactionSelected {

		void onReactionClick(int index);
	}
}