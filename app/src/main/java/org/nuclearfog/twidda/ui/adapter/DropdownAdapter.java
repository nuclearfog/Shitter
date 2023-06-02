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


public class DropdownAdapter extends BaseAdapter {

	private GlobalSettings settings;
	private Context context;

	private String[] items = {};
	private Typeface[] fonts = {};


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


	public void addItem(String item) {
		this.items = new String[] {item};
		notifyDataSetChanged();
	}


	public void addItems(@ArrayRes int arrayRes) {
		TypedArray tArray = context.getResources().obtainTypedArray(arrayRes);
		items = new String[tArray.length()];
		for (int i = 0; i < tArray.length(); i++) {
			items[i] = tArray.getString(i);
		}
		tArray.recycle();
		notifyDataSetChanged();
	}


	public void addItems(String[] items) {
		this.items = Arrays.copyOf(items, items.length);
		notifyDataSetChanged();
	}


	public void addFonts(Typeface[] fonts) {
		this.fonts = Arrays.copyOf(fonts, fonts.length);
	}
}