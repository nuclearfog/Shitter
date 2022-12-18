package org.nuclearfog.twidda.backend.api.twitter.impl;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Card;
import org.nuclearfog.twidda.model.Metrics;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;

import java.util.LinkedList;
import java.util.List;

/**
 * Tweet implementation of Twitter API v2 adding extra information to Tweet V1.1
 *
 * @author nuclearfog
 */
public class TweetV2  implements Status {

	private static final long serialVersionUID = -2740140825640061692L;

	public static final String FIELDS_TWEET = "attachments%2Cconversation_id%2Centities%2Cpublic_metrics%2Creply_settings";
	public static final String FIELDS_POLL ="duration_minutes%2Cend_datetime%2Cid%2Coptions%2Cvoting_status";
	public static final String FIELDS_EXPANSION = "attachments.poll_ids";

	private long conversationId;
	private int replyCount;
	private Status tweetCompat;
	private Metrics metrics;
	private List<Card> cards = new LinkedList<>();

	/**
	 * @param json Tweet v2 json
	 * @param tweetCompat Tweet containing base informations
	 */
	public TweetV2(JSONObject json, Status tweetCompat) throws JSONException {
		JSONObject publicMetrics = json.getJSONObject("public_metrics");
		JSONObject entities = json.getJSONObject("entities");
		JSONArray urls = entities.optJSONArray("urls");
		String conversationIdStr = json.optString("conversation_id", "-1");
		JSONObject metricsData = json.optJSONObject("data");
		replyCount = publicMetrics.getInt("reply_count");
		if (!conversationIdStr.equals("null")) {
			conversationId = Long.parseLong(conversationIdStr);
		} else {
			conversationId = -1L;
		}
		if (metricsData != null) {
			metrics = new MetricsV2(metricsData, tweetCompat.getId());
		}
		if (urls != null) {
			for (int i = 0 ; i < urls.length() ; i++) {
				TwitterCard item = new TwitterCard(urls.getJSONObject(i));
				if (!item.getUrl().startsWith("https://twitter.com"))
					cards.add(item);
			}
		}
		this.tweetCompat = tweetCompat;
	}


	@Override
	public long getId() {
		return tweetCompat.getId();
	}


	@Override
	public String getText() {
		return tweetCompat.getText();
	}


	@Override
	public User getAuthor() {
		return tweetCompat.getAuthor();
	}


	@Override
	public long getTimestamp() {
		return tweetCompat.getTimestamp();
	}


	@Override
	public String getSource() {
		return tweetCompat.getSource();
	}


	@Nullable
	@Override
	public Status getEmbeddedStatus() {
		return tweetCompat.getEmbeddedStatus();
	}


	@Override
	public String getReplyName() {
		return tweetCompat.getReplyName();
	}


	@Override
	public long getRepliedUserId() {
		return tweetCompat.getRepliedUserId();
	}


	@Override
	public long getRepliedStatusId() {
		return tweetCompat.getRepliedStatusId();
	}


	@Override
	public long getConversationId() {
		return conversationId;
	}


	@Override
	public long getRepostId() {
		return tweetCompat.getRepostId();
	}


	@Override
	public int getRepostCount() {
		return tweetCompat.getRepostCount();
	}


	@Override
	public int getFavoriteCount() {
		return tweetCompat.getFavoriteCount();
	}


	@Override
	public int getReplyCount() {
		return replyCount;
	}


	@NonNull
	@Override
	public Uri[] getMediaUris() {
		return tweetCompat.getMediaUris();
	}


	@Override
	public String getUserMentions() {
		return tweetCompat.getUserMentions();
	}


	@Override
	public int getMediaType() {
		return tweetCompat.getMediaType();
	}


	@Override
	public boolean isSensitive() {
		return tweetCompat.isSensitive();
	}


	@Override
	public boolean isReposted() {
		return tweetCompat.isReposted();
	}


	@Override
	public boolean isFavorited() {
		return tweetCompat.isFavorited();
	}


	@Override
	public boolean isHidden() {
		return tweetCompat.isHidden();
	}

	@Override
	public String getLinkPath() {
		return tweetCompat.getLinkPath();
	}


	@Override
	public String getLocationName() {
		return tweetCompat.getLocationName();
	}


	@Override
	public String getLocationCoordinates() {
		return tweetCompat.getLocationCoordinates();
	}


	@Override
	public Card[] getCards() {
		return cards.toArray(new Card[0]);
	}


	@Nullable
	@Override
	public Poll getPoll() {
		// todo add implementation
		return null;
	}


	@Nullable
	@Override
	public Metrics getMetrics() {
		return metrics;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Status))
			return false;
		return ((Status) obj).getId() == tweetCompat.getId();
	}


	@NonNull
	@Override
	public String toString() {
		return tweetCompat.toString();
	}
}