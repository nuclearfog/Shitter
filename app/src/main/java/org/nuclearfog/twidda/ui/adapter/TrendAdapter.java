package org.nuclearfog.twidda.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.ui.adapter.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.holder.TrendHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter implementation to show trends
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.fragments.TrendFragment
 */
public class TrendAdapter extends Adapter<ViewHolder> implements OnHolderClickListener {

	/**
	 * trend limit
	 */
	private static final int INIT_COUNT = 50;

	private TrendClickListener itemClickListener;
	private GlobalSettings settings;

	private List<Trend> trends;

	/**
	 * @param itemClickListener Listener for item click
	 */
	public TrendAdapter(GlobalSettings settings, TrendClickListener itemClickListener) {
		this.itemClickListener = itemClickListener;
		this.settings = settings;
		trends = new ArrayList<>(INIT_COUNT);
	}


	@Override
	public int getItemCount() {
		return trends.size();
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new TrendHolder(parent, settings, this);
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder vh, int index) {
		TrendHolder holder = (TrendHolder) vh;
		Trend trend = trends.get(index);
		holder.setContent(trend, index);
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		itemClickListener.onTrendClick(trends.get(position));
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		return false;
	}

	/**
	 * get adapter items
	 *
	 * @return array of items
	 */
	public Trend[] getItems() {
		return trends.toArray(new Trend[0]);
	}

	/**
	 * replace data from list
	 *
	 * @param items list of trend items
	 */
	public void replaceItems(@NonNull List<Trend> items) {
		trends.clear();
		trends.addAll(items);
		notifyDataSetChanged();
	}

	/**
	 * replace data from list
	 *
	 * @param items array of trend items
	 */
	public void replaceItems(Trend[] items) {
		trends.clear();
		trends.addAll(Arrays.asList(items));
		notifyDataSetChanged();
	}

	/**
	 * clear adapter data
	 */
	public void clear() {
		trends.clear();
		notifyDataSetChanged();
	}

	/**
	 * check if adapter is empty
	 *
	 * @return true if adapter is empty
	 */
	public boolean isEmpty() {
		return trends.isEmpty();
	}

	/**
	 * Listener for trend list
	 */
	public interface TrendClickListener {

		/**
		 * called when trend item is clicked
		 *
		 * @param trend trend name
		 */
		void onTrendClick(Trend trend);
	}
}