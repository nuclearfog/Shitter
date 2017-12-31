package org.nuclearfog.twidda.Backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;


public class ImageDownloader extends AsyncTask<String, Void, Bitmap>
{
    private ImageView imgView;

    public ImageDownloader(ImageView imgView){
        this.imgView = imgView;
    }

    @Override
    protected Bitmap doInBackground(String... links){
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
    protected void onPostExecute(Bitmap img){
        imgView.setImageBitmap(img);
    }
}
