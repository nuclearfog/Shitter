package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.ImageLoader;
import org.nuclearfog.twidda.backend.async.ImageLoader.ImageParameter;
import org.nuclearfog.twidda.backend.async.ImageLoader.ImageResult;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.views.AnimatedImageView;
import org.nuclearfog.twidda.ui.views.DescriptionView;
import org.nuclearfog.zoomview.ZoomView;

import java.io.File;

/**
 * Activity to show online and local images
 *
 * @author nuclearfog
 */
public class ImageViewer extends MediaActivity implements AsyncCallback<ImageResult> {

	/**
	 * indicates a default image (jpg, png, etc.)
	 */
	public static final int IMAGE_DEFAULT = 1;

	/**
	 * indicates an animated image (gif)
	 */
	public static final int IMAGE_GIF = 2;

	/**
	 * key to add URI of the image (online or local)
	 * value type is {@link Uri}
	 */
	public static final String LINK = "image-uri";

	/**
	 * key to set image format (image or gif)
	 * value type is Integer {@link #IMAGE_DEFAULT,#IMAGE_GIF}
	 */
	public static final String TYPE = "image-type";

	/**
	 * key to set image description
	 * value type is String
	 */
	public static final String DESCRIPTION = "image-description";

	/**
	 * name of the cache folder where online images will be stored
	 * after the end of this activity this folder will be cleared
	 */
	private static final String CACHE_FOLDER = "imagecache";

	private ZoomView zoomImage;
	private AnimatedImageView gifImage;
	private ProgressBar loadingCircle;

	@Nullable
	private Uri cacheUri;
	@Nullable
	private ImageLoader imageAsync;
	private GlobalSettings settings;
	private File cacheFolder;
	private boolean enableSave = false;
	private int mode = 0;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_image);
		Toolbar toolbar = findViewById(R.id.page_image_toolbar);
		DescriptionView descriptionView = findViewById(R.id.page_image_description);
		loadingCircle = findViewById(R.id.page_image_progress);
		zoomImage = findViewById(R.id.page_image_viewer);
		gifImage = findViewById(R.id.page_image_gif);

		settings = GlobalSettings.getInstance(this);
		AppStyles.setProgressColor(loadingCircle, settings.getHighlightColor());
		toolbar.setTitle("");
		toolbar.setBackgroundColor(settings.getBackgroundColor());
		setSupportActionBar(toolbar);

		cacheFolder = new File(getExternalCacheDir(), ImageViewer.CACHE_FOLDER);
		cacheFolder.mkdirs();

		Uri data = getIntent().getParcelableExtra(LINK);
		mode = getIntent().getIntExtra(TYPE, IMAGE_DEFAULT);
		String description = getIntent().getStringExtra(DESCRIPTION);
		boolean isLocalFile = !data.getScheme().startsWith("http");

		switch (mode) {
			case IMAGE_DEFAULT:
				zoomImage.setVisibility(View.VISIBLE);
				gifImage.setVisibility(View.INVISIBLE);
				if (isLocalFile) {
					zoomImage.setImageURI(data);
				}
				break;

			case IMAGE_GIF:
				zoomImage.setVisibility(View.INVISIBLE);
				gifImage.setVisibility(View.VISIBLE);
				if (isLocalFile) {
					gifImage.setImageURI(data);
				}
				break;
		}
		if (!isLocalFile) {
			ImageParameter request = new ImageParameter(data, cacheFolder);
			imageAsync = new ImageLoader(this);
			imageAsync.execute(request, this);
			enableSave = true;
		} else {
			loadingCircle.setVisibility(View.INVISIBLE);
			toolbar.setVisibility(View.GONE);
		}
		if (description != null && !description.trim().isEmpty()) {
			descriptionView.setDescription(description);
		} else {
			descriptionView.setVisibility(View.INVISIBLE);
		}
	}


	@Override
	protected void onDestroy() {
		if (imageAsync != null)
			imageAsync.cancel();
		clearCache();
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.image, menu);
		AppStyles.setMenuIconColor(menu, settings.getIconColor());
		menu.findItem(R.id.menu_image_save).setVisible(enableSave);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.menu_image_save) {
			if (cacheUri != null) {
				storeImage(cacheUri);
				return true;
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


	@Override
	public void onResult(@NonNull ImageResult result) {
		if (result.uri != null) {
			loadingCircle.setVisibility(View.INVISIBLE);
			cacheUri = result.uri;
			switch (mode) {
				case IMAGE_DEFAULT:
					zoomImage.reset();
					zoomImage.setImageURI(cacheUri);
					break;

				case IMAGE_GIF:
					gifImage.setImageURI(cacheUri);
					break;
			}
		} else {
			String message = ErrorHandler.getErrorMessage(this, result.exception);
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			finish();
		}
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