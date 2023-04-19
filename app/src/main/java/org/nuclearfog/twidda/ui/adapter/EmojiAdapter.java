package org.nuclearfog.twidda.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.ui.adapter.holder.EmojiHolder;
import org.nuclearfog.twidda.ui.adapter.holder.OnHolderClickListener;

import java.util.LinkedList;
import java.util.List;


public class EmojiAdapter extends Adapter<EmojiHolder> implements OnHolderClickListener {

	private OnEmojiClickListener listener;
	private GlobalSettings settings;
	private Picasso picasso;

	private LinkedList<Object> items = new LinkedList<>();


	public EmojiAdapter(Context context, OnEmojiClickListener listener) {
		settings = GlobalSettings.getInstance(context);
		picasso = PicassoBuilder.get(context);
		this.listener = listener;
	}


	@NonNull
	@Override
	public EmojiHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new EmojiHolder(parent, settings, picasso, this);
	}


	@Override
	public void onBindViewHolder(@NonNull EmojiHolder holder, int position) {
		Object item = items.get(position);
		if (item instanceof Emoji[]) {
			holder.setData((Emoji[]) item);
		} else if (item instanceof String) {
			holder.setLabel((String) item);
		}
	}


	@Override
	public int getItemCount() {
		return items.size();
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		Object item = items.get(position);
		if (item instanceof Emoji[]) {
			Emoji[] emoji = (Emoji[]) item;
			int index = extras[0];
			if (emoji[index] != null) {
				listener.onEmojiClick(emoji[index]);
			}
		}
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		return false;
	}


	public void replaceItems(List<Emoji> emojis) {
		items.clear();
		String groupname = "";
		for (Emoji emoji : emojis) {
			Emoji[] row;
			Object item = items.peekLast();
			if (!emoji.getCategory().equals(groupname)) {
				row = new Emoji[EmojiHolder.ROW_COUNT];
				groupname = emoji.getCategory();
				row[0] = emoji;
				if (!groupname.trim().isEmpty())
					items.add(groupname);
				items.add(row);
			} else if (item instanceof Emoji[]) {
				row = (Emoji[]) item;
				if (row[EmojiHolder.ROW_COUNT - 1] == null) {
					for (int j = 0; j < row.length; j++) {
						if (row[j] == null) {
							row[j] = emoji;
							break;
						}
					}
				} else {
					row = new Emoji[EmojiHolder.ROW_COUNT];
					row[0] = emoji;
					items.add(row);
				}
			}
		}
		notifyDataSetChanged();
	}


	public interface OnEmojiClickListener {

		void onEmojiClick(Emoji emoji);
	}
}