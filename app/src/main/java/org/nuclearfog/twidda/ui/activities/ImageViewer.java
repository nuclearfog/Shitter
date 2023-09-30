package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.ImageDownloader;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.BlurHashDecoder;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.ui.dialogs.DescriptionDialog;
import org.nuclearfog.twidda.ui.dialogs.DescriptionDialog.DescriptionCallback;
import org.nuclearfog.twidda.ui.dialogs.MetaDialog;
import org.nuclearfog.twidda.ui.views.AnimatedImageView;
import org.nuclearfog.twidda.ui.views.DescriptionView;
import org.nuclearfog.zoomview.ZoomView;

import java.io.File;
import java.io.Serializable;

/**
 * Activity to show online and local images
 *
 * @author nuclearfog
 */
public class ImageViewer extends MediaActivity implements AsyncCallback<ImageDownloader.Result>, DescriptionCallback {

	/**
	 * activity result code indicates that {@link MediaStatus} data has been updated
	 */
	public static final int RETURN_MEDIA_STATUS_UPDATE = 0x5895;

	/**
	 * key to add media data (online or local)
	 * value type can be {@link Media} for online media, {@link MediaStatus} for local media or {@link Uri} for media links
	 */
	public static final String KEY_IMAGE_DATA = "image-data";

	/**
	 * name of the cache folder where online images will be stored
	 * after the end of this activity this folder will be cleared
	 */
	private static final String CACHE_FOLDER = "imagecache";

	private ZoomView zoomImage;
	private ProgressBar loadingCircle;
	private DescriptionView descriptionView;

	private DescriptionDialog descriptionDialog;
	private MetaDialog metaDialog;

	@Nullable
	private Uri cacheUri;
	@Nullable
	private MediaStatus mediaStatus;
	@Nullable
	private ImageDownloader imageAsync;
	private GlobalSettings settings;
	private File cacheFolder;
	private Media.Meta meta;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_image);
		Toolbar toolbar = findViewById(R.id.page_image_toolbar);
		AnimatedImageView gifImage = findViewById(R.id.page_image_gif);
		descriptionView = findViewById(R.id.page_image_description);
		loadingCircle = findViewById(R.id.page_image_progress);
		zoomImage = findViewById(R.id.page_image_viewer);

		settings = GlobalSettings.get(this);
		AppStyles.setProgressColor(loadingCircle, settings.getHighlightColor());
		toolbar.setTitle("");
		toolbar.setBackgroundColor(settings.getBackgroundColor());
		setSupportActionBar(toolbar);

		imageAsync = new ImageDownloader(this);
		descriptionDialog = new DescriptionDialog(this, this);
		metaDialog = new MetaDialog(this);

		cacheFolder = new File(getExternalCacheDir(), ImageViewer.CACHE_FOLDER);
		cacheFolder.mkdirs();

		// get parameters
		String imageUrl = null;
		String blurHash = null;
		String description = null;
		boolean animated = false;
		boolean local = false;
		float ratio = 1.0f;
		Serializable serializedData;
		if (savedInstanceState != null) {
			serializedData = savedInstanceState.getSerializable(KEY_IMAGE_DATA);
		} else {
			serializedData = getIntent().getSerializableExtra(KEY_IMAGE_DATA);
		}
		if (serializedData instanceof MediaStatus) {
			mediaStatus = (MediaStatus) serializedData;
			imageUrl = mediaStatus.getPath();
			animated = mediaStatus.getMediaType() == MediaStatus.GIF;
			local = imageUrl != null && !imageUrl.startsWith("http");
			description = mediaStatus.getDescription();
		} else if (serializedData instanceof Media) {
			Media media = (Media) serializedData;
			meta = media.getMeta();
			blurHash = media.getBlurHash();
			imageUrl = media.getUrl();
			description = media.getDescription();
			animated = media.getMediaType() == Media.GIF;
			if (meta != null) {
				ratio = meta.getWidth() / (float) meta.getHeight();
			}
		} else if (serializedData instanceof String) {
			imageUrl = (String) serializedData;
		} else {
			finish();
		}
		// setup image view
		if (imageUrl != null && !imageUrl.trim().isEmpty()) {
			// select view to show image
			if (animated) {
				gifImage.setVisibility(View.VISIBLE);
				zoomImage.setVisibility(View.INVISIBLE);
			} else {
				gifImage.setVisibility(View.INVISIBLE);
				zoomImage.setVisibility(View.VISIBLE);
			}
			//  load image
			if (local) {
				if (animated) {
					gifImage.setImageURI(Uri.parse(imageUrl));
				} else {
					zoomImage.setImageURI(Uri.parse(imageUrl));
				}
			} else {
				loadingCircle.setVisibility(View.VISIBLE);
				ImageDownloader.Param request = new ImageDownloader.Param(Uri.parse(imageUrl), cacheFolder);
				imageAsync.execute(request, this);
			}
		}
		// set image description
		if (description != null && !description.trim().isEmpty()) {
			descriptionView.setDescription(description);
			descriptionView.setVisibility(View.VISIBLE);
		}
		// set image blur placeholder
		if (blurHash != null && !blurHash.trim().isEmpty()) {
			Bitmap blur = BlurHashDecoder.decode(blurHash, ratio);
			zoomImage.setImageBitmap(blur);
			zoomImage.setMovable(false);
		}
	}


	@Override
	public void onBackPressed() {
		if (mediaStatus != null) {
			Intent intent = new Intent();
			intent.putExtra(KEY_IMAGE_DATA, mediaStatus);
			setResult(RETURN_MEDIA_STATUS_UPDATE, intent);
		}
		super.onBackPressed();
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
		MenuItem itemDescription = menu.findItem(R.id.menu_image_add_description);
		itemDescription.setVisible(mediaStatus != null);
		AppStyles.setMenuIconColor(menu, settings.getIconColor());
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem itemSave = menu.findItem(R.id.menu_image_save);
		MenuItem itemMeta = menu.findItem(R.id.menu_image_show_meta);
		itemSave.setVisible(cacheUri != null);
		itemMeta.setVisible(meta != null);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.menu_image_save) {
			if (cacheUri != null) {
				storeImage(cacheUri);
				return true;
			}
		} else if (item.getItemId() == R.id.menu_image_add_description) {
			descriptionDialog.show();
			return true;
		} else if (item.getItemId() == R.id.menu_image_show_meta) {
			if (meta != null) {
				metaDialog.show(meta);
			}
			return true;
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
	public void onResult(@NonNull ImageDownloader.Result result) {
		if (result.uri != null) {
			loadingCircle.setVisibility(View.INVISIBLE);
			cacheUri = result.uri;
			zoomImage.reset();
			zoomImage.setImageURI(cacheUri);
			zoomImage.setMovable(true);
			invalidateMenu();
		} else {
			ErrorUtils.showErrorMessage(getApplicationContext(), result.exception);
			finish();
		}
	}


	@Override
	public void onDescriptionSet(String description) {
		if (description != null && !description.trim().isEmpty()) {
			descriptionView.setDescription(description);
			descriptionView.setVisibility(View.VISIBLE);
			if (mediaStatus != null) {
				mediaStatus.setDescription(description);
			}
		} else {
			descriptionView.setDescription("");
			descriptionView.setVisibility(View.INVISIBLE);
			if (mediaStatus != null) {
				mediaStatus.setDescription("");
			}
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
		} catch (SecurityException exception) {
			if (BuildConfig.DEBUG) {
				exception.printStackTrace();
			}
		}
	}
}