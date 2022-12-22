package org.nuclearfog.twidda.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.adapter.holder.CardHolder;
import org.nuclearfog.twidda.adapter.holder.CardHolder.OnItemClickListener;
import org.nuclearfog.twidda.adapter.holder.PreviewHolder;
import org.nuclearfog.twidda.adapter.holder.PreviewHolder.OnPreviewClickListener;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Card;
import org.nuclearfog.twidda.model.Media;

import java.util.Arrays;

/**
 * Recyclerview Adapter used for link preview "Cards" or media previews
 *
 * @author nuclearfog
 */
public class PreviewAdapter extends RecyclerView.Adapter<ViewHolder> implements OnItemClickListener, OnPreviewClickListener {

	/**
	 * ID used for media preview
	 */
	private static final int ITEM_PREVIEW = 0;

	/**
	 * ID used for card preview
	 */
	private static final int ITEM_CARD = 1;

	private GlobalSettings settings;
	private Picasso picasso;
	private OnCardClickListener listener;

	private Card[] cards = {};
	private Media[] medias = {};

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
			PreviewHolder holder = new PreviewHolder(parent, picasso);
			holder.setOnPreviewClickListener(this);
			return holder;
		} else {
			CardHolder holder = new CardHolder(parent, settings, picasso);
			holder.setClickListener(this);
			return holder;
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		if (holder instanceof PreviewHolder) {
			((PreviewHolder) holder).setContent(medias[position]);
		} else {
			((CardHolder) holder).setContent(cards[position - medias.length]);
		}
	}


	@Override
	public int getItemCount() {
		return cards.length + medias.length;
	}


	@Override
	public int getItemViewType(int position) {
		if (position < medias.length)
			return ITEM_PREVIEW;
		return ITEM_CARD;
	}


	@Override
	public void onItemClick(int pos, int type) {
		if (type == OnItemClickListener.TYPE_LINK) {
			listener.onCardClick(cards[pos - medias.length], OnCardClickListener.TYPE_LINK);
		} else if (type == OnItemClickListener.TYPE_IMAGE) {
			listener.onCardClick(cards[pos - medias.length], OnCardClickListener.TYPE_IMAGE);
		}
	}


	@Override
	public void onPreviewClick(int pos) {
		listener.onMediaClick(medias[pos]);
	}

	/**
	 * replace all items
	 *
	 * @param medias new media items to insert
	 * @param cards  new cards to insert
	 */
	public void replaceAll(@NonNull Card[] cards, @NonNull Media[] medias) {
		this.cards = Arrays.copyOf(cards, cards.length);
		this.medias = Arrays.copyOf(medias, medias.length);
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
	}
}