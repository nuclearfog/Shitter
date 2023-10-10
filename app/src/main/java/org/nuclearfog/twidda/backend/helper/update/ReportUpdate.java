package org.nuclearfog.twidda.backend.helper.update;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Updater class to create report of status/user
 *
 * @author nuclearfog
 */
public class ReportUpdate implements Serializable {

	private static final long serialVersionUID = 7643792374030657129L;

	public static final int CATEGORY_OTHER = 10;
	public static final int CATEGORY_SPAM = 11;
	public static final int CATEGORY_VIOLATION = 12;

	private long userId;
	private long[] statusIds = {};
	private long[] ruleIds = {};
	private String comment = "";
	private int category = CATEGORY_OTHER;
	private boolean forward = false;

	/**
	 * @param userId user ID to report
	 */
	public ReportUpdate(long userId) {
		this.userId = userId;
	}

	/**
	 * get user ID to report
	 *
	 * @return user ID
	 */
	public long getUserId() {
		return userId;
	}

	/**
	 * set status ID's related to user ID
	 *
	 * @param statusIds array of status IDs
	 */
	public void setStatusIds(long[] statusIds) {
		this.statusIds = Arrays.copyOf(statusIds, statusIds.length);
	}

	/**
	 * get status ID's related to user ID
	 *
	 * @return array of status IDs
	 */
	public long[] getStatusIds() {
		return Arrays.copyOf(statusIds, statusIds.length);
	}

	/**
	 * set rule IDs violated by user
	 *
	 * @param ruleIds array of rule IDs
	 */
	public void setRuleIds(long[] ruleIds) {
		this.ruleIds = Arrays.copyOf(ruleIds, ruleIds.length);
	}

	/**
	 * get rule IDs violated by user
	 *
	 * @return array of rule IDs
	 */
	public long[] getRuleIds() {
		return Arrays.copyOf(ruleIds, ruleIds.length);
	}

	/**
	 * add additional comment to violation
	 *
	 * @param comment comment attached to the report
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * get additional comment to violation
	 *
	 * @return comment attached to the report
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * set category of violation
	 *
	 * @param category violation category {@link #CATEGORY_VIOLATION ,#CATEGORY_SPAM,#CATEGORY_OTHER}
	 */
	public void setCategory(int category) {
		this.category = category;
	}

	/**
	 * get category of violation
	 *
	 * @return violation category {@link #CATEGORY_VIOLATION ,#CATEGORY_SPAM,#CATEGORY_OTHER}
	 */
	public int getCategory() {
		return category;
	}

	/**
	 * set report forwarding to source instance
	 *
	 * @param forward true to forward report to source instance
	 */
	public void setForward(boolean forward) {
		this.forward = forward;
	}

	/**
	 * should report be forwarded to source instance
	 *
	 * @return true to forward report to source instance
	 */
	public boolean getForward() {
		return forward;
	}


	@NonNull
	@Override
	public String toString() {
		return "userID=" + userId;
	}
}