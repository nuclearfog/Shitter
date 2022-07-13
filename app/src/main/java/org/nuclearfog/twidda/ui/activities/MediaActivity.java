package org.nuclearfog.twidda.ui.activities;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_PICK;
import static android.content.Intent.EXTRA_MIME_TYPES;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.provider.MediaStore.Images.Media.DATE_TAKEN;
import static android.provider.MediaStore.Images.Media.DISPLAY_NAME;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.Images.Media.MIME_TYPE;
import static android.provider.MediaStore.Images.Media.RELATIVE_PATH;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.ImageSaver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * This activity provides methods to get media or location
 *
 * @author nuclearfog
 */
public abstract class MediaActivity extends AppCompatActivity implements LocationListener {

	/**
	 * permissions used by the app
	 */
	private static final String[][] PERMISSIONS = {
			{READ_EXTERNAL_STORAGE},
			{ACCESS_FINE_LOCATION},
			{WRITE_EXTERNAL_STORAGE}
	};

	private static final String MIME_ALL_READ = "*/*";
	private static final String MIME_IMAGE_READ = "image/*";
	private static final String MIME_VIDEO_READ = "video/*";

	/**
	 * image name prefix used to save images
	 */
	private static final String IMAGE_PREFIX = "twitter_";

	/**
	 * mime types for videos and images
	 */
	private static final String[] TYPE_ALL = {MIME_IMAGE_READ, MIME_VIDEO_READ};

	/**
	 * request code to check permissions
	 */
	private static final int REQ_CHECK_PERM = 0xF233;

	/**
	 * request code to pick an image
	 */
	protected static final int REQUEST_IMAGE = 0x383C;

	/**
	 * request code to pick image or video
	 */
	protected static final int REQUEST_IMG_VID = 0x6B1A;

	/**
	 * request code to pick an image for a profile picture
	 */
	protected static final int REQUEST_PROFILE = 0xD636;

	/**
	 * request code to pick an image for a profile banner
	 */
	protected static final int REQUEST_BANNER = 0xE7E3;

	/**
	 * request code to store image into storage
	 */
	protected static final int REQUEST_STORE_IMG = 0x58D3;

	@Nullable
	private ImageSaver imageTask;
	private boolean locationPending = false;
	private Uri selectedImage;
	private String imageName;


	@Override
	protected void onDestroy() {
		if (locationPending) {
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if (locationManager != null) {
				locationManager.removeUpdates(this);
			}
		}
		if (imageTask != null && imageTask.getStatus() == RUNNING) {
			imageTask.cancel(true);
		}
		super.onDestroy();
	}


