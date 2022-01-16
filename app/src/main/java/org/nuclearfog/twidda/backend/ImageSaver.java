package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activities.MediaActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

/**
 * This AsyncTask class moves a cached image to the destiny folder
 *
 * @author nuclearfog
 * @see MediaActivity
 */
public class ImageSaver extends AsyncTask<Object, Void, Boolean> {


    private WeakReference<MediaActivity> callback;


    public ImageSaver(MediaActivity activity) {
        super();
        callback = new WeakReference<>(activity);
    }


    @Override
    protected Boolean doInBackground(Object... data) {
        try {
            if (data != null && data.length == 2) {
                if (data[0] instanceof InputStream && data[1] instanceof OutputStream) {
                    InputStream source = (InputStream) data[0];
                    OutputStream destiny = (OutputStream) data[1];

                    // copy file from cache to the destiny folder
                    int length;
                    byte[] buffer = new byte[4096];
                    while ((length = source.read(buffer)) > 0) {
                        destiny.write(buffer, 0, length);
                    }
                    source.close();
                    destiny.close();
                    return true;
                }
            }
        } catch (IOException err) {
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