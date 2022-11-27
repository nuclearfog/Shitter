package org.nuclearfog.twidda.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.holder.AccountHolder;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter to show a list of accounts
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.fragments.AccountFragment
 */
public class AccountAdapter extends Adapter<AccountHolder> {

	private GlobalSettings settings;
	private OnAccountClickListener listener;
	private Resources resources;
	private Picasso picasso;

	private List<Account> accounts = new ArrayList<>();

	/**
	 * @param listener item click listener
	 */
	public AccountAdapter(Context context, OnAccountClickListener listener) {
		this.listener = listener;
		picasso = PicassoBuilder.get(context);
		settings = GlobalSettings.getInstance(context);
		resources = context.getResources();
	}


	@NonNull
	@Override
	public AccountHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		final AccountHolder holder = new AccountHolder(parent, settings);
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = holder.getLayoutPosition();
				if (position != NO_POSITION) {
					Account account = accounts.get(position);
					listener.onAccountClick(account);
				}
			}
		});
		holder.remove.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = holder.getLayoutPosition();
				if (position != NO_POSITION) {
					Account account = accounts.get(position);
					listener.onAccountRemove(account);
				}
			}
		});
		return holder;
	}


	@Override
	public void onBindViewHolder(@NonNull AccountHolder holder, int position) {
		Account account = accounts.get(position);
		User user = account.getUser();
		String date = StringTools.formatCreationTime(resources, account.getLoginDate());
		holder.date.setText(date);
		if (user != null) {
			// set profile information
			holder.username.setText(user.getUsername());
			holder.screenname.setText(user.getScreenname());
			// set profile image
			if (settings.imagesEnabled() && !user.getProfileUrl().isEmpty()) {
				String profileImageUrl;
				if (!user.hasDefaultProfileImage() && account.getApiType() == Account.API_TWITTER) {
					profileImageUrl = StringTools.buildImageLink(user.getImageUrl(), settings.getImageSuffix());
				} else {
					profileImageUrl = user.getImageUrl();
				}
				picasso.load(profileImageUrl).transform(new RoundedCornersTransformation(2, 0)).error(R.drawable.no_image).into(holder.profile);
			} else {
				holder.profile.setImageResource(0);
			}
		} else {
			holder.profile.setImageResource(0);
			holder.username.setText(R.string.account_user_unnamed);
			holder.screenname.setText(R.string.account_user_id_prefix);
			holder.screenname.append(Long.toString(account.getId()));
		}
	}


	@Override
	public int getItemCount() {
		return accounts.size();
	}

	/**
	 * sets login data
	 *
	 * @param newAccounts list with login items
	 */
	public void replaceItems(List<Account> newAccounts) {
		accounts.clear();
		accounts.addAll(newAccounts);
		notifyDataSetChanged();
	}

	/**
	 * remove single item with specific ID
	 *
	 * @param id Id of the element to remove
	 */
	public void removeItem(long id) {
		for (int i = accounts.size() - 1; i >= 0; i--) {
			Account account = accounts.get(i);
			if (account != null && account.getId() == id) {
				accounts.remove(i);
				notifyItemRemoved(i);
				break;
			}
		}
	}

	/**
	 * click listener for an account item
	 */
	public interface OnAccountClickListener {

		/**
		 * called on item select
		 *
		 * @param account selected account information
		 */
		void onAccountClick(Account account);

		/**
		 * called to remove item
		 *
		 * @param account selected account information
		 */
		void onAccountRemove(Account account);
	}
}