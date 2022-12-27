package org.nuclearfog.twidda.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.adapter.holder.CardHolder;
import org.nuclearfog.twidda.adapter.holder.CardHolder.OnItemClickListener;
import org.nuclearfog.twidda.adapter.holder.PollHolder;
import org.nuclearfog.twidda.adapter.holder.PollHolder.OnPollOptionClickListener;
import org.nuclearfog.twidda.adapter.holder.PreviewHolder;
import org.nuclearfog.twidda.adapter.holder.PreviewHolder.OnPreviewClickListener;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Card;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Poll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Recyclerview Adapter used for link preview "Cards" or media previews
 *
 * @author nuclearfog
 */
public class PreviewAdapter extends RecyclerView.Adapter<ViewHolder> implements OnItemClickListener, OnPreviewClickListener, OnPollOptionClickListener {

	/**
	 * ID used for media preview
	 */
	private static final int ITEM_PREVIEW = 0;

	/**
	 * ID used for card preview
	 */
	private static final int ITEM_CARD = 1;

	private static final int INVALID_ID = -1;

	/**
	 * ID used for {@link org.nuclearfog.twidda.adapter.holder.PollHolder}
	 */
	private static final int ITEM_POLL = 2;

	private GlobalSettings settings;
	private Picasso picasso;
	private OnCardClickListener listener;

	private List<Object> items = new ArrayList<>();

	/**
	 *
	 */
	public PreviewAdapter(Context context, OnCardClickListener listener) {
		settings = GlobalSettings.getInstance(context);
		picasso = PicassoBuilder.get(context);
		this.listener = listener;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == ITEM_PREVIEW) {
			PreviewHolder holder = new PreviewHolder(parent, settings, picasso);
			holder.setOnPreviewClickListener(this);
			return holder;
		} else if (viewType == ITEM_CARD) {
			CardHolder holder = new CardHolder(parent, settings, picasso);
			holder.setOnCardClickListener(this);
			return holder;
		} else {
			PollHolder holder = new PollHolder(parent, settings);
			holder.setOnPollOptionClickListener(this);
			return holder;
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
	public void onCardItemClick(int pos, int type) {
		Object item = items.get(pos);
		if (item instanceof Card) {
			Card card = (Card) item;
			if (type == OnItemClickListener.TYPE_LINK) {
				listener.onCardClick(card, OnCardClickListener.TYPE_LINK);
			} else if (type == OnItemClickListener.TYPE_IMAGE) {
				listener.onCardClick(card, OnCardClickListener.TYPE_IMAGE);
			}
		}
	}


	@Override
	public void onPreviewClick(int pos) {
		Object item = items.get(pos);
		if (item instanceof Media) {
			Media media = (Media) item;
			listener.onMediaClick(media);
		}
	}


	@Override
	public void onPollOptionClick(int position, int selection) {
		Object item = items.get(position);
		if (item instanceof Poll) {
			Poll poll = (Poll) item;
			listener.onPollOptionClick(poll, selection);
		}
	}

	/**
	 * replace all items
	 *
	 * @param medias new media items to insert
	 * @param cards  new cards to insert
	 */
	public void replaceAll(@NonNull Card[] cards, @NonNull Media[] medias, @Nullable Poll poll) {
		items.clear();
		if (poll != null)
			items.add(poll);
		items.addAll(Arrays.asList(medias));
		items.addAll(Arrays.asList(cards));
		notifyDataSetChanged();
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


		void onPollOptionClick(Poll poll, int selection);
	}
}