package org.nuclearfog.twidda.backend.api.twitter.v1.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Instance;

/**
 * Twitter API v1.1 configuration
 *
 * @author nuclearfog
 */
public class TwitterV1Instance implements Instance {


	private static final long serialVersionUID = 6248302391974167770L;

	private String hostname;

	/**
	 * @param hostname currently used hostname
	 */
	public TwitterV1Instance(String hostname) {
		this.hostname = hostname;
	}


	@Override
	public String getTitle() {
		return "Twitter";
	}


	@Override
	public String getDomain() {
		return hostname;
	}


	@Override
	public String getVersion() {
		return "1.1";
	}


	@Override
	public String getDescription() {
		return "";
	}


	@Override
	public long getTimestamp() {
		return Long.MAX_VALUE;
	}


	@Override
	public int getHashtagFollowLimit() {
		return 0;
	}


	@Override
	public int getStatusCharacterLimit() {
		return 280;
	}


	@Override
	public int getImageLimit() {
		return 4;
	}


	@Override
	public int getVideoLimit() {
		return 1;
	}


	@Override
	public int getGifLimit() {
		return 1;
	}


	@Override
	public int getAudioLimit() {
		return 0;
	}


	@Override
	public String[] getSupportedFormats() {
		return new String[]{"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "video/mp4", "video/mov", "video/3gp", "video/webm"};
	}


	@Override
	public int getImageSizeLimit() {
		return 5000000;
	}


	@Override
	public int getGifSizeLimit() {
		return 15000000;
	}


	@Override
	public int getVideoSizeLimit() {
		return 512000000;
	}


	@Override
	public int getAudioSizeLimit() {
		return 0;
	}


	@Override
	public int getPollOptionsLimit() {
		return 0;
	}


	@Override
	public int getPollOptionCharacterLimit() {
		return 0;
	}


	@Override
	public int getMinPollDuration() {
		return 0;
	}


	@Override
	public int getMaxPollDuration() {
		return 0;
	}


	@Override
	public boolean isTranslationSupported() {
		return false;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof TwitterV1Instance))
			return false;
		TwitterV1Instance instance = (TwitterV1Instance) obj;
		return instance.hostname.equals(hostname);
	}


	@NonNull
	@Override
	public String toString() {
		return "domain=\"" + getDomain() + " \" version=\"2.0\"";
	}
}