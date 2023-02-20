package org.nuclearfog.twidda.backend.helper;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is used to create a status poll
 *
 * @author nuclearfog
 * @see StatusUpdate
 */
public class PollUpdate {

	private int validity;
	private boolean multipleChoice;
	private boolean hideTotals;
	private List<String> options;


	public PollUpdate() {
		options = new LinkedList<>();
	}

	/**
	 * get validity in seconds
	 *
	 * @return time until the poll is finnished
	 */
	public int getValidity() {
		return validity;
	}

	/**
	 * @return true if multiple choice is enabled
	 */
	public boolean multipleChoiceEnabled() {
		return multipleChoice;
	}

	/**
	 * @return true to hide total votes until poll is finnished
	 */
	public boolean hideTotalVotes() {
		return hideTotals;
	}

	/**
	 * @return an array of vote options
	 */
	public String[] getOptions() {
		return options.toArray(new String[0]);
	}


	@NonNull
	@Override
	public String toString() {
		return "valid=" + validity + " multiple=" + multipleChoice + "options=" + options.size();
	}
}