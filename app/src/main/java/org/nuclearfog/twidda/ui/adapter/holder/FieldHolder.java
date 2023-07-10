package org.nuclearfog.twidda.ui.adapter.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.User.Field;

public class FieldHolder extends ViewHolder {

	private View verified;
	private TextView key, value, time;


	public FieldHolder(ViewGroup parent) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_field, parent, false));
		verified = itemView.findViewById(R.id.item_field_verified);
		key = itemView.findViewById(R.id.item_field_key);
		value = itemView.findViewById(R.id.item_field_value);
		time = itemView.findViewById(R.id.item_field_timestamp);
	}


	public void setContent(Field field) {
		key.setText(field.getKey());
		value.setText(field.getValue());
		if (field.getTimestamp() != 0L) {
			verified.setVisibility(View.VISIBLE);
			time.setText(R.string.field_verified_in);
			time.append(StringUtils.formatCreationTime(time.getResources(), field.getTimestamp()));
		} else {
			verified.setVisibility(View.GONE);
			time.setVisibility(View.GONE);
		}
	}
}