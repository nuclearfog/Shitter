package org.nuclearfog.twidda.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.adapter.holder.Footer;
import org.nuclearfog.twidda.adapter.holder.ImageItem;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import com.squareup.picasso.Picasso;

/**
 * Adapter class for image previews
 *
 * @author nuclearfog
 */
public class ImageAdapter extends Adapter<ViewHolder> {

    /**
     * View type for an image item
     */
    private static final int PICTURE = 0;

    /**
     * View type for a circle view
     */
    private static final int LOADING = 1;

    private OnImageClickListener itemClickListener;

    private GlobalSettings settings;

    private Picasso picasso;

    private List<Uri> imageUri = new ArrayList<>(5);
    private boolean loading = false;
    private boolean saveImg = true;

    /**
     * @param itemClickListener click listener
     */
    public ImageAdapter(Context context, OnImageClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        this.settings = GlobalSettings.getInstance(context);
        picasso = PicassoBuilder.get(context);
    }

    /**
     * add new image at the last position
     *
     * @param uri Uri of the image
     */
    @MainThread
    public void addLast(@NonNull Uri uri) {
        int imagePos = imageUri.size();
        if (imagePos == 0)
            loading = true;
        imageUri.add(uri);
        notifyItemInserted(imagePos);
    }

    /**
     * disable placeholder view
     */
    @MainThread
    public void disableLoading() {
        loading = false;
        int circlePos = imageUri.size();
        notifyItemRemoved(circlePos);
    }

    /**
     * disable save button on images
     */
    public void disableSaveButton() {
        saveImg = false;
    }

    /**
     * check if image adapter is empty
     *
     * @return true if there isn't any image
     */
    public boolean isEmpty() {
        return imageUri.isEmpty();
    }


    @Override
    public int getItemViewType(int position) {
        if (loading && position == imageUri.size())
            return LOADING;
        return PICTURE;
    }


    @Override
    public int getItemCount() {
        if (loading)
            return imageUri.size() + 1;
        return imageUri.size();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        if (viewType == PICTURE) {
            final ImageItem item = new ImageItem(parent, settings);
            item.preview.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = item.getLayoutPosition();
                    if (pos != NO_POSITION) {
                        itemClickListener.onImageClick(imageUri.get(pos));
                    }
                }
            });
            if (saveImg) {
                item.saveButton.setVisibility(VISIBLE);
                item.saveButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = item.getLayoutPosition();
                        if (pos != NO_POSITION) {
                            itemClickListener.onImageSave(imageUri.get(pos));
                        }
                    }
                });
            }
            return item;
        } else {
            return new Footer(parent, settings, true);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int index) {
        if (vh instanceof ImageItem) {
            ImageItem item = (ImageItem) vh;
            Uri uri = imageUri.get(index);
            picasso.load(uri).into(item.preview);
        }
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