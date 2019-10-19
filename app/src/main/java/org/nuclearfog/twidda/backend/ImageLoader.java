package org.nuclearfog.twidda.backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.ImageAdapter;
import org.nuclearfog.twidda.window.MediaViewer;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;


public class ImageLoader extends AsyncTask<String, Bitmap, Boolean> {

    public enum Mode {
        ONLINE,
        STORAGE
    }

    private WeakReference<MediaViewer> ui;
    private ImageAdapter imageAdapter;
    private Mode mode;


    public ImageLoader(@NonNull MediaViewer context, Mode mode) {
        ui = new WeakReference<>(context);
        imageAdapter = context.getAdapter();
        this.mode = mode;
    }


    @Override
    protected Boolean doInBackground(String[] links) {
        try {
            switch (mode) {
                case ONLINE:
                    for (String link : links) {
                        URL url = new URL(link);
                        InputStream stream = url.openStream();
                        Bitmap image = BitmapFactory.decodeStream(stream);
                        publishProgress(image);
                    }
                    break;

                case STORAGE:
                    for (String link : links) {
                        Bitmap image = BitmapFactory.decodeFile(link);
                        publishProgress(image);
                    }
                    break;
            }
        } catch (Exception err) {
            err.printStackTrace();
            return false;
        }
        return true;
    }


    @Override
    protected void onProgressUpdate(Bitmap[] btm) {
        Bitmap image = btm[0];
        if (ui.get() != null && image != null) {
            if (imageAdapter.getItemCount() == 2)
                ui.get().disableProgressbar();
            imageAdapter.addLast(btm[0]);
        }
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() != null) {
            if (success) {
                imageAdapter.disableLoading();
            } else {
                Toast.makeText(ui.get(), R.string.image_download_fail, Toast.LENGTH_SHORT).show();
                ui.get().finish();
            }
        }
    }
}