package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.model.Media.Meta;
import org.nuclearfog.twidda.ui.adapter.listview.MetaAdapter;

/**
 * Dialog to show media information
 *
 * @author nuclearfog
 */
public class MetaDialog extends Dialog {

	private MetaAdapter adapter;

	/**
	 *
	 */
	public MetaDialog(Activity activity) {
		super(activity, R.style.MetaDialog);
		adapter = new MetaAdapter(activity.getApplicationContext());
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_meta);
		ViewGroup root = findViewById(R.id.dialog_meta_root);
		ListView list = findViewById(R.id.dialog_meta_list);

		list.setAdapter(adapter);
		AppStyles.setTheme(root);
	}


	@Override
	public void show() {
		// using show(Meta) instead
	}


	@Override
	public void dismiss() {
		if (isShowing()) {
			super.dismiss();
		}
	}

	/**
	 *
	 */
	public void show(@NonNull Meta meta) {
		if (!isShowing()) {
			super.show();
			adapter.setItems(meta);
		}
	}
}