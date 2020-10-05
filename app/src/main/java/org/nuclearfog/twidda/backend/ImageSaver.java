package org.nuclearfog.twidda.backend;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.nuclearfog.twidda.activity.MediaViewer;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * this class is for saving images into storage
 */
public class ImageSaver extends AsyncTask<Object, Void, ImageSaver.ImageStat> {

    public enum ImageStat {
        IMAGE_SAVE_SUCCESS,
        IMAGE_SAVE_FAILED,
        IMAGE_DUPLICATE
    }

    private WeakReference<MediaViewer> callback;
    private File imagePath;


    public ImageSaver(MediaViewer activity) {
        this.callback = new WeakReference<>(activity);
        // path where images to save
        imagePath = activity.getExternalFilesDir(DIRECTORY_PICTURES);
        if (imagePath == null) {
            imagePath = activity.getFilesDir();
        }
    }


    @Override
    protected ImageStat doInBackground(Object[] data) {
        try {
            String link = (String) data[0];
            Bitmap image = (Bitmap) data[1];
            link = link.substring(link.lastIndexOf('/') + 1);
            String name = "shitter_" + link;
            File imageFile = new File(imagePath, name);
            if (imageFile.exists())
                return ImageStat.IMAGE_DUPLICATE;
            FileOutputStream imageWrite = new FileOutputStream(imageFile);
            image.compress(Bitmap.CompressFormat.JPEG, 90, imageWrite);
            imageWrite.flush();
            imageWrite.close();
            return ImageStat.IMAGE_SAVE_SUCCESS;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return ImageStat.IMAGE_SAVE_FAILED;
    }


    @Override
    protected void onPostExecute(ImageStat status) {
        if (callback.get() != null) {
            callback.get().onImageSaved(status);
        }
    }
}