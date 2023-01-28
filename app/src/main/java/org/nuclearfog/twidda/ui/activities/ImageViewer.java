package org.nuclearfog.twidda.ui.activities;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.INVISIBLE;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.ImageLoader;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.zoomview.ZoomView;

import java.io.File;

/**
 * Activity to show online and local images
 *
 * @author nuclearfog
 */
public class ImageViewer extends MediaActivity {

	/**
	 * key to add URI of the image (online or local)
	 * value type is {@link Uri}
	 */
	public static final String IMAGE_URI = "image-uri";

	/**
	 * name of the cache folder where online images will be stored
	 * after the end of this activity this folder will be cleared
	 */
	private static final String CACHE_FOLDER = "imagecache";

	private static final int TOOLBAR_COLOR_MASK = 0xcfa0a0a0;

	private ZoomView zoomImage;
	private ProgressBar loadingCircle;

	@Nullable
	private Uri cacheUri;
	@Nullable
	private ImageLoader imageAsync;
	private GlobalSettings settings;
	private File cacheFolder;
	private boolean enableSave = false;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_image);
		Toolbar toolbar = findViewById(R.id.page_image_toolbar);
		loadingCircle = findViewById(R.id.page_image_progress);
		zoomImage = findViewById(R.id.page_image_viewer);

		settings = GlobalSettings.getInstance(this);
		AppStyles.setProgressColor(loadingCircle, settings.getHighlightColor());
		toolbar.setTitle("");
		toolbar.setBackgroundColor(settings.getBackgroundColor() & TOOLBAR_COLOR_MASK);
		setSupportActionBar(toolbar);

		cacheFolder = new File(getExternalCacheDir(), ImageViewer.CACHE_FOLDER);
		cacheFolder.mkdirs();

		Parcelable data = getIntent().getParcelableExtra(IMAGE_URI);
		if (data instanceof Uri) {
			Uri uri = (Uri) data;
			if (uri.getScheme().startsWith("http")) {
				imageAsync = new ImageLoader(this, cacheFolder);
				imageAsync.execute(uri);
				enableSave = true;
			} else {
				zoomImage.setImageURI(uri);
				loadingCircle.setVisibility(INVISIBLE);
			}
		}
	}


	@Override
	protected void onDestroy() {
		if (imageAsync != null && imageAsync.getStatus() == RUNNING) {
			imageAsync.cancel(true);
			clearCache();
		}
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.image, menu);
		AppStyles.setMenuIconColor(menu, settings.getIconColor());
		menu.findItem(R.id.menu_image_save).setVisible(enableSave);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.menu_image_save) {
			if (cacheUri != null) {
				storeImage(cacheUri);
			}
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onAttachLocation(@Nullable Location location) {
	}


	@Override
	protected void onMediaFetched(int resultType, @NonNull Uri uri) {
	}

	/**
	 * Called from {@link ImageLoader} when all images are downloaded successfully
	 *
	 * @param uri Uri of the cached image file
	 */
	public void onSuccess(@NonNull Uri uri) {
		cacheUri = uri;
		zoomImage.reset();
		zoomImage.setImageURI(uri);
		loadingCircle.setVisibility(INVISIBLE);
	}

	/**
	 * Called from {@link ImageLoader} when an error occurs
	 */
	public void onError(@Nullable ConnectionException err) {
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