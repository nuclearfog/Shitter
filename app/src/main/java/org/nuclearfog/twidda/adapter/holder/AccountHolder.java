package org.nuclearfog.twidda.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * item holder for a user login item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.AccountAdapter
 */
public class AccountHolder extends ViewHolder {

	public final ImageView profile;
	public final ImageButton remove;
	public final TextView username, screenname, date;

	/**
	 *
	 */
	public AccountHolder(ViewGroup parent, GlobalSettings settings) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false));
		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_account_container);
		username = itemView.findViewById(R.id.item_account_username);
		screenname = itemView.findViewById(R.id.item_account_screenname);
		date = itemView.findViewById(R.id.item_account_date);
		remove = itemView.findViewById(R.id.item_account_remove);
		profile = itemView.findViewById(R.id.item_account_profile);

		AppStyles.setTheme(container, 0);
		background.setCardBackgroundColor(settings.getCardColor());
	}
}