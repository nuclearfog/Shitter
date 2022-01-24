package org.nuclearfog.twidda.activities;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.INVISIBLE;
import static androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.ImageAdapter;
import org.nuclearfog.twidda.adapter.ImageAdapter.OnImageClickListener;
import org.nuclearfog.twidda.backend.ImageLoader;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.zoomview.ZoomView;

import java.io.File;

/**
 * Activity to show online of local images
 *
 * @author nuclearfog
 */
public class ImageViewer extends MediaActivity implements OnImageClickListener {

    /**
     * key to add URI of the image (online or local)
     */
    public static final String IMAGE_URIS = "image-uri";

    /**
     * key to define where the images are located (online or local)
     */
    public static final String IMAGE_DOWNLOAD = "image-download";

    /**
     * name of the cache folder where online images will be stored
     * after the end of this activity this folder will be cleared
     */
    private static final String CACHE_FOLDER = "imagecache";

    @Nullable
    private ImageLoader imageAsync;
    private ImageAdapter adapter;
    private File cacheFolder;

    private ZoomView zoomImage;
    private ProgressBar loadingCircle;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_image);
        loadingCircle = findViewById(R.id.media_progress);
        zoomImage = findViewById(R.id.image_full);
        RecyclerView imageList = findViewById(R.id.image_list);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        AppStyles.setProgressColor(loadingCircle, settings.getHighlightColor());

        cacheFolder = new File(getExternalCacheDir(), ImageViewer.CACHE_FOLDER);
        cacheFolder.mkdirs();
        adapter = new ImageAdapter(getApplicationContext(), this);
        imageList.setLayoutManager(new LinearLayoutManager(this, HORIZONTAL, false));
        imageList.setAdapter(adapter);

        Parcelable[] links = getIntent().getParcelableArrayExtra(IMAGE_URIS);
        boolean online = getIntent().getBooleanExtra(IMAGE_DOWNLOAD, true);
        Uri[] uris = {null};
        if (links != null) {
            uris = new Uri[links.length];
            for (int i = 0; i < uris.length; i++) {
                uris[i] = (Uri) links[i];
            }
        }
        if (online) {
            imageAsync = new ImageLoader(this, cacheFolder);
            imageAsync.execute(uris);
        } else {
            adapter.addAll(uris);
            adapter.disableSaveButton();
            zoomImage.setImageURI(uris[0]);
            zoomImage.reset();
            loadingCircle.setVisibility(INVISIBLE);
        }
    }


    @Override
    protected void onDestroy() {
        if (imageAsync != null && imageAsync.getStatus() == RUNNING)
            imageAsync.cancel(true);
        clearCache();
        super.onDestroy();
    }


    @Override
    public void onImageClick(Uri uri) {
        zoomImage.reset();
        zoomImage.setImageURI(uri);
    }


    @Override
    public void onImageSave(Uri uri) {
        storeImage(uri);
    }


    @Override
    protected void onAttachLocation(@Nullable Location location) {
    }


    @Override
    protected void onMediaFetched(int resultType, @NonNull Uri uri) {
    }

    /**
     * set downloaded image into preview list
     *
     * @param imageUri Image Uri
     */
    public void setImage(Uri imageUri) {
        if (adapter.isEmpty()) {
            zoomImage.reset();
            zoomImage.setImageURI(imageUri);
            loadingCircle.setVisibility(INVISIBLE);
        }
        adapter.addLast(imageUri);
    }

    /**
     * Called from {@link ImageLoader} when all images are downloaded successfully
     */
    public void onSuccess() {
        adapter.disableLoading();
    }

    /**
     * Called from {@link ImageLoader} when an error occurs
     *
     * @param err Exception caught by {@link ImageLoader}
     */
    public void onError(ErrorHandler.TwitterError err) {
        ErrorHandler.handleFailure(getApplicationContext(), err);
        finish();
    }

    /**
     * clear the image cache
     */
    private void clearCache() {
        try {
            File[] files = cacheFolder.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    file.delete();
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}