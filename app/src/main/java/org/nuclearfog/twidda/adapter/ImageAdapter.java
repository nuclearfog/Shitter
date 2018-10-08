package org.nuclearfog.twidda.adapter;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageHolder> {

    private OnImageClickListener l;
    private List<Bitmap> images;

    public ImageAdapter(OnImageClickListener l) {
        images = new ArrayList<>();
        this.l = l;
    }


    public void addImage(Bitmap image) {
        images.add(image);
    }


    @Override
    public int getItemCount() {
        return images.size();
    }


    @NonNull
    @Override
    public ImageAdapter.ImageHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        return new ImageHolder(imageView);
    }


    @Override
    public void onBindViewHolder(@NonNull final ImageAdapter.ImageHolder vh, int index) {
        final Bitmap image = images.get(index);
        float ratio = image.getHeight() / 256.0f;
        int destWidth = (int) (image.getWidth() / ratio);
        Bitmap result = Bitmap.createScaledBitmap(image, destWidth, 256, false);

        vh.item.setImageBitmap(result);
        vh.item.setBackgroundColor(0xffffffff);
        vh.item.setPadding(1, 1, 1, 1);
        vh.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                l.onImageClick(image);
            }
        });
    }


    public interface OnImageClickListener {
        void onImageClick(Bitmap image);
    }

    class ImageHolder extends ViewHolder {
        final ImageView item;

        ImageHolder(ImageView item) {
            super(item);
            this.item = item;
        }
    }
}