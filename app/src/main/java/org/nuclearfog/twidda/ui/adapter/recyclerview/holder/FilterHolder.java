package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

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
import org.nuclearfog.twidda.model.Filter;

/**
 * View Holder used for FilterAdapter
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.adapter.recyclerview.FilterAdapter
 */
public class FilterHolder extends ViewHolder implements OnClickListener {

	private View icon_home, icon_public, icon_user, icon_thread, icon_notification;
	private TextView title, date, action, keywords;

	private OnHolderClickListener listener;

	/**
	 *
	 */
	public FilterHolder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter, parent, false));
		this.listener = listener;
		ViewGroup container = itemView.findViewById(R.id.item_filter_container);
		CardView background = (CardView) itemView;
		View icon_remove = itemView.findViewById(R.id.item_filter_remove);
		title = itemView.findViewById(R.id.item_filter_title);
		date = itemView.findViewById(R.id.item_filter_expiration);
		action = itemView.findViewById(R.id.item_filter_action);
		keywords = itemView.findViewById(R.id.item_filter_keyword_list);
		icon_home = itemView.findViewById(R.id.item_filter_icon_home);
		icon_public = itemView.findViewById(R.id.item_filter_icon_public);
		icon_user = itemView.findViewById(R.id.item_filter_icon_user);
		icon_thread = itemView.findViewById(R.id.item_filter_icon_thread);
		icon_notification = itemView.findViewById(R.id.item_filter_icon_notification);

		GlobalSettings settings = GlobalSettings.get(parent.getContext());
		background.setCardBackgroundColor(settings.getCardColor());
		AppStyles.setTheme(container, Color.TRANSPARENT);

		itemView.setOnClickListener(this);
		icon_remove.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position == RecyclerView.NO_POSITION)
			return;
		if (v == itemView) {
			listener.onItemClick(position, OnHolderClickListener.FILTER_CLICK);
		} else if (v.getId() == R.id.item_filter_remove) {
			listener.onItemClick(position, OnHolderClickListener.FILTER_REMOVE);
		}
	}

	/**
	 * set view content
	 *
	 * @param filter view content to display
	 */
	public void setContent(Filter filter) {
		title.setText(filter.getTitle());
		if (filter.getExpirationTime() != 0L) {
			date.setText(StringUtils.formatExpirationTime(date.getResources(), filter.getExpirationTime()));
			date.setVisibility(View.VISIBLE);
		} else {
			date.setText("");
			date.setVisibility(View.GONE);
		}
		if (filter.getAction() == Filter.ACTION_HIDE) {
			action.setText(R.string.filter_hide);
		} else {
			action.setText(R.string.filter_warn);
		}
		if (filter.filterHome()) {
			icon_home.setVisibility(View.VISIBLE);
		} else {
			icon_home.setVisibility(View.GONE);
		}
		if (filter.filterNotifications()) {
			icon_notification.setVisibility(View.VISIBLE);
		} else {
			icon_notification.setVisibility(View.GONE);
		}
		if (filter.filterPublic()) {
			icon_public.setVisibility(View.VISIBLE);
		} else {
			icon_public.setVisibility(View.GONE);
		}
		if (filter.filterThreads()) {
			icon_thread.setVisibility(View.VISIBLE);
		} else {
			icon_thread.setVisibility(View.GONE);
		}
		if (filter.filterUserTimeline()) {
			icon_user.setVisibility(View.VISIBLE);
		} else {
			icon_user.setVisibility(View.GONE);
		}
		StringBuilder builder = new StringBuilder();
		for (Filter.Keyword keyword : filter.getKeywords()) {
			if (keyword.isOneWord()) {
				builder.append("\"").append(keyword.getKeyword()).append("\"");
			} else {
				builder.append(keyword.getKeyword());
			}
			builder.append('\n');
		}
		if (builder.length() > 1)
			builder.deleteCharAt(builder.length() - 1);
		keywords.setText(builder);
	}
}