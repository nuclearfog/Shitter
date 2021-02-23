package org.nuclearfog.twidda.backend;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.nuclearfog.twidda.activity.MediaActivity;

import java.io.OutputStream;
import java.lang.ref.WeakReference;

/**
 * background task to save an image to the external storage
 *
 * @author nuclearfog
 * @see MediaActivity
 */
public class ImageSaver extends AsyncTask<Object, Void, Boolean> {

    /**
     * Quality of the saved jpeg images
     */
    private static final int JPEG_QUALITY = 90;

    private WeakReference<MediaActivity> callback;


    public ImageSaver(MediaActivity activity) {
        super();
        callback = new WeakReference<>(activity);
    }


    @Override
    protected Boolean doInBackground(Object... data) {
        try {
            if (data != null && data.length == 2) {
                if (data[0] instanceof Bitmap && data[1] instanceof OutputStream) {
                    Bitmap image = (Bitmap) data[0];
                    OutputStream fileStream = (OutputStream) data[1];
                    boolean imageSaved = image.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fileStream);
                    fileStream.close();
                    return imageSaved;
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (callback.get() != null) {
            if (success) {
                callback.get().onImageSaved();
            } else {
                callback.get().onError();
            }
        }
    }
}