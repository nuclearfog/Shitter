package org.nuclearfog.twidda.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.ui.adapter.holder.AccountHolder;
import org.nuclearfog.twidda.ui.adapter.holder.OnHolderClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter to show a list of accounts
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.fragments.AccountFragment
 */
public class AccountAdapter extends Adapter<AccountHolder> implements OnHolderClickListener {

	private GlobalSettings settings;
	private OnAccountClickListener listener;
	private Picasso picasso;

	private List<Account> accounts;

	/**
	 * @param listener item click listener
	 */
	public AccountAdapter(Context context, OnAccountClickListener listener) {
		picasso = PicassoBuilder.get(context);
		settings = GlobalSettings.getInstance(context);
		accounts = new ArrayList<>();
		this.listener = listener;
	}


	@NonNull
	@Override
	public AccountHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new AccountHolder(parent, settings, picasso, this);
	}


	@Override
	public void onBindViewHolder(@NonNull AccountHolder holder, int position) {
		Account account = accounts.get(position);
		holder.setContent(account);
	}


	@Override
	public int getItemCount() {
		return accounts.size();
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		Account account = accounts.get(position);
		if (type == OnHolderClickListener.ACCOUNT_SELECT) {
			listener.onAccountClick(account);
		} else if (type == OnHolderClickListener.ACCOUNT_REMOVE) {
			listener.onAccountRemove(account);
		}
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		return false;
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
	 * clear adapter data
	 */
	public void clear() {
		accounts.clear();
		notifyDataSetChanged();
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