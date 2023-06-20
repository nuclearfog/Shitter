package org.nuclearfog.twidda.ui.adapter.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * domain holder class for {@link org.nuclearfog.twidda.ui.adapter.DomainAdapter}
 *
 * @author nuclearfog
 */
public class DomainHolder extends ViewHolder implements OnClickListener {

	private TextView domain_name;

	private OnHolderClickListener listener;

	/**
	 *
	 */
	public DomainHolder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_domain, parent, false));
		this.listener = listener;
		CardView card = (CardView) itemView;
		View deleteButton = itemView.findViewById(R.id.item_domain_delete);
		domain_name = itemView.findViewById(R.id.item_domain_name);

		GlobalSettings settings = GlobalSettings.get(parent.getContext());
		domain_name.setTextColor(settings.getTextColor());
		domain_name.setTypeface(settings.getTypeFace());
		card.setCardBackgroundColor(settings.getCardColor());

		deleteButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.item_domain_delete) {
			int position = getLayoutPosition();
			if (position != RecyclerView.NO_POSITION) {
				listener.onItemClick(position, OnHolderClickListener.DOMAIN_REMOVE);
			}
		}
	}

	/**
	 * set domain information
	 *
	 * @param name domain information
	 */
	public void setContent(String name) {
		domain_name.setText(name);
	}
}