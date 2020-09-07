package org.nuclearfog.twidda.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.holder.ImageHolder;

import java.util.LinkedList;
import java.util.List;

import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * Adapter class for image previews
 */
public class ImageAdapter extends Adapter<ViewHolder> {

    private static final int PICTURE = 0;
    private static final int LOADING = 1;

    private OnImageClickListener itemClickListener;

    private List<ImageHolder> images;
    private boolean loading = false;
    private boolean saveImg = true;

    /**
     * Create an adapter for image previews
     *
     * @param itemClickListener Click listener for images
     */
    public ImageAdapter(OnImageClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        images = new LinkedList<>();
    }


    @MainThread
    public void addLast(@NonNull ImageHolder imageItem) {
        int imagePos = images.size();
        if (imagePos == 0)
            loading = true;
        images.add(imageItem);
        notifyItemInserted(imagePos);
    }


    @MainThread
    public void disableLoading() {
        loading = false;
        int circlePos = images.size();
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
        return images.isEmpty();
    }


    @Override
    public int getItemViewType(int position) {
        if (loading && position == images.size())
            return LOADING;
        return PICTURE;
    }


    @Override
    public int getItemCount() {
        if (loading)
            return images.size() + 1;
        return images.size();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == PICTURE) {
            View view = inflater.inflate(R.layout.item_image, parent, false);
            final ImageItem item = new ImageItem(view);
            item.preview.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = item.getAdapterPosition();
                    if (pos != NO_POSITION) {
                        Bitmap img = images.get(pos).getMiddleSize();
                        itemClickListener.onImageClick(img);
                    }
                }
            });
            if (saveImg) {
                item.saveButton.setVisibility(VISIBLE);
                item.saveButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = item.getAdapterPosition();
                        if (pos != NO_POSITION) {
                            Bitmap img = images.get(pos).getOriginalImage();
                            itemClickListener.onSaveClick(img);
                        }
                    }
                });
            }
            return item;
        } else {
            View view = inflater.inflate(R.layout.item_image_load, parent, false);
            return new LoadItem(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int index) {
        if (vh instanceof ImageItem) {
            ImageItem item = (ImageItem) vh;
            Bitmap image = images.get(index).getSmallSize();
            item.preview.setImageBitmap(image);
        }
    }

    /**
     * Holder for image
     */
    class ImageItem extends ViewHolder {
        final ImageView preview;
        final ImageButton saveButton;

        ImageItem(View view) {
            super(view);
            preview = view.findViewById(R.id.item_image_preview);
            saveButton = view.findViewById(R.id.item_image_save);
        }
    }

    /**
     * Holder for progress circle
     */
    class LoadItem extends ViewHolder {

        LoadItem(View v) {
            super(v);
        }
    }


    public interface OnImageClickListener {
        /**
         * simple click on image_add
         *
         * @param image selected image_add bitmap
         */
        void onImageClick(Bitmap image);

        /**
         * long touch on image_add
         *
         * @param image selected image_add bitmap
         */
        void onSaveClick(Bitmap image);
    }
}