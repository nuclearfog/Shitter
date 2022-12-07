package org.nuclearfog.twidda.adapter.holder;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * item holder for a user login item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.AccountAdapter
 */
public class AccountHolder extends ViewHolder implements OnClickListener {

	private ImageView profile;
	private ImageButton remove;
	private TextView username, screenname, date;

	private GlobalSettings settings;
	private Picasso picasso;

	private OnHolderClickListener listener;

	/**
	 *
	 */
	public AccountHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso) {
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
		this.settings = settings;
		this.picasso = picasso;

		itemView.setOnClickListener(this);
		remove.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != NO_POSITION && listener != null) {
			if (v == itemView) {
				listener.onItemClick(position, OnHolderClickListener.ACCOUNT_SELECT);
			} else if (v == remove) {
				listener.onItemClick(position, OnHolderClickListener.ACCOUNT_REMOVE);
			}
		}
	}

	/**
	 * set view content
	 *
	 * @param account content
	 */
	public void setContent(Account account) {
		date.setText(StringTools.formatCreationTime(itemView.getResources(), account.getLoginDate()));
		User user = account.getUser();
		if (user != null) {
			// set profile information
			username.setText(user.getUsername());
			screenname.setText(user.getScreenname());
			// set profile image
			String profileImageUrl = user.getProfileImageThumbnailUrl();
			if (settings.imagesEnabled() && !profileImageUrl.isEmpty()) {
				Transformation roundCorner = new RoundedCornersTransformation(2, 0);
				picasso.load(profileImageUrl).transform(roundCorner).error(R.drawable.no_image).into(profile);
			} else {
				profile.setImageResource(0);
			}
		} else {
			profile.setImageResource(0);
			username.setText(R.string.account_user_unnamed);
			screenname.setText(R.string.account_user_id_prefix);
			screenname.append(Long.toString(account.getId()));
		}
	}

	/**
	 * set item click listener
	 */
	public void setOnAccountClickListener(OnHolderClickListener listener) {
		this.listener = listener;
	}
}