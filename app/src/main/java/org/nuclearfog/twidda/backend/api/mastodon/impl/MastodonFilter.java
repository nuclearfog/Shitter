package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.Filter;

/**
 * Mastodon implementation of a status filter
 *
 * @author nuclearfog
 */
public class MastodonFilter implements Filter {

	private static final long serialVersionUID = -3363900731940590588L;

	private long id;
	private long expiresAt = 0L;
	private String title;
	private Keyword[] keywords;
	private int action;
	private boolean filterHome, filterNotification, filterPublic, filterUser, filterThread;

	/**
	 * @param json Mastodon Filter json
	 */
	public MastodonFilter(JSONObject json) throws JSONException {
		JSONArray typeArray = json.getJSONArray("context");
		JSONArray keywordArray = json.getJSONArray("keywords");
		String idStr = json.getString("id");
		String actionStr = json.getString("filter_action");
		String expiresStr = json.optString("expires_at");
		title = json.getString("title");
		switch (actionStr) {
			default:
			case "warn":
				action = ACTION_WARN;
				break;

			case "hide":
				action = ACTION_HIDE;
				break;
		}
		for (int i = 0 ; i < typeArray.length() ; i++) {
			switch (typeArray.getString(i)) {
				case "home":
					filterHome = true;
					break;

				case "notifications":
					filterNotification = true;
					break;

				case "public":
					filterPublic = true;
					break;

				case "account":
					filterUser = true;
					break;

				case "thread":
					filterThread = true;
					break;
			}
		}
		keywords = new Keyword[keywordArray.length()];
		for (int i = 0 ; i < keywordArray.length() ; i++) {
			keywords[i] = new MastodonKeyword(keywordArray.getJSONObject(i));
		}
		if (!expiresStr.equals("null")) {
			expiresAt = StringUtils.getTime(expiresStr, StringUtils.TIME_MASTODON);
		}
		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException exception) {
			throw new JSONException("Bad ID: " + idStr);
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getTitle() {
		return title;
	}


	@Override
	public long getExpirationTime() {
		return expiresAt;
	}


	@Override
	public Keyword[] getKeywords() {
		return keywords;
	}


	@Override
	public int getAction() {
		return action;
	}


	@Override
	public boolean filterHome() {
		return filterHome;
	}


	@Override
	public boolean filterNotifications() {
		return filterNotification;
	}


	@Override
	public boolean filterPublic() {
		return filterPublic;
	}


	@Override
	public boolean filterThreads() {
		return filterThread;
	}


	@Override
	public boolean filterUserTimeline() {
		return filterUser;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Filter))
			return false;
		return ((Filter) obj).getId() == getId();
	}

	/**
	 *
	 */
	private static class MastodonKeyword implements Keyword {

		private static final long serialVersionUID = -2619670483101168015L;

		private long id;
		private String keyword;
		private boolean oneWord;

		/**
		 * @param json json object containing filter keyword
		 */
		private MastodonKeyword(JSONObject json) throws JSONException {
			String idStr = json.getString("id");
			keyword = json.getString("keyword");
			oneWord = json.optBoolean("whole_word", false);
			try {
				id = Long.parseLong(idStr);
			} catch (NumberFormatException exception) {
				throw new JSONException("Bad ID: " + idStr);
			}
		}


		@Override
		public long getId() {
			return id;
		}


		@Override
		public String getKeyword() {
			return keyword;
		}


		@Override
		public boolean isOneWord() {
			return oneWord;
		}
	}
}