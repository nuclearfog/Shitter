package org.nuclearfog.twidda.backend.api.twitter.impl.v1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Message;
import org.nuclearfog.twidda.model.User;

/**
 * API 1.1 implementation of a directmessage
 *
 * @author nuclearfog
 */
public class MessageV1 implements Message {

	private static final long serialVersionUID = 6821470849021163618L;

	private long id;
	private long timestamp;
	private long sender_id;
	private long receiver_id;
	private User sender;
	private String text;
	private Media media;

	/**
	 * @param json JSON object containing directmessage information
	 * @throws JSONException if some values are missing
	 */
	public MessageV1(JSONObject json) throws JSONException {
		JSONObject message = json.getJSONObject("message_create");
		JSONObject target = message.getJSONObject("target");
		JSONObject data = message.getJSONObject("message_data");
		JSONObject attachment = data.optJSONObject("attachment");
		String idStr = json.getString("id");

		timestamp = Long.parseLong(json.getString("created_timestamp"));
		sender_id = Long.parseLong(message.getString("sender_id"));
		receiver_id = Long.parseLong(target.getString("recipient_id"));
		text = setText(data);
		if (attachment != null) {
			JSONObject mediaJson = attachment.optJSONObject("media");
			if (mediaJson != null) {
				media = new MediaV1(mediaJson);
			}
		}
		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException e) {
			throw new JSONException("bad message ID:" + idStr);
		}

	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public User getSender() {
		return sender;
	}


	@Override
	public String getText() {
		return text;
	}


	@Override
	public long getTimestamp() {
		return timestamp;
	}


	@Override
	public long getReceiverId() {
		return receiver_id;
	}


	@Nullable
	@Override
	public Media getMedia() {
		return media;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Message))
			return false;
		return ((Message) obj).getId() == id;
	}


	@NonNull
	@Override
	public String toString() {
		return "from=" + sender + " message=\"" + text + "\"";
	}

	/**
	 * get ID of the sender
	 *
	 * @return user ID
	 */
	public long getSenderId() {
		return sender_id;
	}

	/**
	 * add sender information
	 *
	 * @param sender user information
	 */
	public void addSender(User sender) {
		this.sender = sender;
	}

	/**
	 * set message text and expand urls
	 *
	 * @param data message data
	 */
	private String setText(JSONObject data) {
		String text = data.optString("text", "");
		JSONObject entities = data.optJSONObject("entities");
		if (entities != null) {
			JSONArray urls = entities.optJSONArray("urls");
			if (urls != null) {
				try {
					StringBuilder buf = new StringBuilder(text);
					for (int pos = urls.length() - 1; pos >= 0; pos--) {
						JSONObject url = urls.getJSONObject(pos);
						String displayUrl = url.getString("display_url");
						String expandedUrl = url.getString("expanded_url");
						JSONArray indices = url.getJSONArray("indices");
						int start = indices.getInt(0);
						int end = indices.getInt(1);
						if (displayUrl.startsWith("pic.twitter.com")) {
							// remove media link
							buf.delete(start, end);
						} else {
							// replace shortened url with original url
							buf.replace(start, end, expandedUrl);
						}
					}
					return buf.toString();
				} catch (JSONException e) {
					// ignore, return default text
				}
			}
		}
		return text;
	}
}