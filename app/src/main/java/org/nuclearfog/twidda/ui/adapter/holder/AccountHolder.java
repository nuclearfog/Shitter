package org.nuclearfog.twidda.ui.adapter.holder;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * item holder for a user login item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.adapter.AccountAdapter
 */
public class AccountHolder extends ViewHolder implements OnClickListener {

	private static final int IMG_SIZE = 150;

	private static final int EMPTY_COLOR = 0x2F000000;

	private ImageView profile;
	private ImageButton remove;
	private TextView username, screenname, date;
	private Drawable placeholder;

	private OnHolderClickListener listener;
	private GlobalSettings settings;
	private Picasso picasso;


	public AccountHolder(ViewGroup parent, GlobalSettings settings, Picasso picasso, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false));
		this.settings = settings;
		this.picasso = picasso;
		this.listener = listener;

		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_account_container);
		username = itemView.findViewById(R.id.item_account_username);
		screenname = itemView.findViewById(R.id.item_account_screenname);
		date = itemView.findViewById(R.id.item_account_date);
		remove = itemView.findViewById(R.id.item_account_remove);
		profile = itemView.findViewById(R.id.item_account_profile);
		placeholder = new ColorDrawable(EMPTY_COLOR);

		AppStyles.setTheme(container, Color.TRANSPARENT);
		background.setCardBackgroundColor(settings.getCardColor());

		itemView.setOnClickListener(this);
		remove.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != NO_POSITION) {
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
		date.setText(StringUtils.formatCreationTime(itemView.getResources(), account.getTimestamp()));
		User user = account.getUser();
		if (user != null) {
			// set profile information
			username.setText(user.getUsername());
			screenname.setText(user.getScreenname());
			// set profile image
			String profileImageUrl = user.getProfileImageThumbnailUrl();
			if (settings.imagesEnabled() && !profileImageUrl.isEmpty()) {
				Transformation roundCorner = new RoundedCornersTransformation(2, 0);
				picasso.load(profileImageUrl).resize(IMG_SIZE, IMG_SIZE).centerCrop().placeholder(placeholder).transform(roundCorner).error(R.drawable.no_image).into(profile);
			} else {
				profile.setImageDrawable(placeholder);
			}
		} else {
			profile.setImageResource(0);
			username.setText(R.string.account_user_unnamed);
			screenname.setText(R.string.account_user_id_prefix);
			screenname.append(Long.toString(account.getId()));
		}
		switch (account.getConfiguration()) {
			case TWITTER1:
			case TWITTER2:
				screenname.append(" @twitter.com");
				break;

			case MASTODON:
				String host = account.getHostname();
				if (host.startsWith("https://"))
					host = host.substring(8);
				screenname.append(" @" + host);
				break;
		}
	}
}