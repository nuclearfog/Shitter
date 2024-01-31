package org.nuclearfog.twidda.backend.helper.update;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.Status;

import java.io.Serializable;

/**
 * @author nuclearfog
 */
public class StatusPreferenceUpdate implements Serializable {

	private static final long serialVersionUID = 1942415485336294199L;

	private boolean sensitive, spoiler;
	private int visibility;
	private String lang = "";
	private long scheduleAt;


	public boolean isSensitive() {
		return sensitive;
	}


	public void setSensitive(boolean sensitive) {
		this.sensitive = sensitive;
	}


	public boolean isSpoiler() {
		return spoiler;
	}


	public void setSpoiler(boolean spoiler) {
		this.spoiler = spoiler;
	}


	public int getVisibility() {
		return visibility;
	}

	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}


	public String getLanguage() {
		return lang;
	}


	public void setLanguage(@NonNull String lang) {
		this.lang = lang;
	}


	public long getScheduleTime() {
		return scheduleAt;
	}


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