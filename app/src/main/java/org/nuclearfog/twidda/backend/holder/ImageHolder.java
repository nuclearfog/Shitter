package org.nuclearfog.twidda.backend.holder;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

/**
 * Container class for Bitmap images and previews
 *
 * @author nuclearfog
 */
public class ImageHolder {

    private Bitmap smallImage, middleImage, fullImage;

    /**
     * @param fullImage        Full size image
     * @param smallImageHeight height of the smallest image preview
     * @param middleImageWidth width of the image preview
     */
    public ImageHolder(@NonNull Bitmap fullImage, float smallImageHeight, float middleImageWidth) {
        this.fullImage = fullImage;

        float ratio = fullImage.getHeight() / smallImageHeight;
        int destWidth = (int) (fullImage.getWidth() / ratio);
        smallImage = Bitmap.createScaledBitmap(fullImage, destWidth, (int) smallImageHeight, false);

        if (middleImageWidth > 0 && fullImage.getWidth() > middleImageWidth) {
            ratio = fullImage.getWidth() / middleImageWidth;
            int destHeight = (int) (fullImage.getHeight() / ratio);
            middleImage = Bitmap.createScaledBitmap(fullImage, (int) middleImageWidth, destHeight, false);
        } else {
            middleImage = fullImage;
        }
    }

    /**
     * get small sized image
     *
     * @return Image Bitmap
     */
    public Bitmap getSmallSize() {
        return smallImage;
    }

    /**
     * get Middle sized image
     *
     * @return Image Bitmap
     */
    public Bitmap getMiddleSize() {
        return middleImage;
    }

    /**
     * get Original Image
     *
     * @return Image Bitmap
     */
    public Bitmap getOriginalImage() {
        return fullImage;
    }
}