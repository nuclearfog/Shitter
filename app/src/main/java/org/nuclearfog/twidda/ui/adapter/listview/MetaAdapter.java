package org.nuclearfog.twidda.ui.adapter.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Media;

import java.util.ArrayList;
import java.util.List;

/**
 * ListView adapter used to show Meta items
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.dialogs.MetaDialog
 */
public class MetaAdapter extends BaseAdapter {

	private List<String> keys = new ArrayList<>();
	private List<String> values = new ArrayList<>();

	private Context context;
	private GlobalSettings settings;

	/**
	 *
	 */
	public MetaAdapter(Context context) {
		this.context = context;
		settings = GlobalSettings.get(context);
	}


	@Override
	public int getCount() {
		return Math.min(keys.size(), values.size());
	}


	@Override
	public Object getItem(int position) {
		return new String[]{keys.get(position), values.get(position)};
	}


	@Override
	public long getItemId(int position) {
		return keys.get(position).hashCode();
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView key_text;
		TextView value_text;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_meta_field, parent, false);
			key_text = convertView.findViewById(R.id.item_meta_field_key);
			value_text = convertView.findViewById(R.id.item_meta_field_value);
			key_text.setTextColor(settings.getTextColor());
			value_text.setTextColor(settings.getTextColor());
		} else {
			key_text = convertView.findViewById(R.id.item_meta_field_key);
			value_text = convertView.findViewById(R.id.item_meta_field_value);
		}
		key_text.setText(keys.get(position));
		value_text.setText(values.get(position));
		return convertView;
	}

	/**
	 * set adapter items
	 *
	 * @param meta media meta information
	 */
	public void setItems(Media.Meta meta) {
		if (meta.getWidth() > 1 && meta.getHeight() > 1) {
			keys.add(context.getString(R.string.dialog_meta_width));
			values.add(StringUtils.NUMBER_FORMAT.format(meta.getWidth()));
			keys.add(context.getString(R.string.dialog_meta_height));
			values.add(StringUtils.NUMBER_FORMAT.format(meta.getHeight()));
		}
		if (meta.getBitrate() > 0) {
			keys.add(context.getString(R.string.dialog_meta_bitrate));
			values.add(StringUtils.NUMBER_FORMAT.format(meta.getBitrate()) + " kbit/s");
		}
		if (meta.getFrameRate() > 0) {
			keys.add(context.getString(R.string.dialog_meta_framerate));
			values.add(StringUtils.NUMBER_FORMAT.format(meta.getFrameRate()) + " fps");
		}
		if (meta.getDuration() > 0) {
			StringBuilder durationValue = new StringBuilder();
			keys.add(context.getString(R.string.dialog_meta_duration));
			long hours = Math.round(meta.getDuration() / 3600);
			long mins = Math.round(meta.getDuration() / 60) % 60L;
			long sec = Math.round(meta.getDuration() % 60.0);
			if (hours > 0) {
				if (hours < 10)
					durationValue.append('0');
				durationValue.append(hours).append(':');
			}
			if (mins < 10)
				durationValue.append('0');
			durationValue.append(mins).append(':');
			if (sec < 10)
				durationValue.append('0');
			durationValue.append(sec).append('\n');
			values.add(durationValue.toString());
		}
		notifyDataSetChanged();
	}
}