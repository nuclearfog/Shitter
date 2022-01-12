package org.nuclearfog.twidda.backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activities.MediaViewer;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.holder.ImageHolder;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;

import java.lang.ref.WeakReference;

/**
 * Background task to load images from twitter and storage
 *
 * @author nuclearfog
 * @see MediaViewer
 */
public class ImageLoader extends AsyncTask<String, ImageHolder, Boolean> {

    @Nullable
    private ErrorHandler.TwitterError err;
    private Twitter twitter;
    private WeakReference<MediaViewer> callback;


    /**
     * initialize image loader
     *
     * @param activity Activity context
     */
    public ImageLoader(@NonNull MediaViewer activity) {
        super();
        callback = new WeakReference<>(activity);
        twitter = Twitter.get(activity);
    }


    @Override
    protected Boolean doInBackground(String[] links) {
        try {
            for (String link : links) {
                Bitmap image = null;
                if (link.startsWith("https://")) { // fixme
                    //image = mTwitter.getImage(link);
                } else {
                    image = BitmapFactory.decodeFile(link);
                }
                if (image != null) {
                    ImageHolder images = new ImageHolder(image);
                    publishProgress(images);
                }
            }
            return true;
        } /*catch (EngineException err) {
            this.err = err;
        }*/ catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }


    @Override
    protected void onProgressUpdate(ImageHolder[] images) {
        if (callback.get() != null) {
            callback.get().setImage(images[0]);
        }
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (callback.get() != null) {
            if (success) {
                callback.get().onSuccess();
            } else {
                callback.get().onError(err);
            }
        }
    }
}