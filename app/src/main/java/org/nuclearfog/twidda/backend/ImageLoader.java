package org.nuclearfog.twidda.backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.ImageAdapter;
import org.nuclearfog.twidda.window.ImageDetail;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;


public class ImageLoader extends AsyncTask<String, Void, Boolean> {

    private WeakReference<ImageDetail> ui;
    private ImageAdapter imageAdapter;
    private Bitmap[] images;


    public ImageLoader(@NonNull ImageDetail context) {
        ui = new WeakReference<>(context);
        RecyclerView imageList = context.findViewById(R.id.image_list);
        imageAdapter = (ImageAdapter) imageList.getAdapter();
    }


    @Override
    protected Boolean doInBackground(String... links) {
        try {
            int i = 0;
            images = new Bitmap[links.length];

            for (String link : links) {
                if (link.startsWith("/"))
                    images[i++] = BitmapFactory.decodeFile(link);
                else {
                    URL u = new URL(link);
                    InputStream stream = u.openStream();
                    images[i++] = BitmapFactory.decodeStream(stream);
                }
            }
        } catch (Exception err) {
            if (err.getMessage() != null)
                Log.e("Image Popup", err.getMessage());
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() == null) return;

        ProgressBar progress = ui.get().findViewById(R.id.image_load);
        progress.setVisibility(View.INVISIBLE);

        if (success) {
            ui.get().setImage(images[0]);
            imageAdapter.setImages(images);
            imageAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(ui.get(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
            ui.get().finish();
        }
    }
}