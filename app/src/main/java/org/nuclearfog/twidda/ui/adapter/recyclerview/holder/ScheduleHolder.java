package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.textviewtool.LinkAndScrollMovement;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.ScheduledStatus;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.adapter.recyclerview.IconAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.IconAdapter.OnIconClickListener;

import java.text.SimpleDateFormat;

/**
 * @author nuclearfog
 */
public class ScheduleHolder extends ViewHolder implements OnClickListener, OnIconClickListener {

	private TextView time, text, visibility;
	private RecyclerView iconList;

	private IconAdapter adapter;
	private OnHolderClickListener listener;
	private GlobalSettings settings;

	/**
	 *
	 */
	public ScheduleHolder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false));
		this.listener = listener;
		settings = GlobalSettings.get(parent.getContext());
		CardView cardLayout = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_schedule_container);
		View removeButton = itemView.findViewById(R.id.item_schedule_delete_button);
		visibility = itemView.findViewById(R.id.item_schedule_visibility);
		iconList = itemView.findViewById(R.id.item_schedule_attachment);
		time = itemView.findViewById(R.id.item_schedule_time);
		text = itemView.findViewById(R.id.item_schedule_text);

		adapter = new IconAdapter(this, false);
		iconList.setLayoutManager(new LinearLayoutManager(parent.getContext(), RecyclerView.HORIZONTAL, false));
		iconList.setAdapter(adapter);
		text.setMovementMethod(LinkAndScrollMovement.getInstance());

		time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.schedule, 0, 0, 0);
		visibility.setCompoundDrawablesWithIntrinsicBounds(R.drawable.visibility, 0, 0, 0);
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


	@Override
	public void onIconClick(int type, int index) {
		int position = getLayoutPosition();
		if (position != RecyclerView.NO_POSITION) {
			if (type == OnIconClickListener.MEDIA) {
				listener.onItemClick(position, OnHolderClickListener.STATUS_MEDIA, index);
			} else if (type == OnIconClickListener.POLL) {
				listener.onItemClick(position, OnHolderClickListener.STATUS_POLL, index);
			}
		}
	}

	/**
	 *
	 */
	public void setContent(ScheduledStatus status) {
		time.setText(SimpleDateFormat.getDateTimeInstance().format(status.getPublishTime()));
		text.setText(Tagger.makeText(status.getText(), settings.getHighlightColor()));
		if (status.getVisibility() == Status.VISIBLE_PRIVATE) {
			visibility.setText(R.string.status_visibility_private);
			visibility.setVisibility(View.VISIBLE);
		} else if (status.getVisibility() == Status.VISIBLE_DIRECT) {
			visibility.setText(R.string.status_visibility_direct);
			visibility.setVisibility(View.VISIBLE);
		} else if (status.getVisibility() == Status.VISIBLE_UNLISTED) {
			visibility.setText(R.string.status_visibility_unlisted);
			visibility.setVisibility(View.VISIBLE);
		} else {
			visibility.setVisibility(View.GONE);
		}
		if (status.getMedia().length > 0 || status.getPoll() != null) {
			adapter.setItems(status.getMedia());
			if (status.getPoll() != null)
				adapter.addPollItem();
			iconList.setVisibility(View.VISIBLE);
		} else {
			iconList.setVisibility(View.GONE);
		}
	}
}