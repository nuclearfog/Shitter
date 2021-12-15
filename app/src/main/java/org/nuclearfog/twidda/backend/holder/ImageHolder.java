package org.nuclearfog.twidda.backend.holder;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

/**
 * Container class for Bitmap images and previews
 *
 * @author nuclearfog
 */
public class ImageHolder {

    /**
     * maximum height of the smallest preview in pixels
     */
    private static final float PREVIEW_HEIGHT = 320.0f;

    /**
     * maximum height of the image preview in pixels
     */
    private static final float REDUCED_HEIGHT = 1200.0f;

    /**
     * preview image bitmap
     */
    public final Bitmap preview;

    /**
     * downscaled image bitmap
     */
    public final Bitmap reducedImage;

    /**
     * full image bitmap
     */
    public final Bitmap fullImage;

    /**
     * @param fullImage Full size image
     */
    public ImageHolder(@NonNull Bitmap fullImage) {
        this.fullImage = fullImage;

        float reducedRatio = fullImage.getHeight() / REDUCED_HEIGHT;
        float previewRatio = fullImage.getHeight() / PREVIEW_HEIGHT;

        if (reducedRatio > 1.0f) {
            int height = (int) REDUCED_HEIGHT;
            int width = (int) (fullImage.getWidth() / reducedRatio);
            reducedImage = Bitmap.createScaledBitmap(fullImage, width, height, false);
        } else {
            reducedImage = fullImage;
        }
        if (previewRatio > 1.0f) {
            int height = (int) PREVIEW_HEIGHT;
            int width = (int) (fullImage.getWidth() / previewRatio);
            preview = Bitmap.createScaledBitmap(fullImage, width, height, false);
        } else {
            preview = fullImage;
        }
    }
}