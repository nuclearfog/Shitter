package org.nuclearfog.twidda.backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.ImageAdapter;
import org.nuclearfog.twidda.window.ImageDetail;

import java.lang.ref.WeakReference;
import java.net.URL;


public class ImageLoad extends AsyncTask<String, Void, Boolean> {

    private WeakReference<ImageDetail> ui;
    private ImageAdapter imageAdapter;
    private Bitmap images[];


    public ImageLoad(@NonNull ImageDetail context) {
        ui = new WeakReference<>(context);
        RecyclerView imageList = context.findViewById(R.id.image_list);
        imageAdapter = (ImageAdapter) imageList.getAdapter();
    }


    @Override
    protected Boolean doInBackground(String... links) {
        try {
            images = new Bitmap[links.length];

            for (int i = 0; i < links.length; i++) {
                String link = links[i];
                if (link.startsWith("/"))
                    images[i] = BitmapFactory.decodeFile(link);
                else
                    images[i] = BitmapFactory.decodeStream(new URL(link).openStream());
            }
        } catch (Exception err) {
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