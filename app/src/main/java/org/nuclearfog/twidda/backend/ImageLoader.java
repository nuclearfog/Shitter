package org.nuclearfog.twidda.backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.activity.MediaViewer;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

/**
 * Background task to load images from twitter and storage
 * @see MediaViewer
 */
public class ImageLoader extends AsyncTask<String, Bitmap, Boolean> {

    private WeakReference<MediaViewer> callback;


    /**
     * initialize image loader
     *
     * @param callback Activity context
     */
    public ImageLoader(@NonNull MediaViewer callback) {
        this.callback = new WeakReference<>(callback);
    }


    @Override
    protected Boolean doInBackground(String[] links) {
        try {
            for (String link : links) {
                Bitmap image;
                if (link.startsWith("https://")) {
                    URL url = new URL(link);
                    InputStream stream = url.openStream();
                    image = BitmapFactory.decodeStream(stream);
                } else {
                    image = BitmapFactory.decodeFile(link);
                }
                if (image != null)
                    publishProgress(image);
            }
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }


    @Override
    protected void onProgressUpdate(Bitmap[] btm) {
        if (callback.get() != null)
            callback.get().setImage(btm[0]);
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (callback.get() != null) {
            if (success) {
                callback.get().onSuccess();
            } else {
                callback.get().onError();
            }
        }
    }
}