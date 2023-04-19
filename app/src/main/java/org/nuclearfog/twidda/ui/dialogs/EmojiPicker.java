package org.nuclearfog.twidda.ui.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.adapter.EmojiAdapter;

public class EmojiPicker extends BottomSheetDialog {

	private EmojiAdapter adapter;

	public EmojiPicker(@NonNull Context context) {
		super(context);
		RecyclerView listView = new RecyclerView(context);
		GlobalSettings settings  = GlobalSettings.getInstance(context);
		setContentView(listView);

		BottomSheetBehavior<View> mBehavior = BottomSheetBehavior.from((View)listView.getParent());
		mBehavior.setPeekHeight(500);
		listView.setBackgroundColor(settings.getBackgroundColor());

		adapter = new EmojiAdapter(context);
	}


}