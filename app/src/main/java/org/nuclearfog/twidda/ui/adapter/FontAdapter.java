package org.nuclearfog.twidda.ui.adapter;

import static org.nuclearfog.twidda.config.GlobalSettings.FONT_NAMES;
import static org.nuclearfog.twidda.config.GlobalSettings.FONT_TYPES;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.activities.SettingsActivity;

/**
 * Spinner Adapter for font settings
 *
 * @author nuclearfog
 * @see SettingsActivity
 */
public class FontAdapter extends BaseAdapter {

	private GlobalSettings settings;

	/**
	 * @param settings app settings for background and font color
	 */
	public FontAdapter(GlobalSettings settings) {
		this.settings = settings;
	}


	@Override
	public int getCount() {
		return FONT_TYPES.length;
	}


	@Override
	public long getItemId(int pos) {
		return pos;
	}


	@Override
	public Typeface getItem(int pos) {
		return FONT_TYPES[pos];
	}


	@Override
	public View getView(int pos, View view, ViewGroup parent) {
		TextView textItem;
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			view = inflater.inflate(R.layout.item_dropdown, parent, false);
		}
		textItem = view.findViewById(R.id.dropdown_textitem);
		textItem.setText(FONT_NAMES[pos]);
		textItem.setTypeface(FONT_TYPES[pos]);
		textItem.setTextColor(settings.getTextColor());
		textItem.setBackgroundColor(settings.getCardColor());
		view.setBackgroundColor(settings.getBackgroundColor());
		return view;
	}
}