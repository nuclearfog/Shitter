package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.lists.Accounts;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.AccountHolder;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.OnHolderClickListener;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter to show a list of accounts
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.fragments.AccountFragment
 */
public class AccountAdapter extends Adapter<AccountHolder> implements OnHolderClickListener {

	private OnAccountClickListener listener;

	private Accounts items = new Accounts();

	/**
	 * @param listener item click listener
	 */
	public AccountAdapter(OnAccountClickListener listener) {
		this.listener = listener;
	}


	@NonNull
	@Override
	public AccountHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new AccountHolder(parent, this);
	}


	@Override
	public void onBindViewHolder(@NonNull AccountHolder holder, int position) {
		Account account = items.get(position);
		holder.setContent(account);
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		Account account = items.get(position);
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
	 * get adapter items
	 *
	 * @return list of adapter items
	 */
	public Accounts getItems() {
		return new Accounts(items);
	}

	/**
	 * sets login data
	 *
	 * @param newAccounts list with login items
	 */
	public void setItems(Accounts newAccounts) {
		items.clear();
		items.addAll(newAccounts);
		notifyDataSetChanged();
	}

	/**
	 * remove single item with specific ID
	 *
	 * @param account account to remove
	 */
	public void removeItem(Account account) {
		int idx = items.indexOf(account);
		if (idx >= 0) {
			items.remove(idx);
			notifyItemRemoved(idx);
		}
	}

	/**
	 * clear adapter data
	 */
	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}

	/**
	 * @return true if adapter doesn't contain any items
	 */
	public boolean isEmpty() {
		return items.isEmpty();
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