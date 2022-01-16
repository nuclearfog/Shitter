package org.nuclearfog.twidda.backend;

import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activities.MediaViewer;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.api.holder.MediaStream;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.StringTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * This AsyncTask class downloads images to a local cache folder
 * and creates Uri of the images.
 *
 * @author nuclearfog
 * @see MediaViewer
 */
public class ImageLoader extends AsyncTask<Uri, Uri, Boolean> {

    @Nullable
    private ErrorHandler.TwitterError err;
    private Twitter twitter;
    private WeakReference<MediaViewer> callback;
    private File cache;

    /**
     * initialize image loader
     *
     * @param activity Activity context
     */
    public ImageLoader(@NonNull MediaViewer activity) {
        super();
        callback = new WeakReference<>(activity);
        twitter = Twitter.get(activity);
        // create cache folder if not exists
        cache = new File(activity.getExternalCacheDir(), MediaViewer.CACHE_FOLDER);
        cache.mkdirs();
    }


    @Override
    protected Boolean doInBackground(Uri[] links) {
        try {
            // download imaged to a local cache folder
            for (Uri link : links) {
                // get input stream
                MediaStream input = twitter.downloadImage(link.toString());
                InputStream stream = input.getStream();
                String mimeType = input.getMimeType();

                // create file
                String ext = '.' + mimeType.substring(mimeType.indexOf('/') + 1);
                File file = new File(cache, StringTools.getRandomString() + ext);
                file.createNewFile();

                // copy image to cache folder
                FileOutputStream os = new FileOutputStream(file);
                int length;
                byte[] buffer = new byte[4096];
                while ((length = stream.read(buffer)) > 0)
                    os.write(buffer, 0, length);
                input.close();
                os.close();

                // create a new uri
                publishProgress(Uri.fromFile(file));
            }
            return true;
        } catch (TwitterException err) {
            this.err = err;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return false;
    }


    @Override
    protected void onProgressUpdate(Uri[] uris) {
        if (callback.get() != null) {
            callback.get().setImage(uris[0]);
        }
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (callback.get() != null) {
            if (success) {
                callback.get().onSuccess();
            } else {
                callback.get().onError(err);
            }
        }
    }
}