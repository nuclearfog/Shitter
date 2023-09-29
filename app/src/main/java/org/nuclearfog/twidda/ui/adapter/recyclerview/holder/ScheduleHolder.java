package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.ScheduledStatus;

/**
 * @author nuclearfog
 */
public class ScheduleHolder extends ViewHolder implements OnClickListener {

	private TextView time, text;

	private OnHolderClickListener listener;


	public ScheduleHolder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false));
		this.listener = listener;
		GlobalSettings settings = GlobalSettings.get(parent.getContext());
		CardView cardLayout = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_schedule_container);
		time = itemView.findViewById(R.id.item_schedule_time);
		text = itemView.findViewById(R.id.item_schedule_text);

		AppStyles.setTheme(container, Color.TRANSPARENT);
		cardLayout.setCardBackgroundColor(settings.getCardColor());


	}


	@Override
	public void onClick(View v) {

	}

	/**
	 *
	 */
	public void setContent(ScheduledStatus status) {
		time.setText(StringUtils.formatExpirationTime(time.getResources(), status.getPublishTime()));
		text.setText(status.getText());
	}
}