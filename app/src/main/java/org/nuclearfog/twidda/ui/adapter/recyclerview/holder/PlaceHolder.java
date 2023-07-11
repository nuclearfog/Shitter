package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * ViewHolder class for a placeholder view
 *
 * @author nuclearfog
 */
public class PlaceHolder extends ViewHolder implements OnClickListener {

	private ProgressBar circle;
	private TextView label;

	private OnHolderClickListener listener;

	/**
	 * @param parent     Parent view from adapter
	 * @param horizontal true if placeholder orientation is horizontal
	 */
	public PlaceHolder(ViewGroup parent, OnHolderClickListener listener, boolean horizontal) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_placeholder, parent, false));
		this.listener = listener;

		CardView background = (CardView) itemView;
		circle = itemView.findViewById(R.id.placeholder_loading);
		label = itemView.findViewById(R.id.placeholder_button);

		GlobalSettings settings = GlobalSettings.get(parent.getContext());
		background.setCardBackgroundColor(settings.getCardColor());
		label.setTextColor(settings.getTextColor());
		label.setTypeface(settings.getTypeFace());
		AppStyles.setProgressColor(circle, settings.getHighlightColor());

		// enable extra views
		if (horizontal && background.getLayoutParams() != null) {
			label.setVisibility(View.INVISIBLE);
			circle.setVisibility(View.VISIBLE);
			background.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
			background.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
		}
		itemView.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v == itemView) {
			int position = getLayoutPosition();
			if (position != RecyclerView.NO_POSITION) {
				boolean enableLoading = listener.onPlaceholderClick(position);
				setLoading(enableLoading);
			}
		}
	}

	/**
	 * enable or disable progress circle
	 *
	 * @param enable true to enable progress, false to disable
	 */
	public void setLoading(boolean enable) {
		if (enable) {
			circle.setVisibility(View.VISIBLE);
			label.setVisibility(View.INVISIBLE);
		} else {
			circle.setVisibility(View.INVISIBLE);
			label.setVisibility(View.VISIBLE);
		}
	}
}