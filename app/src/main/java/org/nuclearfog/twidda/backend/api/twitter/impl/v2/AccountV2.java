package org.nuclearfog.twidda.backend.api.twitter.impl.v2;

import org.nuclearfog.twidda.backend.api.twitter.impl.v1.AccountV1;
import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.model.Account;

/**
 * Twitter 'API 2.0 account implementation
 *
 * @author nuclearfog
 */
public class AccountV2 extends AccountV1 {

	private static final long serialVersionUID = -1001326005675835242L;


	public AccountV2(Account account) {
		super(account.getOauthToken(), account.getOauthSecret(), account.getConsumerToken(), account.getConsumerSecret(), account.getUser());
	}


	@Override
	public Configuration getConfiguration() {
		return Configuration.TWITTER2;
	}
}