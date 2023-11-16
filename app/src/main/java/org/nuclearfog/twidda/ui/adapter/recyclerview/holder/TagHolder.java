package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Tag;
import org.nuclearfog.twidda.ui.adapter.recyclerview.TagAdapter;

/**
 * ViewHolder for a trend item
 *
 * @author nuclearfog
 * @see TagAdapter
 */
public class TagHolder extends ViewHolder implements OnClickListener {

	private TextView name, rank, vol;

	private OnHolderClickListener listener;


	public TagHolder(ViewGroup parent, OnHolderClickListener listener, boolean enableRemove) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false));
		this.listener = listener;

		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_tag_container);
		View btnRemove = itemView.findViewById(R.id.item_tag_delete_button);
		rank = itemView.findViewById(R.id.item_tag_rank);
		name = itemView.findViewById(R.id.item_tag_name);
		vol = itemView.findViewById(R.id.item_tag_vol);

		GlobalSettings settings = GlobalSettings.get(parent.getContext());
		AppStyles.setTheme(container, Color.TRANSPARENT);
		background.setCardBackgroundColor(settings.getCardColor());
		if (enableRemove) {
			btnRemove.setVisibility(View.VISIBLE);
		} else {
			btnRemove.setVisibility(View.GONE);
		}
		itemView.setOnClickListener(this);
		btnRemove.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (v.getId() == R.id.item_tag_delete_button) {
			if (position != RecyclerView.NO_POSITION) {
				listener.onItemClick(position, OnHolderClickListener.TAG_REMOVE);
			}
		} else if (v == itemView) {
			if (position != RecyclerView.NO_POSITION) {
				listener.onItemClick(position, OnHolderClickListener.TAG_CLICK);
			}
		}
	}

	/**
	 * set view content
	 *
	 * @param tag   content information
	 * @param index index of the item
	 */
	public void setContent(Tag tag, int index) {
		rank.setText(index + 1 + ".");
		name.setText(tag.getName());
		if (tag.getPopularity() > 0) {
			Resources resources = vol.getResources();
			String trendVol = StringUtils.NUMBER_FORMAT.format(tag.getPopularity()) + resources.getString(R.string.trend_range);
			vol.setText(trendVol);
			vol.setVisibility(View.VISIBLE);
		} else {
			vol.setVisibility(View.GONE);
		}
	}
}