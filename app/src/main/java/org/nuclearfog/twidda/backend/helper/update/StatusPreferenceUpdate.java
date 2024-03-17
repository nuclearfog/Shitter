package org.nuclearfog.twidda.backend.helper.update;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.Status;

import java.io.Serializable;

/**
 * Used to set preferences of a status update
 *
 * @author nuclearfog
 */
public class StatusPreferenceUpdate implements Serializable {

	private static final long serialVersionUID = 1942415485336294199L;

	private boolean sensitive, spoiler;
	private int visibility;
	private String lang = "";
	private long scheduleAt;

	/**
	 * @return true if status contains sensitive content
	 */
	public boolean isSensitive() {
		return sensitive;
	}

	/**
	 * @param sensitive true to enable "sensitive content" warning
	 */
	public void setSensitive(boolean sensitive) {
		this.sensitive = sensitive;
	}

	/**
	 * @return true to enable "spoiler" warining
	 */
	public boolean isSpoiler() {
		return spoiler;
	}

	/**
	 * @param spoiler true to enable "spoiler" warning
	 */
	public void setSpoiler(boolean spoiler) {
		this.spoiler = spoiler;
	}

	/**
	 * @return status visibility to other users e.g. {@link Status#VISIBLE_DEFAULT}
	 */
	public int getVisibility() {
		return visibility;
	}

	/**
	 * @param visibility status visibility to other users e.g. {@link Status#VISIBLE_DEFAULT}
	 */
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	/**
	 * @return default lanugage of a status (language code)
	 */
	public String getLanguage() {
		return lang;
	}

	/**
	 * set language information
	 *
	 * @param lang ISO 639 Part 1 two-letter language code or empty
	 */
	public void setLanguage(@NonNull String lang) {
		this.lang = lang;
	}

	/**
	 * @return datetime when the status will be posted
	 */
	public long getScheduleTime() {
		return scheduleAt;
	}

	/**
	 * @param scheduleAt datetime when the status will be posted
	 */
	public void setScheduleTime(long scheduleAt) {
		this.scheduleAt = scheduleAt;
	}


	@NonNull
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("lang=").append(lang);
		buf.append(" isSensitive=").append(sensitive);
		buf.append(" isSpoiler=").append(isSpoiler());
		buf.append(" visibility=");
		if (visibility == Status.VISIBLE_DEFAULT)
			buf.append("default");
		else if (visibility == Status.VISIBLE_DIRECT)
			buf.append("direct");
		else if (visibility == Status.VISIBLE_PRIVATE)
			buf.append("private");
		else if (visibility == Status.VISIBLE_PUBLIC)
			buf.append("public");
		else if (visibility == Status.VISIBLE_UNLISTED)
			buf.append("unlisted");
		else
			buf.append("unknown");
		return buf.toString();
	}
}