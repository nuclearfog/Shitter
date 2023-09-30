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
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.ScheduledStatus;

import java.text.SimpleDateFormat;

/**
 * @author nuclearfog
 */
public class ScheduleHolder extends ViewHolder implements OnClickListener {

	private TextView time, text;

	private OnHolderClickListener listener;

	/**
	 *
	 */
	public ScheduleHolder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false));
		this.listener = listener;
		GlobalSettings settings = GlobalSettings.get(parent.getContext());
		CardView cardLayout = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_schedule_container);
		View removeButton = itemView.findViewById(R.id.item_schedule_delete_button);
		time = itemView.findViewById(R.id.item_schedule_time);
		text = itemView.findViewById(R.id.item_schedule_text);

		time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.schedule, 0, 0, 0);
		AppStyles.setTheme(container, Color.TRANSPARENT);
		cardLayout.setCardBackgroundColor(settings.getCardColor());

		container.setOnClickListener(this);
		removeButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int position = getLayoutPosition();
		if (position != RecyclerView.NO_POSITION) {
			if (v.getId() == R.id.item_schedule_container) {
				listener.onItemClick(position, OnHolderClickListener.SCHEDULE_CLICK);
			} else if (v.getId() == R.id.item_schedule_delete_button) {
				listener.onItemClick(position, OnHolderClickListener.SCHEDULE_REMOVE);
			}
		}
	}

	/**
	 *
	 */
	public void setContent(ScheduledStatus status) {
		time.setText(SimpleDateFormat.getDateTimeInstance().format(status.getPublishTime()));
		text.setText(status.getText());
	}
}