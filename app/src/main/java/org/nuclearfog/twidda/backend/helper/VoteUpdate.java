package org.nuclearfog.twidda.backend.helper;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.Poll;

import java.util.Set;
import java.util.TreeSet;

/**
 * This class is used to send a vote to a poll
 *
 * @author nuclearfog
 */
public class VoteUpdate {

	private long id;
	private boolean multipleChoice;
	private Set<Integer> choices;

	/**
	 * @param poll poll to vote
	 */
	public VoteUpdate(Poll poll) {
		id = poll.getId();
		multipleChoice = poll.multipleChoice();
		choices = new TreeSet<>();
	}

	/**
	 * select index of the selected vote option, starting with "0"
	 *
	 * @param index index of the option
	 */
	public void setChoice(int index) {
		if (!multipleChoice || choices.isEmpty()) {
			choices.add(index);
		}
	}

	/**
	 * ID of the poll
	 *
	 * @return poll ID
	 */
	public long getPollId() {
		return id;
	}

	/**
	 * get all selected vote option
	 *
	 * @return array of selected option index
	 */
	public int[] getSelected() {
		int i = 0;
		int[] result = new int[choices.size()];
		for (Integer choice : choices) {
			result[i] = choice;
			i++;
		}
		return result;
	}


	@NonNull
	@Override
	public String toString() {
		return "id=" + id + " choices=" + choices.size();
	}
}