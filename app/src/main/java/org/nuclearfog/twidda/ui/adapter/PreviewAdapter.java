package org.nuclearfog.twidda.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Card;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.adapter.holder.CardHolder;
import org.nuclearfog.twidda.ui.adapter.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.holder.PollHolder;
import org.nuclearfog.twidda.ui.adapter.holder.PreviewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Recyclerview Adapter used for link preview "Cards" or media previews
 *
 * @author nuclearfog
 */
public class PreviewAdapter extends RecyclerView.Adapter<ViewHolder> implements OnHolderClickListener {

	private static final int INVALID_ID = -1;

	/**
	 * ID used for {@link PreviewHolder}
	 */
	private static final int ITEM_PREVIEW = 0;

	/**
	 * ID used for {@link CardHolder}
	 */
	private static final int ITEM_CARD = 1;

	/**
	 * ID used for {@link PollHolder}
	 */
	private static final int ITEM_POLL = 2;

	private GlobalSettings settings;
	private Picasso picasso;
	private OnCardClickListener listener;

	private List<Object> items = new ArrayList<>();

	/**
	 *
	 */
	public PreviewAdapter(GlobalSettings settings, Picasso picasso, OnCardClickListener listener) {
		this.settings = settings;
		this.picasso = picasso;
		this.listener = listener;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		switch (viewType) {
			default:
			case ITEM_PREVIEW:
				return new PreviewHolder(parent, settings, picasso, this);

			case ITEM_CARD:
				return new CardHolder(parent, settings, picasso, this);

			case ITEM_POLL:
				return new PollHolder(parent, settings, this);
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Object item = items.get(position);
		if (holder instanceof PreviewHolder && item instanceof Media) {
			PreviewHolder previewHolder = ((PreviewHolder) holder);
			Media media = (Media) item;
			previewHolder.setContent(media);
		} else if (holder instanceof CardHolder && item instanceof Card) {
			CardHolder cardHolder = (CardHolder) holder;
			Card card = (Card) item;
			cardHolder.setContent(card);
		} else if (holder instanceof PollHolder && item instanceof Poll) {
			PollHolder pollHolder = (PollHolder) holder;
			Poll poll = (Poll) item;
			pollHolder.setContent(poll);
		}
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public int getItemViewType(int position) {
		Object item = items.get(position);
		if (item instanceof Poll)
			return ITEM_POLL;
		if (item instanceof Media)
			return ITEM_PREVIEW;
		if (item instanceof Card)
			return ITEM_CARD;
		return INVALID_ID;
	}


	@Override
	public void onItemClick(int pos, int type, int... extras) {
		Object item = items.get(pos);
		switch (type) {
			case OnHolderClickListener.PREVIEW_CLICK:
				if (item instanceof Media) {
					Media media = (Media) item;
					listener.onMediaClick(media);
				}
				break;

			case OnHolderClickListener.CARD_LINK:
				if (item instanceof Card) {
					Card card = (Card) item;
					listener.onCardClick(card, OnCardClickListener.TYPE_LINK);
				}
				break;

			case OnHolderClickListener.CARD_IMAGE:
				if (item instanceof Card) {
					Card card = (Card) item;
					listener.onCardClick(card, OnCardClickListener.TYPE_IMAGE);
				}
				break;

			case OnHolderClickListener.POLL_VOTE:
				if (item instanceof Poll && extras.length == 1) {
					Poll poll = (Poll) item;
					listener.onVoteClick(poll, extras);
				}
				break;

		}
	}


	@Override
	public boolean onPlaceholderClick(int position) {
		return false;
	}

	/**
	 * replace all items
	 *
	 * @param status status information with attachments
	 */
	public void replaceAll(Status status) {
		items.clear();
		if (status.getPoll() != null)
			items.add(status.getPoll());
		if (status.getMedia().length > 0)
			items.addAll(Arrays.asList(status.getMedia()));
		if (status.getCards().length > 0)
			items.addAll(Arrays.asList(status.getCards()));
		notifyDataSetChanged();
	}

	/**
	 * update existing poll item
	 *
	 * @param poll updated poll item
	 */
	public void updatePoll(Poll poll) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i) instanceof Poll && ((Poll) items.get(i)).getId() == poll.getId()) {
				items.set(i, poll);
				notifyItemChanged(i);
				break;
			}
		}
	}

	/**
	 * item click listener
	 */
	public interface OnCardClickListener {

		/**
		 * indicates a link click
		 */
		int TYPE_LINK = 1;

		/**
		 * indicates an image thumbnail click
		 */
		int TYPE_IMAGE = 2;

		/**
		 * called on item click
		 *
		 * @param card card item
		 * @param type type of click {@link #TYPE_LINK,#TYPE_IMAGE}
		 */
		void onCardClick(Card card, int type);

		/**
		 * called on media item click
		 *
		 * @param media media item
		 */
		void onMediaClick(Media media);

		/**
		 * called on poll option click
		 *
		 * @param poll      poll containing the clicked option
		 * @param selection selected poll options
		 */
		void onVoteClick(Poll poll, int[] selection);
	}
}