	@Override
	public final void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (permissions.length > 0 && grantResults.length > 0) {
			// read storage permission granted
			if (PERMISSIONS[0][0].equals(permissions[0]) && grantResults[0] == PERMISSION_GRANTED) {
				openMediaPicker(requestCode);
			}
			// location permission granted
			else if (PERMISSIONS[1][0].equals(permissions[0])) {
				getLocation(false);
			}
			// Write storage permissions granted
			else if (PERMISSIONS[2][0].equals(permissions[0]) && grantResults[0] == PERMISSION_GRANTED) {
				saveImage();
			}
		}
	}


	@Override
	protected final void onActivityResult(int reqCode, int returnCode, @Nullable Intent intent) {
		super.onActivityResult(reqCode, returnCode, intent);
		if (returnCode == RESULT_OK && intent != null && intent.getData() != null) {
			onMediaFetched(reqCode, intent.getData());
		}
	}


	@Override
	public final void onLocationChanged(@NonNull Location location) {
		locationPending = false;
		onAttachLocation(location);
	}


	@Override
	public final void onProviderDisabled(@NonNull String provider) {
		locationPending = false;
		onAttachLocation(null);
	}


	@Override
	public final void onProviderEnabled(@NonNull String provider) {
	}


	@Override
	public final void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/**
	 * save image to external storage
	 */
	private void saveImage() {
		try {
			if (imageTask == null || imageTask.getStatus() != RUNNING) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
					// store images directly
					File imageFolder = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
					File imageFile = new File(imageFolder, imageName);
					InputStream src = getContentResolver().openInputStream(selectedImage);
					OutputStream dest = new FileOutputStream(imageFile);
					imageTask = new ImageSaver(this, src, dest);
					imageTask.execute();
				} else {
					// use scoped storage
					String ext = selectedImage.getLastPathSegment();
					ext = ext.substring(ext.indexOf('.') + 1).toLowerCase(Locale.ENGLISH);
					String mime = "image/" + ext;
					ContentValues values = new ContentValues();
					values.put(DISPLAY_NAME, imageName);
					values.put(DATE_TAKEN, System.currentTimeMillis());
					values.put(RELATIVE_PATH, DIRECTORY_PICTURES);
					values.put(MIME_TYPE, mime);
					Uri imageUri = getContentResolver().insert(EXTERNAL_CONTENT_URI, values);
					if (imageUri != null) {
						InputStream src = getContentResolver().openInputStream(selectedImage);
						OutputStream dest = getContentResolver().openOutputStream(imageUri);
						imageTask = new ImageSaver(this, src, dest);
						imageTask.execute();
					}
				}
			}
		} catch (Exception err) {
			err.printStackTrace();
			onError();
		}
	}

	/**
	 * called when an image was successfully saved to external storage
	 */
	public void onImageSaved() {
		Toast.makeText(this, R.string.info_image_saved, LENGTH_SHORT).show();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			// start media scanner to scan for new image
			MediaScannerConnection.scanFile(this, new String[]{imageName}, null, null);
		}
	}

	/**
	 * called when an error occurs while storing image
	 */
	public void onError() {
		Toast.makeText(this, R.string.error_image_save, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Ask for GPS location
	 *
	 * @param ask set true to ask for permission
	 */
	protected void getLocation(boolean ask) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(PERMISSIONS[1][0]) == PERMISSION_GRANTED) {
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
				locationPending = true;
				return;
			}
		} else if (ask) {
			if (shouldShowRequestPermissionRationale(PERMISSIONS[1][0]))
				Toast.makeText(this, R.string.info_permission_location, LENGTH_LONG).show();
			requestPermissions(PERMISSIONS[1], REQ_CHECK_PERM);
			return;
		}
		onAttachLocation(null);
	}

	/**
	 * get media from storage
	 *
	 * @param requestCode media type
	 */
	protected void getMedia(int requestCode) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(PERMISSIONS[0][0]) == PERMISSION_GRANTED) {
			openMediaPicker(requestCode);
		} else {
			if (shouldShowRequestPermissionRationale(PERMISSIONS[0][0]))
				Toast.makeText(this, R.string.info_permission_read, LENGTH_LONG).show();
			requestPermissions(PERMISSIONS[0], requestCode);
		}
	}

	/**
	 * store image bitmap into storage
	 *
	 * @param uri Uri of the image
	 */
	protected void storeImage(Uri uri) {
		selectedImage = uri;
		imageName = IMAGE_PREFIX + uri.getLastPathSegment();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
				|| checkSelfPermission(PERMISSIONS[2][0]) == PERMISSION_GRANTED) {
			saveImage();
		} else {
			if (shouldShowRequestPermissionRationale(PERMISSIONS[2][0]))
				Toast.makeText(this, R.string.info_permission_write, LENGTH_LONG).show();
			requestPermissions(PERMISSIONS[2], REQUEST_STORE_IMG);
		}
	}

	/**
	 * check if a location is pending
	 *
	 * @return true if GPS access if finished
	 */
	protected boolean isLocating() {
		return locationPending;
	}

	/**
	 * open media picker to select a media file
	 *
	 * @param requestCode type of media to fetch
	 */
	private void openMediaPicker(int requestCode) {
		Intent mediaSelect = new Intent(ACTION_PICK);
		mediaSelect.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			switch (requestCode) {
				case REQUEST_IMG_VID:
					// pick image or video
					mediaSelect.setType(MIME_ALL_READ);
					mediaSelect.putExtra(EXTRA_MIME_TYPES, TYPE_ALL);
					break;

				case REQUEST_IMAGE:
				case REQUEST_PROFILE:
				case REQUEST_BANNER:
					mediaSelect.setType(MIME_IMAGE_READ);
					break;
			}
		} else {
			mediaSelect.setType(MIME_ALL_READ);
		}
		try {
			startActivityForResult(mediaSelect, requestCode);
		} catch (ActivityNotFoundException err) {
			Toast.makeText(this, R.string.error_no_media_app, LENGTH_SHORT).show();
		}
	}

	/**
	 * called when location information was successfully fetched
	 *
	 * @param location location information
	 */
	protected abstract void onAttachLocation(@Nullable Location location);

	/**
	 * called when a media file path was successfully fetched
	 *
	 * @param resultType type of media call
	 * @param uri        Uri of the file
	 */
	protected abstract void onMediaFetched(int resultType, @NonNull Uri uri);
}