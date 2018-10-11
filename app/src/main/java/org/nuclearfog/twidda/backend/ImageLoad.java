package org.nuclearfog.twidda.backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

public class ImageLoad extends AsyncTask<String, Bitmap, Boolean> {

    private WeakReference<ImageDetail> ui;
    private ImageAdapter imageAdapter;

    public ImageLoad(ImageDetail context) {
        ui = new WeakReference<>(context);
        RecyclerView imageList = context.findViewById(R.id.image_list);
        imageAdapter = (ImageAdapter) imageList.getAdapter();
    }


    @Override
    protected Boolean doInBackground(String... links) {
        try {
            for (String link : links) {
                Bitmap image;
                if (link.startsWith("/"))
                    image = BitmapFactory.decodeFile(link);
                else
                    image = BitmapFactory.decodeStream(new URL(link).openStream());
                publishProgress(image);
            }
        } catch (Exception err) {
            err.printStackTrace();
            Log.e("Image Popup", err.getMessage());
            return false;
        }
        return true;
    }


    @Override
    protected void onProgressUpdate(Bitmap... images) {
        if (ui.get() == null) return;

        ProgressBar progress = ui.get().findViewById(R.id.image_load);

        imageAdapter.addImage(images[0]);

        if (progress.getVisibility() == View.VISIBLE) {
            ui.get().setImage(images[0]);
            progress.setVisibility(View.INVISIBLE);
        } else {
            imageAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() == null) return;

        if (!success) {
            View progress = ui.get().findViewById(R.id.image_load);
            progress.setVisibility(View.INVISIBLE);
            Toast.makeText(ui.get(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
        }
    }
}