package org.nuclearfog.twidda.backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.ImageAdapter;
import org.nuclearfog.twidda.window.MediaViewer;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;


public class ImageLoader extends AsyncTask<String, Void, Bitmap[]> {

    public enum Mode {
        ONLINE,
        STORAGE
    }

    private WeakReference<MediaViewer> ui;
    private ImageAdapter imageAdapter;
    private Mode mode;


    public ImageLoader(@NonNull MediaViewer context, Mode mode) {
        ui = new WeakReference<>(context);
        RecyclerView imageList = context.findViewById(R.id.image_list);
        imageAdapter = (ImageAdapter) imageList.getAdapter();
        this.mode = mode;
    }


    @Override
    protected Bitmap[] doInBackground(String[] links) {
        try {
            int i = 0;
            Bitmap[] images = new Bitmap[links.length];
            for (String link : links) {
                switch (mode) {
                    case ONLINE:
                        URL url = new URL(link);
                        InputStream stream = url.openStream();
                        images[i++] = BitmapFactory.decodeStream(stream);
                        break;

                    case STORAGE:
                        images[i++] = BitmapFactory.decodeFile(link);
                        break;
                }
            }
            return images;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(@Nullable Bitmap[] images) {
        if (ui.get() != null) {
            ProgressBar progress = ui.get().findViewById(R.id.image_load);
            progress.setVisibility(View.INVISIBLE);
            if (images != null && images.length > 0) {
                ui.get().setImage(images[0]);
                imageAdapter.setImages(images);
                imageAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(ui.get(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
                ui.get().finish();
            }
        }
    }
}