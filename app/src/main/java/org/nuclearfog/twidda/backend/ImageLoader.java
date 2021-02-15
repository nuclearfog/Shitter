package org.nuclearfog.twidda.backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.MediaViewer;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.holder.ImageHolder;

import java.lang.ref.WeakReference;

/**
 * Background task to load images from twitter and storage
 *
 * @author nuclearfog
 * @see MediaViewer
 */
public class ImageLoader extends AsyncTask<String, ImageHolder, Boolean> {

    @Nullable
    private EngineException err;
    private TwitterEngine mTwitter;
    private WeakReference<MediaViewer> callback;


    /**
     * initialize image loader
     *
     * @param callback Activity context
     */
    public ImageLoader(@NonNull MediaViewer callback) {
        super();
        this.callback = new WeakReference<>(callback);
        mTwitter = TwitterEngine.getInstance(callback);
    }


    @Override
    protected Boolean doInBackground(String[] links) {
        try {
            for (String link : links) {
                Bitmap image;
                if (link.startsWith("https://")) {
                    image = mTwitter.getImage(link);
                } else {
                    image = BitmapFactory.decodeFile(link);
                }
                if (image != null) {
                    ImageHolder images = new ImageHolder(image);
                    publishProgress(images);
                }
            }
            return true;
        } catch (EngineException err) {
            this.err = err;
        } catch (Exception exception) {
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