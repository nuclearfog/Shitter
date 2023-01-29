package org.nuclearfog.twidda.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * Adapter used to show a list of social networks for the selector
 *
 * @author nuclearfog
 */
public class NetworkAdapter extends BaseAdapter {

	/**
	 * ID for Twitter selection
	 */
	public static final int ID_TWITTER = 10;

	/**
	 * ID for Mastodon selection
	 */
	public static final int ID_MASTODON = 20;

	/**
	 * social network icons
	 */
	private static final int[] ICONS = {R.drawable.mastodon, R.drawable.twitter};

	/**
	 * social network names
	 */
	private static final int[] STRINGS = {R.string.mastodon, R.string.twitter};

	/**
	 * social network IDs
	 */
	private static final int[] IDS = {ID_MASTODON, ID_TWITTER};

	private GlobalSettings settings;

	/**
	 *
	 */
	public NetworkAdapter(Context context) {
		settings = GlobalSettings.getInstance(context);
	}


	@Override
	public int getCount() {
		return ICONS.length;
	}


	@Override
	public Object getItem(int position) {
		return null;
	}


	@Override
	public long getItemId(int position) {
		return IDS[position];
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textItem;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(R.layout.item_dropdown, parent, false);
		}
		textItem = convertView.findViewById(R.id.dropdown_textitem);
		textItem.setText(STRINGS[position]);
		textItem.setCompoundDrawablesWithIntrinsicBounds(ICONS[position], 0, 0, 0);
		textItem.setTextColor(settings.getFontColor());
		textItem.setBackgroundColor(settings.getCardColor());
		AppStyles.setDrawableColor(textItem, settings.getIconColor());
		convertView.setBackgroundColor(settings.getBackgroundColor());
		return convertView;
	}
}