package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.UserList;

/**
 * view holder class for an user list item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.adapter.recyclerview.UserlistAdapter
 */
public class UserlistHolder extends ViewHolder implements OnClickListener {

	private OnHolderClickListener listener;
	private TextView title;

	/**
	 *
	 */
	public UserlistHolder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false));
		GlobalSettings settings = GlobalSettings.get(parent.getContext());
		this.listener = listener;

		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_list_container);
		title = itemView.findViewById(R.id.item_list_title);

		AppStyles.setTheme(container, Color.TRANSPARENT);
		background.setCardBackgroundColor(settings.getCardColor());
		itemView.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != RecyclerView.NO_POSITION) {
			if (v == itemView) {
				listener.onItemClick(position, OnHolderClickListener.LIST_CLICK);
			}
		}
	}

	/**
	 * set view content
	 */
	public void setContent(UserList userlist) {
		title.setText(userlist.getTitle());
	}
}