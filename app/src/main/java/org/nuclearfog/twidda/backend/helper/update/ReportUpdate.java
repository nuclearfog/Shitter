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

	/**
	 * report category for other violations
	 */
	public static final int CATEGORY_OTHER = 10;

	/**
	 * report category for spam
	 */
	public static final int CATEGORY_SPAM = 11;

	/**
	 * report category for rule violation
	 */
	public static final int CATEGORY_VIOLATION = 12;

	private long userId;
	private long[] statusIds;
	private long[] ruleIds = {};
	private String comment = "";
	private int category = CATEGORY_OTHER;
	private boolean forward = false;

	/**
	 *
	 */
	public ReportUpdate() {
		userId = 0L;
		statusIds = new long[0];
	}

	/**
	 * @param userId user ID to report
	 */
	public ReportUpdate(long userId, long[] statusIds) {
		this.userId = userId;
		this.statusIds = Arrays.copyOf(statusIds, statusIds.length);
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