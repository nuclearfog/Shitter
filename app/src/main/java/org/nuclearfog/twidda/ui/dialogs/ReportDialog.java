package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;

public class ReportDialog extends Dialog {

	private TextView textTitle;
	private SwitchButton switchForward;
	private EditText editDescription;

	/**
	 *
	 */
	public ReportDialog(Activity activity) {
		super(activity, R.style.ReportDialog);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_report);
		ViewGroup root = findViewById(R.id.dialog_report_root);
		textTitle = findViewById(R.id.dialog_report_title);
		switchForward = findViewById(R.id.dialog_report_switch_forward);
		editDescription = findViewById(R.id.dialog_report_description);
		AppStyles.setTheme(root);
	}

	@Override
	public void show() {
	}
}