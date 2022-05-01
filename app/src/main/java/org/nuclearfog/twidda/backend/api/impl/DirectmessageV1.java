package org.nuclearfog.twidda.backend.api.impl;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.DirectMessage;
import org.nuclearfog.twidda.model.User;

/**
 * API 1.1 implementation of a directmessage
 *
 * @author nuclearfog
 */
public class DirectmessageV1 implements DirectMessage {

    private long id;
    private long timestamp;
    private long sender_id;
    private long receiver_id;
    private User sender;
    private User receiver;
    private String text;
    private String mediaLink;


    public DirectmessageV1(JSONObject json) throws JSONException {
        id = Long.parseLong(json.getString("id"));
        timestamp = Long.parseLong(json.getString("created_timestamp"));
        JSONObject message = json.getJSONObject("message_create");
        JSONObject target = message.getJSONObject("target");
        JSONObject data = message.getJSONObject("message_data");
        sender_id = Long.parseLong(message.getString("sender_id"));
        receiver_id = Long.parseLong(target.getString("recipient_id"));
        mediaLink = setMedia(data);
        text = setText(data);
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
    public User getReceiver() {
        return receiver;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Nullable
    @Override
    public Uri getMedia() {
        if (!mediaLink.isEmpty())
            return Uri.parse(mediaLink);
        return null;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DirectMessage))
            return false;
        return ((DirectMessage) obj).getId() == id;
    }

    @NonNull
    @Override
    public String toString() {
        return "from:" + sender + " to:" + receiver + " message:\"" + text + "\"";
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
     * get ID of the receiver
     *
     * @return user ID
     */
    public long getReceiverId() {
        return receiver_id;
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
     * add receiver information
     *
     * @param receiver user information
     */
    public void addReceiver(User receiver) {
        this.receiver = receiver;
    }

    /**
     * add media links
     *
     * @param data message data
     */
    private String setMedia(JSONObject data) {
        JSONObject attachment = data.optJSONObject("attachment");
        if (attachment != null) {
            try {
                JSONObject urls = attachment.getJSONObject("media");
                return urls.getString("media_url_https");
            } catch (JSONException e) {
                // ignore
            }
        }
        return "";
    }

    /**
     * set message text and expand urls
     *
     * @param data message data
     */
    private String setText(JSONObject data) {
        String text = data.optString("text");
        StringBuilder buf = new StringBuilder(text);
        JSONObject entities = data.optJSONObject("entities");
        if (entities != null) {
            try {
                JSONArray urls = entities.getJSONArray("urls");
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
            } catch (JSONException e) {
                // ignore, set default text
            }
        }
        return buf.toString();
    }
}