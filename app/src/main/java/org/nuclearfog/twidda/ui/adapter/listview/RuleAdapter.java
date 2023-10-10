package org.nuclearfog.twidda.ui.adapter.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Rule;
import org.nuclearfog.twidda.model.lists.Rules;

import java.util.Set;
import java.util.TreeSet;

/**
 * A {@link android.widget.ListView} adapter used to show instance rules and provide function to select rule items and their IDs
 *
 * @author nuclearfog
 */
public class RuleAdapter extends BaseAdapter {

	private GlobalSettings settings;

	private Rules items = new Rules();
	private Set<Long> selection = new TreeSet<>();

	/**
	 *
	 */
	public RuleAdapter(Context context) {
		settings = GlobalSettings.get(context);
	}


	@Override
	public int getCount() {
		return items.size();
	}


	@Override
	public Object getItem(int position) {
		return items.get(position);
	}


	@Override
	public long getItemId(int position) {
		return items.get(position).getId();
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView description;
		ImageView button;
		final Rule item = items.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule, parent, false);
			description = convertView.findViewById(R.id.item_rule_description);
			button = convertView.findViewById(R.id.item_rule_select);
			button.setColorFilter(settings.getIconColor());
			description.setTextColor(settings.getTextColor());
			description.setTypeface(settings.getTypeFace());
		} else {
			description = convertView.findViewById(R.id.item_rule_description);
			button = convertView.findViewById(R.id.item_rule_select);
		}

		description.setText(item.getDescription());
		if (selection.contains(item.getId())) {
			button.setImageResource(R.drawable.check);
		} else {
			button.setImageResource(R.drawable.circle);
		}
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selection.contains(item.getId())) {
					button.setImageResource(R.drawable.circle);
					selection.remove(item.getId());
				} else {
					button.setImageResource(R.drawable.check);
					selection.add(item.getId());
				}
			}
		});
		return convertView;
	}

	/**
	 * set adapter items
	 *
	 * @param rules adapter items to set
	 */
	public void setItems(Rules rules) {
		items.clear();
		items.addAll(rules);
		notifyDataSetChanged();
	}

	/**
	 * get user selected item IDs
	 *
	 * @return an array containing selected item IDs
	 */
	public long[] getSelectedIds() {
		int i = 0;
		long[] result = new long[selection.size()];
		for (Long select : selection) {
			result[i++] = select;
		}
		return result;
	}

	/**
	 * @return true if adapter doesn't contain any items
	 */
	public boolean isEmpty() {
		return items.isEmpty();
	}
}