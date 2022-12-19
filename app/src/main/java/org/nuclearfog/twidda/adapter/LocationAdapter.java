package org.nuclearfog.twidda.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.ui.activities.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for Location selection spinner
 *
 * @author nuclearfog
 * @see SettingsActivity
 */
public class LocationAdapter extends BaseAdapter {

	private GlobalSettings settings;

	private List<Location> locations;

	/**
	 *
	 */
	public LocationAdapter(GlobalSettings settings) {
		locations = new ArrayList<>();
		this.settings = settings;
	}


	@Override
	public int getCount() {
		return locations.size();
	}


	@Override
	public Location getItem(int pos) {
		return locations.get(pos);
	}


	@Override
	public long getItemId(int pos) {
		return getItem(pos).getWorldId();
	}


	@Override
	public View getView(int pos, View view, ViewGroup parent) {
		TextView textItem;
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			view = inflater.inflate(R.layout.item_dropdown, parent, false);
		}
		textItem = view.findViewById(R.id.dropdown_textitem);
		textItem.setBackgroundColor(settings.getCardColor());
		textItem.setTextColor(settings.getFontColor());
		textItem.setTypeface(settings.getTypeFace());
		textItem.setText(locations.get(pos).getFullName());
		view.setBackgroundColor(settings.getBackgroundColor());
		return view;
	}

	/**
	 * Add a single item to top
	 *
	 * @param top top item to add
	 */
	public void addItem(Location top) {
		locations.add(top);
		notifyDataSetChanged();
	}

	/**
	 * replace content with new items
	 *
	 * @param newData item list
	 */
	public void replaceItems(List<Location> newData) {
		locations.clear();
		locations.addAll(newData);
		notifyDataSetChanged();
	}

	/**
	 * get position of the item or "0" if not found
	 *
	 * @param item item to search
	 * @return index of the item
	 */
	public int indexOf(Location item) {
		int pos = locations.indexOf(item);
		if (pos == -1) {
			return 0;
		}
		return pos;
	}
}