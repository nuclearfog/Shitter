package org.nuclearfog.twidda.backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import org.nuclearfog.twidda.R;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

public class ImageDownloader extends AsyncTask<String, Void, Bitmap>
{
    private final WeakReference<ImageView> imgReference ;

    public ImageDownloader(ImageView imgView) {
        imgReference = new WeakReference<>(imgView);
    }

    @Override
    protected Bitmap doInBackground(String... links) {
        Bitmap picture = null;
        try {
            InputStream iStream = new URL(links[0]).openStream();
            picture = BitmapFactory.decodeStream(iStream);
        } catch(Exception err) {
            err.printStackTrace();
        }
        return picture;
    }

    @Override
    protected void onPostExecute(Bitmap img) {
        ImageView pb = imgReference.get();
        if(pb != null) {
            if(img != null)
                pb.setImageBitmap(img);
            else
                pb.setImageResource(R.mipmap.pb);
        }
    }

    @Override
    protected void onCancelled(){
        super.onCancelled();
    }
}
