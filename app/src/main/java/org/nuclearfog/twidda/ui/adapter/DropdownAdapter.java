package org.nuclearfog.twidda.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.ArrayRes;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.GlobalSettings;

import java.util.Arrays;

/**
 * Dropdown list adapter used for spinners
 *
 * @author nuclearfog
 */
public class DropdownAdapter extends BaseAdapter {

	private GlobalSettings settings;
	private Context context;

	private String[] items = {};
	private Typeface[] fonts = {};

	/**
	 *
	 */
	public DropdownAdapter(Context context) {
		settings = GlobalSettings.getInstance(context);
		this.context = context;
	}


	@Override
	public int getCount() {
		return items.length;
	}


	@Override
	public String getItem(int position) {
		return items[position];
	}


	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textItem;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(R.layout.item_dropdown, parent, false);
			textItem = convertView.findViewById(R.id.dropdown_textitem);
			textItem.setTextColor(settings.getTextColor());
			textItem.setTypeface(settings.getTypeFace());
			textItem.setBackgroundColor(settings.getCardColor());
			convertView.setBackgroundColor(settings.getBackgroundColor());
		} else {
			textItem = convertView.findViewById(R.id.dropdown_textitem);
		}
		textItem.setText(items[position]);
		if (fonts.length >= items.length) {
			textItem.setTypeface(fonts[position]);
		}
		return convertView;
	}

	/**
	 * set single item
	 *
	 * @param item item string
	 */
	public void setItem(String item) {
		this.items = new String[]{item};
		notifyDataSetChanged();
	}

	/**
	 * set items from string array resource
	 *
	 * @param arrayRes array resource containing strings
	 */
	public void setItems(@ArrayRes int arrayRes) {
		TypedArray tArray = context.getResources().obtainTypedArray(arrayRes);
		items = new String[tArray.length()];
		for (int i = 0; i < tArray.length(); i++) {
			String item = tArray.getString(i);
			if (item != null) {
				items[i] = item;
			} else {
				items[i] = "";
			}
		}
		tArray.recycle();
		notifyDataSetChanged();
	}

	/**
	 * set items from string array
	 *
	 * @param items string array containing items
	 */
	public void setItems(String[] items) {
		this.items = Arrays.copyOf(items, items.length);
		notifyDataSetChanged();
	}

	/**
	 * set font for items
	 *
	 * @param fonts font array
	 */
	public void setFonts(Typeface[] fonts) {
		this.fonts = Arrays.copyOf(fonts, fonts.length);
	}
}