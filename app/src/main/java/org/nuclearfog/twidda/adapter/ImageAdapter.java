package org.nuclearfog.twidda.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.adapter.holder.ImageHolder;
import org.nuclearfog.twidda.adapter.holder.OnHolderClickListener;
import org.nuclearfog.twidda.adapter.holder.PlaceHolder;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter implementation to show image previews
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.activities.ImageViewer
 */
public class ImageAdapter extends Adapter<ViewHolder> implements OnHolderClickListener {

	/**
	 * View type for an image item
	 */
	private static final int ITEM_IMAGE = 0;

	/**
	 * View type for a circle view
	 */
	private static final int ITEM_PLACEHOLDER = 1;

	private OnImageClickListener itemClickListener;
	private GlobalSettings settings;

	private List<Uri> imageLinks;
	private boolean enableSaveButton;
	private boolean loading;

	/**
	 * @param itemClickListener click listener
	 */
	public ImageAdapter(Context context, OnImageClickListener itemClickListener) {
		imageLinks = new ArrayList<>(5);
		enableSaveButton = true;
		loading = false;
		this.itemClickListener = itemClickListener;
		this.settings = GlobalSettings.getInstance(context);
	}


	@Override
	public int getItemViewType(int position) {
		if (loading && position == imageLinks.size())
			return ITEM_PLACEHOLDER;
		return ITEM_IMAGE;
	}


	@Override
	public int getItemCount() {
		if (loading)
			return imageLinks.size() + 1;
		return imageLinks.size();
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
		if (viewType == ITEM_IMAGE) {
			ImageHolder item = new ImageHolder(parent, settings);
			item.setOnImageClickListener(this);
			if (enableSaveButton)
				item.enableSaveButton();
			return item;
		}
		return new PlaceHolder(parent, settings, true);
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder vh, int index) {
		if (vh instanceof ImageHolder) {
			ImageHolder item = (ImageHolder) vh;
			item.setImageUri(imageLinks.get(index));
		}
	}


	@Override
	public void onItemClick(int position, int type) {
		switch (type) {
			case OnHolderClickListener.IMAGE_CLICK:
				itemClickListener.onImageClick(imageLinks.get(position));
				break;

			case OnHolderClickListener.IMAGE_SAVE:
				itemClickListener.onImageSave(imageLinks.get(position));
				break;
		}
	}


	@Override
	public boolean onPlaceholderClick(int position) {
		return false;
	}

	/**
	 * replace all image links
	 *
	 * @param newLinks list of image links
	 */
	public void replaceItems(List<Uri> newLinks) {
		imageLinks.clear();
		imageLinks.addAll(newLinks);
		notifyDataSetChanged();
	}

	/**
	 * add new image at the last position
	 *
	 * @param uri Uri of the image
	 */
	public void addItem(@NonNull Uri uri) {
		int imagePos = imageLinks.size();
		if (imagePos == 0)
			loading = true;
		imageLinks.add(uri);
		notifyItemInserted(imagePos);
	}

	/**
	 * disable placeholder view
	 */
	public void disableLoading() {
		loading = false;
		int progressViewPos = imageLinks.size();
		notifyItemRemoved(progressViewPos);
	}

	/**
	 * disable save button on images
	 */
	public void disableSaveButton() {
		enableSaveButton = false;
	}

	/**
	 * check if image adapter is empty
	 *
	 * @return true if there isn't any image
	 */
	public boolean isEmpty() {
		return imageLinks.isEmpty();
	}

	/**
	 * click listener for image items
	 */
	public interface OnImageClickListener {

		/**
		 * called to select an image
		 *
		 * @param uri selected image uri
		 */
		void onImageClick(Uri uri);

		/**
		 * called to save image to storage
		 */
		void onImageSave(Uri uri);
	}
}