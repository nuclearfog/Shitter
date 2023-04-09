package org.nuclearfog.twidda.backend.api.twitter.v2.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.twitter.v1.impl.TwitterV1Instance;

/**
 * Twitter API v2.0 configuration
 *
 * @author nuclearfog
 */
public class TwitterV2Instance extends TwitterV1Instance {


	private static final long serialVersionUID = 5979539035652732059L;

	/**
	 * @param hostname currently used hostname
	 */
	public TwitterV2Instance(String hostname) {
		super(hostname);
	}


	@Override
	public String getVersion() {
		return "2.0";
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof TwitterV2Instance))
			return false;
		TwitterV2Instance instance = (TwitterV2Instance) obj;
		return instance.getDomain().equals(getDomain());
	}


	@NonNull
	@Override
	public String toString() {
		return "domain=\"" + getDomain() + " \" version=\"2.0\"";
	}
}