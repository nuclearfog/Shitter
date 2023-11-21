package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.backend.utils.Tagger;
import org.nuclearfog.twidda.backend.utils.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.User.Field;

/**
 * ViewHolder for {@link org.nuclearfog.twidda.ui.adapter.recyclerview.FieldAdapter}
 *
 * @author nuclearfog
 */
public class FieldHolder extends ViewHolder {

	private View verified;
	private TextView key, value, time;

	private GlobalSettings settings;
	private OnTagClickListener listener;

	/**
	 *
	 */
	public FieldHolder(ViewGroup parent, OnTagClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_field, parent, false));
		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_field_container);
		verified = itemView.findViewById(R.id.item_field_verified);
		key = itemView.findViewById(R.id.item_field_key);
		value = itemView.findViewById(R.id.item_field_value);
		time = itemView.findViewById(R.id.item_field_timestamp);
		settings = GlobalSettings.get(parent.getContext());
		this.listener = listener;

		value.setMovementMethod(LinkMovementMethod.getInstance());
		background.setCardBackgroundColor(settings.getCardColor());
		AppStyles.setTheme(container, Color.TRANSPARENT);
	}

	/**
	 * set view content
	 */
	public void setContent(Field field) {
		key.setText(field.getKey());
		value.setText(Tagger.makeTextWithLinks(field.getValue(), settings.getHighlightColor(), listener));
		if (field.getTimestamp() != 0L) {
			verified.setVisibility(View.VISIBLE);
			time.setText(R.string.field_verified);
			time.append(StringUtils.formatCreationTime(time.getResources(), field.getTimestamp()));
		} else {
			verified.setVisibility(View.GONE);
			time.setVisibility(View.GONE);
		}
	}
}