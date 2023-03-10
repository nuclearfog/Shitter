package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.PollUpdate;

/**
 * Dialog class used to show poll editor
 *
 * @author nuclearfog
 */
public class PollDialog extends Dialog {


	public PollDialog(@NonNull Context context) {
		super(context, R.style.PollDialog);
		setContentView(R.layout.dialog_poll);
	}



	public void show(@NonNull PollUpdate poll) {

	}
}