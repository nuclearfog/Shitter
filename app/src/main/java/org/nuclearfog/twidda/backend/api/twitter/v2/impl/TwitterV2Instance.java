package org.nuclearfog.twidda.backend.api.twitter.v2.impl;

import org.nuclearfog.twidda.backend.api.twitter.v1.impl.TwitterV1Instance;
import org.nuclearfog.twidda.model.Instance;

/**
 * Twitter API v2.0 configuration
 *
 * @author nuclearfog
 */
public class TwitterV2Instance extends TwitterV1Instance {


	private static final long serialVersionUID = 5979539035652732059L;


	@Override
	public String getVersion() {
		return "2.0";
	}
}