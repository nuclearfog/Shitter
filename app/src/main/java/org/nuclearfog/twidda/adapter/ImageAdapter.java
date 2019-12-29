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

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import static android.widget.ListPopupWindow.MATCH_PARENT;
import static android.widget.ListPopupWindow.WRAP_CONTENT;


public class ImageAdapter extends Adapter<ImageAdapter.ImageHolder> {

    private static final int PICTURE = 0;
    private static final int LOADING = 1;

    private WeakReference<OnImageClickListener> itemClickListener;
    private List<Bitmap> images;
    private boolean loading;


    public ImageAdapter(OnImageClickListener l) {
        itemClickListener = new WeakReference<>(l);
        images = new LinkedList<>();
        loading = false;
    }


    @MainThread
    public void addLast(@NonNull Bitmap image) {
        int imagePos = images.size();
        if (imagePos == 0)
            loading = true;
        images.add(image);
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
    public ImageAdapter.ImageHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        if (viewType == LOADING) {
            ProgressBar circle = new ProgressBar(parent.getContext());
            LayoutParams param = new LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            circle.setLayoutParams(param);
            return new ImageHolder(circle);
        } else {
            return new ImageHolder(new ImageView(parent.getContext()));
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ImageHolder vh, int index) {
        if (vh.view instanceof ImageView) {
            final Bitmap image = images.get(index);
            ImageView imageView = (ImageView) vh.view;
            imageView.setImageBitmap(downscale(image));
            imageView.setBackgroundColor(0xffffffff);
            imageView.setPadding(1, 1, 1, 1);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener.get() != null)
                        itemClickListener.get().onImageClick(image);
                }
            });
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (itemClickListener.get() != null) {
                        itemClickListener.get().onImageTouch(image);
                        return true;
                    }
                    return false;
                }
            });
        }
    }


    private Bitmap downscale(Bitmap image) {
        float ratio = image.getHeight() / 256.0f;
        int destWidth = (int) (image.getWidth() / ratio);
        return Bitmap.createScaledBitmap(image, destWidth, 256, false);
    }


    class ImageHolder extends ViewHolder {
        final View view;

        ImageHolder(View view) {
            super(view);
            this.view = view;
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