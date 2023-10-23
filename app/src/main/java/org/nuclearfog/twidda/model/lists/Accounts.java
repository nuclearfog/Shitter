package org.nuclearfog.twidda.model.lists;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.Account;

import java.util.LinkedList;

/**
 * @author nuclearfog
 */
public class Accounts extends LinkedList<Account> {

	private static final long serialVersionUID = 6868101446772507083L;

	/**
	 *
	 */
	public Accounts() {
		super();
	}

	/**
	 * @param accounts account list to clone
	 */
	public Accounts(Accounts accounts) {
		super(accounts);
	}


	@NonNull
	@Override
	public String toString() {
		return "item_count=" + size();
	}
}