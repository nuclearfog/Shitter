package org.nuclearfog.twidda.model.lists;

import org.nuclearfog.twidda.model.Emoji;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author nuclearfog
 */
public class Emojis extends ArrayList<Emoji> {

	private static final long serialVersionUID = 9203293103283870491L;

	/**
	 *
	 */
	public Emojis() {
	}

	/**
	 *
	 */
	public Emojis(Emoji[] emojis) {
		super(Arrays.asList(emojis));
	}
}