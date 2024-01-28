package org.nuclearfog.twidda.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Media.Meta;
import org.nuclearfog.twidda.ui.adapter.listview.MetaAdapter;

/**
 * Dialog to show media information
 *
 * @author nuclearfog
 */
public class MetaDialog extends DialogFragment {

	private static final String KEY_META = "meta-data";

	private Meta meta;


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_meta, container, false);
		ListView list = view.findViewById(R.id.dialog_meta_list);
		MetaAdapter adapter = new MetaAdapter(requireContext());
		GlobalSettings settings = GlobalSettings.get(requireContext());

		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_META);
			if (data instanceof Meta) {
				meta = (Meta) data;
				adapter.setItems(meta);
			}
		}
		list.setAdapter(adapter);
		AppStyles.setTheme((ViewGroup) view, settings.getPopupColor());
		return view;
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_META, meta);
		super.onSaveInstanceState(outState);
	}

	/**
	 * show meta dialog
	 *
	 * @param activity activity from which to show this dialog
	 * @param meta     media meta information to show
	 */
	public static void show(FragmentActivity activity, Meta meta) {
		String tag = "MetaDialog: " + meta.hashCode();
		Bundle args = new Bundle();
		args.putSerializable(KEY_META, meta);
		Fragment dialogFragment = activity.getSupportFragmentManager().findFragmentByTag(tag);
		if (dialogFragment == null) {
			MetaDialog dialog = new MetaDialog();
			dialog.setArguments(args);
			dialog.show(activity.getSupportFragmentManager(), tag);
		}
	}
}