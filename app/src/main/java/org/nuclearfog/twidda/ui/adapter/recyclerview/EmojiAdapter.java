package org.nuclearfog.twidda.ui.adapter.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.EmojiHolder;
import org.nuclearfog.twidda.ui.adapter.recyclerview.holder.OnHolderClickListener;

import java.util.LinkedList;
import java.util.List;

/**
 * Recyclerview adapter used to show a list of grouped emojis
 *
 * @author nuclearfog
 */
public class EmojiAdapter extends Adapter<EmojiHolder> implements OnHolderClickListener {

	private OnEmojiClickListener listener;

	private LinkedList<Object> items = new LinkedList<>();

	/**
	 *
	 */
	public EmojiAdapter(OnEmojiClickListener listener) {
		this.listener = listener;
	}


	@NonNull
	@Override
	public EmojiHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new EmojiHolder(parent, this);
	}


	@Override
	public void onBindViewHolder(@NonNull EmojiHolder holder, int position) {
		Object item = items.get(position);
		if (item instanceof Emoji[]) {
			holder.setContent((Emoji[]) item);
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

	/**
	 * add new emoji items
	 *
	 * @param emojis emoji items
	 */
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

	/**
	 * @return true if adapter is empty
	 */
	public boolean isEmpty() {
		return items.isEmpty();
	}

	/**
	 * Listener used to send emoji information on click
	 */
	public interface OnEmojiClickListener {

		/**
		 * called when an emoji was clicked
		 *
		 * @param emoji selected emoji
		 */
		void onEmojiClick(Emoji emoji);
	}
}