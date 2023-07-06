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

import com.wolt.blurhashkt.BlurHashDecoder;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.ImageDownloader;
import org.nuclearfog.twidda.backend.async.ImageDownloader.ImageLoaderParam;
import org.nuclearfog.twidda.backend.async.ImageDownloader.ImageLoaderResult;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.ui.dialogs.DescriptionDialog;
import org.nuclearfog.twidda.ui.dialogs.DescriptionDialog.DescriptionCallback;
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
public class ImageViewer extends MediaActivity implements AsyncCallback<ImageLoaderResult>, DescriptionCallback {

	public static final int IMAGE_LOCAL = 900;

	public static final int GIF_LOCAL = 901;

	public static final int MEDIA_LOCAL = 902;

	public static final int IMAGE_ONLINE = 903;

	public static final int MEDIA_ONLINE = 904;

	public static final int RETURN_MEDIA_STATUS_UPDATE = 0x5895;

	/**
	 * key to set image format (image or gif)
	 * value type is Integer {@link #IMAGE_LOCAL,#IMAGE_ONLINE,#GIF_LOCAL,#MEDIA_LOCAL}
	 */
	public static final String TYPE = "image-type";

	/**
	 * key to add URI of the image (online or local)
	 * value type is {@link Uri}
	 */
	public static final String KEY_MEDIA_URL = "image-url";

	/**
	 * key to add offline media
	 * value type is {@link MediaStatus}
	 */
	public static final String KEY_MEDIA_LOCAL = "media-status";

	/**
	 * key to add online media
	 * value type is {@link Media}
	 */
	public static final String KEY_MEDIA_ONLINE = "media-online";

	/**
	 * name of the cache folder where online images will be stored
	 * after the end of this activity this folder will be cleared
	 */
	private static final String CACHE_FOLDER = "imagecache";

	private ZoomView zoomImage;
	private ProgressBar loadingCircle;
	private DescriptionView descriptionView;

	private DescriptionDialog descriptionDialog;

	@Nullable
	private Uri cacheUri;
	@Nullable
	private MediaStatus mediaStatus;
	@Nullable
	private ImageDownloader imageAsync;
	private GlobalSettings settings;
	private File cacheFolder;
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
		cacheFolder = new File(getExternalCacheDir(), ImageViewer.CACHE_FOLDER);
		cacheFolder.mkdirs();

		mode = getIntent().getIntExtra(TYPE, IMAGE_LOCAL);
		switch (mode) {
			case IMAGE_LOCAL:
				zoomImage.setVisibility(View.VISIBLE);
				gifImage.setVisibility(View.INVISIBLE);
				Uri data = getIntent().getParcelableExtra(KEY_MEDIA_URL);
				zoomImage.setImageURI(data);
				break;

			case IMAGE_ONLINE:
				zoomImage.setVisibility(View.VISIBLE);
				gifImage.setVisibility(View.INVISIBLE);
				loadingCircle.setVisibility(View.VISIBLE);
				data = getIntent().getParcelableExtra(KEY_MEDIA_URL);
				ImageLoaderParam request = new ImageLoaderParam(data, cacheFolder);
				imageAsync.execute(request, this);
				break;

			case GIF_LOCAL:
				zoomImage.setVisibility(View.INVISIBLE);
				gifImage.setVisibility(View.VISIBLE);
				data = getIntent().getParcelableExtra(KEY_MEDIA_URL);
				gifImage.setImageURI(data);
				break;

			case MEDIA_LOCAL:
				Serializable serializedData = getIntent().getSerializableExtra(KEY_MEDIA_LOCAL);
				if (serializedData instanceof  MediaStatus) {
					mediaStatus = (MediaStatus) serializedData;
					if (!mediaStatus.getDescription().trim().isEmpty()) {
						descriptionView.setVisibility(View.VISIBLE);
						descriptionView.setDescription(mediaStatus.getDescription());
					}
					if (mediaStatus.getMediaType() == MediaStatus.PHOTO) {
						zoomImage.setVisibility(View.VISIBLE);
						gifImage.setVisibility(View.INVISIBLE);
						zoomImage.setImageURI(Uri.parse(mediaStatus.getPath()));
					} else if (mediaStatus.getMediaType() == MediaStatus.GIF) {
						zoomImage.setVisibility(View.INVISIBLE);
						gifImage.setVisibility(View.VISIBLE);
						gifImage.setImageURI(Uri.parse(mediaStatus.getPath()));
					}
				}
				break;

			case MEDIA_ONLINE:
				serializedData = getIntent().getSerializableExtra(KEY_MEDIA_ONLINE);
				if (serializedData instanceof Media) {
					Media media = (Media) serializedData;
					if (!media.getBlurHash().isEmpty()) {
						Bitmap blur = BlurHashDecoder.INSTANCE.decode(media.getBlurHash(), 16, 16, 1f, true);
						zoomImage.setImageBitmap(blur);
					}
					if (!media.getDescription().isEmpty()) {
						descriptionView.setVisibility(View.VISIBLE);
						descriptionView.setDescription(media.getDescription());
					}
					if (media.getMediaType() == Media.PHOTO) {
						zoomImage.setVisibility(View.VISIBLE);
						gifImage.setVisibility(View.INVISIBLE);
						request = new ImageLoaderParam(Uri.parse(media.getUrl()), cacheFolder);
						imageAsync.execute(request, this);
					}
				}
				break;
		}
	}


	@Override
	public void onBackPressed() {
		if (mediaStatus != null) {
			Intent intent = new Intent();
			intent.putExtra(KEY_MEDIA_LOCAL, mediaStatus);
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
		MenuItem itemSave = menu.findItem(R.id.menu_image_save);
		MenuItem itemDescription = menu.findItem(R.id.menu_image_add_description);
		AppStyles.setMenuIconColor(menu, settings.getIconColor());
		itemSave.setVisible(mode == IMAGE_ONLINE || mode == MEDIA_ONLINE);
		itemDescription.setVisible(mediaStatus != null);
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
		else if (item.getItemId() == R.id.menu_image_add_description) {
			descriptionDialog.show();
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
	public void onResult(@NonNull ImageLoaderResult result) {
		if (result.uri != null) {
			loadingCircle.setVisibility(View.INVISIBLE);
			cacheUri = result.uri;
			zoomImage.reset();
			zoomImage.setImageURI(cacheUri);
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