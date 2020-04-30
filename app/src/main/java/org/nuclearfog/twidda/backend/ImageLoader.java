package org.nuclearfog.twidda.backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.activity.MediaViewer;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

/**
 * Background task to load images from twitter and storage
 */
public class ImageLoader extends AsyncTask<String, Bitmap, Boolean> {

    public enum Action {
        ONLINE,
        STORAGE
    }

    private WeakReference<MediaViewer> ui;
    private Action action;


    /**
     * initialize image loader
     *
     * @param context Activity context
     * @param action  information from image location
     */
    public ImageLoader(@NonNull MediaViewer context, Action action) {
        ui = new WeakReference<>(context);
        this.action = action;
    }


    @Override
    protected Boolean doInBackground(String[] links) {
        try {
            switch (action) {
                case ONLINE:
                    for (String link : links) {
                        URL url = new URL(link);
                        InputStream stream = url.openStream();
                        Bitmap image = BitmapFactory.decodeStream(stream);
                        if (image != null)
                            publishProgress(image);
                        else
                            return false;
                    }
                    return true;

                case STORAGE:
                    for (String link : links) {
                        Bitmap image = BitmapFactory.decodeFile(link);
                        publishProgress(image);
                    }
                    return true;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }


    @Override
    protected void onProgressUpdate(Bitmap[] btm) {
        if (ui.get() != null)
            ui.get().setImage(btm[0]);
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() != null) {
            if (success) {
                ui.get().setImage(null);
            } else {
                Toast.makeText(ui.get(), R.string.error_image_download, Toast.LENGTH_SHORT).show();
                ui.get().finish();
            }
        }
    }
}