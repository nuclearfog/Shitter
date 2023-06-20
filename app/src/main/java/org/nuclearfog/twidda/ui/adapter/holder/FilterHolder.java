package org.nuclearfog.twidda.ui.adapter.holder;

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
 *
 */
public class FilterHolder extends ViewHolder implements OnClickListener {

	private View icon_home, icon_public, icon_user, icon_thread, icon_notification;
	private TextView title, date, action;
	private ViewGroup keyword_container;

	private OnHolderClickListener listener;


	public FilterHolder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter, parent, false));
		this.listener = listener;
		ViewGroup container = itemView.findViewById(R.id.item_filter_container);
		CardView background = (CardView) itemView;
		View icon_remove = itemView.findViewById(R.id.item_filter_remove);
		title = itemView.findViewById(R.id.item_filter_title);
		date = itemView.findViewById(R.id.item_filter_expiration);
		action = itemView.findViewById(R.id.item_filter_action);
		keyword_container = itemView.findViewById(R.id.item_filter_keyword_list);
		icon_home = itemView.findViewById(R.id.item_filter_icon_home);
		icon_public = itemView.findViewById(R.id.item_filter_icon_public);
		icon_user = itemView.findViewById(R.id.item_filter_icon_user);
		icon_thread = itemView.findViewById(R.id.item_filter_icon_thread);
		icon_notification = itemView.findViewById(R.id.item_filter_icon_notification);
		GlobalSettings settings = GlobalSettings.get(parent.getContext());
		background.setCardBackgroundColor(settings.getCardColor());
		AppStyles.setTheme(container);

		itemView.setOnClickListener(this);
		icon_remove.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position == RecyclerView.NO_POSITION)
			return;
		if (v == itemView) {

		} else if (v.getId() == R.id.item_filter_remove) {

		}
	}


	public void setData(Filter filter) {
		title.setText(filter.getTitle());
		date.setText(StringUtils.formatCreationTime(date.getResources(), filter.getExpirationTime()));
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
	}
}