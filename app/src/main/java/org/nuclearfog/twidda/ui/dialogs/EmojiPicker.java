package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.EmojiLoader;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.ui.adapter.EmojiAdapter;
import org.nuclearfog.twidda.ui.adapter.EmojiAdapter.OnEmojiClickListener;

import java.util.List;


public class EmojiPicker extends BottomSheetDialog implements AsyncCallback<List<Emoji>>, OnEmojiClickListener {

	private OnEmojiSelectListener listener;
	private EmojiAdapter adapter;


	public EmojiPicker(@NonNull Activity activity, OnEmojiSelectListener listener) {
		super(activity);
		RecyclerView listView = new RecyclerView(getContext());
		GlobalSettings settings  = GlobalSettings.getInstance(getContext());
		setContentView(listView);

		BottomSheetBehavior<View> mBehavior = BottomSheetBehavior.from((View)listView.getParent());
		mBehavior.setPeekHeight(500);
		listView.setBackgroundColor(settings.getBackgroundColor());

		this.listener = listener;
		adapter = new EmojiAdapter(getContext(), this);
		listView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		listView.setAdapter(adapter);

		EmojiLoader emojiLoader = new EmojiLoader(getContext());
		emojiLoader.execute(null, this);
	}


	@Override
	public void onResult(@NonNull List<Emoji> emojis) {
		adapter.replaceItems(emojis);
	}


	@Override
	public void show() {
		if (!isShowing()) {
			super.show();
		}
	}


	@Override
	public void onEmojiClick(Emoji emoji) {
		listener.onEmojiSelected(emoji);
	}


	public interface OnEmojiSelectListener {

		void onEmojiSelected(Emoji emoji);
	}
}