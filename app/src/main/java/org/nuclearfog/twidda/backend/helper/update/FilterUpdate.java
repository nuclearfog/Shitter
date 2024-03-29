package org.nuclearfog.twidda.backend.helper.update;

import org.nuclearfog.twidda.model.Filter;
import org.nuclearfog.twidda.model.Filter.Keyword;

import java.io.Serializable;

/**
 * Filter update class used to create or update an existing status filter
 *
 * @author nuclearfog
 */
public class FilterUpdate implements Serializable {

	private static final long serialVersionUID = 7408688572155707380L;

	private long id;
	private String title;
	private String[] keyWordStr;
	private long[] keywordIds;
	private int expires_at;
	private int action;
	private boolean filterHome;
	private boolean filterNotification;
	private boolean filterPublic;
	private boolean filterUser;
	private boolean filterThread;

	/**
	 *
	 */
	public FilterUpdate() {
		id = 0L;
		title = "";
		expires_at = 0;
		filterHome = false;
		filterNotification = false;
		filterPublic = true;
		filterUser = false;
		filterThread = false;
		action = Filter.ACTION_HIDE;
		keywordIds = new long[0];
		keyWordStr = new String[0];
	}

	/**
	 * create filter update with existing filter configuration
	 *
	 * @param filter existing filter
	 */
	public FilterUpdate(Filter filter) {
		id = filter.getId();
		title = filter.getTitle();
		action = filter.getAction();
		filterHome = filter.filterHome();
		filterNotification = filter.filterNotifications();
		filterPublic = filter.filterPublic();
		filterUser = filter.filterUserTimeline();
		filterThread = filter.filterThreads();
		// set keywords and their IDs
		Keyword[] keywords = filter.getKeywords();
		keyWordStr = new String[keywords.length];
		keywordIds = new long[keywords.length];
		for (int i = 0; i < keywords.length; i++) {
			keywordIds[i] = keywords[i].getId();
			if (keywords[i].isOneWord()) {
				keyWordStr[i] = '\"' + keywords[i].getKeyword() + '\"';
			} else {
				keyWordStr[i] = keywords[i].getKeyword();
			}
		}
		// calculate remaining time to expire
		int remainingTime = (int) ((filter.getExpirationTime() - System.currentTimeMillis()) / 1000L);
		expires_at = Math.max(remainingTime, 0);
	}

	/**
	 * filter ID of an existing filter or '0' if a new filter should be created
	 *
	 * @return filter ID
	 */
	public long getId() {
		return id;
	}

	/**
	 * get filter title
	 *
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * set title of the filter
	 *
	 * @param title title (description)
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * get time to expiration in seconds
	 *
	 * @return expiration time
	 */
	public int getExpirationTime() {
		return expires_at;
	}

	/**
	 * set time to expire
	 *
	 * @param expires_at time until filter expires in seconds
	 */
	public void setExpirationTime(int expires_at) {
		this.expires_at = expires_at;
	}

	/**
	 * get IDs of existing keywords
	 *
	 * @return array of keyword IDs
	 */
	public long[] getKeywordIds() {
		return keywordIds;
	}

	/**
	 * get an array of keywords to filter
	 *
	 * @return array of keywords
	 */
	public String[] getKeywords() {
		return keyWordStr;
	}

	/**
	 * add keywords of the filter
	 *
	 * @param keywords array of keywords
	 */
	public void setKeywords(String[] keywords) {
		this.keyWordStr = keywords.clone();
	}

	/**
	 * get filter action
	 *
	 * @return filter action {@link Filter#ACTION_WARN,Filter#ACTION_HIDE}
	 */
	public int getFilterAction() {
		return action;
	}

	/**
	 * set filter action
	 *
	 * @param action filter action {@link Filter#ACTION_WARN,Filter#ACTION_HIDE}
	 */
	public void setFilterAction(int action) {
		this.action = action;
	}

	/**
	 * @return true if filter is set for home timeline
	 */
	public boolean filterHomeSet() {
		return filterHome;
	}

	/**
	 * enable/disable filter for home timeline
	 *
	 * @param filterHome true to enable filter
	 */
	public void setFilterHome(boolean filterHome) {
		this.filterHome = filterHome;
	}

	/**
	 * @return true if filter is set for notifications
	 */
	public boolean filterNotificationSet() {
		return filterNotification;
	}

	/**
	 * enable/disable notification filter
	 *
	 * @param filterNotification true to enable filter
	 */
	public void setFilterNotification(boolean filterNotification) {
		this.filterNotification = filterNotification;
	}

	/**
	 * @return true if filter is set for public timeline
	 */
	public boolean filterPublicSet() {
		return filterPublic;
	}

	/**
	 * enable/disable filter for public timeline
	 *
	 * @param filterPublic true to enable filter
	 */
	public void setFilterPublic(boolean filterPublic) {
		this.filterPublic = filterPublic;
	}

	/**
	 * @return true if filter is set for user timeline
	 */
	public boolean filterUserSet() {
		return filterUser;
	}

	/**
	 * enable/disable filter for user timeline
	 *
	 * @param filterUser true to enable filter
	 */
	public void setFilterUser(boolean filterUser) {
		this.filterUser = filterUser;
	}

	/**
	 * @return true if filter is set for threads
	 */
	public boolean filterThreadSet() {
		return filterThread;
	}

	/**
	 * enable/disable filter for threads
	 *
	 * @param filterThread true to enable filter
	 */
	public void setFilterThread(boolean filterThread) {
		this.filterThread = filterThread;
	}
}