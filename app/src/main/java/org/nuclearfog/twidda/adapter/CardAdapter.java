package org.nuclearfog.twidda.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.adapter.holder.CardHolder;
import org.nuclearfog.twidda.adapter.holder.CardHolder.OnItemClickListener;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Card;

import java.util.Arrays;

/**
 * Recyclerview Adapter used for link preview "Cards"
 *
 * @author nuclearfog
 */
public class CardAdapter extends RecyclerView.Adapter<CardHolder> implements OnItemClickListener {

	private GlobalSettings settings;
	private Picasso picasso;
	private OnCardClickListener listener;

	private Card[] cards;


	public CardAdapter(Context context, OnCardClickListener listener) {
		settings = GlobalSettings.getInstance(context);
		picasso = PicassoBuilder.get(context);
		this.listener = listener;
	}


	@NonNull
	@Override
	public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		CardHolder holder = new CardHolder(parent, settings, picasso);
		holder.setClickListener(this);
		return holder;
	}


	@Override
	public void onBindViewHolder(@NonNull CardHolder holder, int position) {
		holder.setContent(cards[position]);
	}


	@Override
	public int getItemCount() {
		return cards.length;
	}


	@Override
	public void onItemClick(int pos, int type) {
		if (type == OnItemClickListener.TYPE_LINK) {
			listener.onCardClick(cards[pos], OnCardClickListener.TYPE_LINK);
		} else if (type == OnItemClickListener.TYPE_IMAGE) {
			listener.onCardClick(cards[pos], OnCardClickListener.TYPE_IMAGE);
		}
	}

	/**
	 * replace all items
	 * @param newCards new items to insert
	 */
	public void replaceAll(Card[] newCards) {
		cards = Arrays.copyOf(newCards, newCards.length);
		notifyDataSetChanged();
	}

	/**
	 * item click listener
	 */
	public interface OnCardClickListener {

		int TYPE_LINK = 1;

		int TYPE_IMAGE = 2;

		/**
		 * called on item click
		 *
		 * @param card card item
		 * @param type type of click {@link #TYPE_LINK,#TYPE_IMAGE}
		 */
		void onCardClick(Card card, int type);
	}
}