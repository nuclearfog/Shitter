package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.update.FilterUpdate;
import org.nuclearfog.twidda.model.Filter;

/**
 * Filter update dialog
 *
 * @author nuclearfog
 */
public class FilterDialog extends Dialog {

	private FilterUpdate update;

	/**
	 *
	 */
	public FilterDialog(Activity activity) {
		super(activity, R.style.FilterDialog);
	}


	@Override
	public void show() {
	}

	/**
	 * create dialog window
	 *
	 * @param filter configuration of an existing filter to update
	 */
	public void show(@Nullable Filter filter) {
		if (filter != null) {
			update = new FilterUpdate(filter);
		} else {
			update = new FilterUpdate();
		}
	}
}