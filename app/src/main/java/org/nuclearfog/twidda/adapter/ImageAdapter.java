package org.nuclearfog.twidda.adapter;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.backend.holder.ImageHolder;

import java.util.LinkedList;
import java.util.List;

import static android.widget.ListPopupWindow.MATCH_PARENT;
import static android.widget.ListPopupWindow.WRAP_CONTENT;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;


public class ImageAdapter extends Adapter<ViewHolder> {

    private static final int PICTURE = 0;
    private static final int LOADING = 1;

    private OnImageClickListener itemClickListener;

    private List<ImageHolder> images;
    private boolean loading;


    public ImageAdapter(OnImageClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        images = new LinkedList<>();
        loading = false;
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
        if (viewType == PICTURE) {
            ImageView preview = new ImageView(parent.getContext());
            preview.setBackgroundColor(0xffffffff);
            preview.setPadding(1, 1, 1, 1);
            final ImageItem item = new ImageItem(preview);
            preview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = item.getAdapterPosition();
                    if (pos != NO_POSITION) {
                        Bitmap img = images.get(pos).getMiddleSize();
                        itemClickListener.onImageClick(img);
                    }
                }
            });
            preview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = item.getAdapterPosition();
                    if (pos != NO_POSITION) {
                        Bitmap img = images.get(pos).getOriginalImage();
                        itemClickListener.onImageTouch(img);
                    }
                    return true;
                }
            });
            return item;
        } else {
            ProgressBar circle = new ProgressBar(parent.getContext());
            LayoutParams param = new LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            circle.setLayoutParams(param);
            return new LoadItem(circle);
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

        ImageItem(ImageView preview) {
            super(preview);
            this.preview = preview;
        }
    }

    /**
     * Holder for progress circle
     */
    class LoadItem extends ViewHolder {
        final ProgressBar circle;

        LoadItem(ProgressBar circle) {
            super(circle);
            this.circle = circle;
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
        void onImageTouch(Bitmap image);
    }
}