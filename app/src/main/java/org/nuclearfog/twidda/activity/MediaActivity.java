package org.nuclearfog.twidda.activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.ImageSaver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_PICK;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.widget.Toast.LENGTH_SHORT;

/**
 * This activity is a superclass to all activities who need permission to take actions
 *
 * @author nuclearfog
 */
public abstract class MediaActivity extends AppCompatActivity implements LocationListener {

    /**
     * permission set
     */
    private static final String[][] PERMISSIONS = {{READ_EXTERNAL_STORAGE}, {ACCESS_FINE_LOCATION}, {WRITE_EXTERNAL_STORAGE}};

    /**
     * Cursor mode to get the full path to the image
     */
    private static final String[] GET_MEDIA = {MediaStore.Images.Media.DATA};


    /**
     * mime type for image files with undefined extensions
     */
    private static final String TYPE_IMAGE = "image/*";

    /**
     * mime type for image files with undefined extensions
     */
    private static final String TYPE_VIDEO = "video/*";

    /**
     * request code to check permissions
     */
    private static final int REQ_CHECK_PERM = 4;

    /**
     * request code to pick an image
     */
    protected static final int REQUEST_IMAGE = 5;

    /**
     * request code to pick image or video
     */
    protected static final int REQUEST_IMG_VID = 6;

    /**
     * request code to pick an image for a profile picture
     */
    protected static final int REQUEST_PROFILE = 7;

    /**
     * request code to pick an image for a profile banner
     */
    protected static final int REQUEST_BANNER = 8;

    /**
     * request code to store image into storage
     */
    protected static final int REQUEST_STORE_IMG = 9;

    @Nullable
    private ImageSaver imageTask;
    private Bitmap image;
    private String filename;
    private boolean locationPending = false;


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
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length > 0 && grantResults.length > 0) {
            if (PERMISSIONS[0][0].equals(permissions[0])) {
                if (grantResults[0] == PERMISSION_GRANTED)
                    openMediaPicker(requestCode);
            } else if (PERMISSIONS[1][0].equals(permissions[0])) {
                if (grantResults[0] == PERMISSION_GRANTED)
                    fetchLocation();
                else
                    onAttachLocation(null);
            } else if ((PERMISSIONS[2][0].equals(permissions[0]))) {
                if (grantResults[0] == PERMISSION_GRANTED) {
                    saveImage();
                }
            }
        }
    }


    @Override
    protected final void onActivityResult(int reqCode, int returnCode, @Nullable Intent intent) {
        super.onActivityResult(reqCode, returnCode, intent);
        if (returnCode == RESULT_OK && intent != null && intent.getData() != null) {
            Cursor cursor = getContentResolver().query(intent.getData(), GET_MEDIA, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(GET_MEDIA[0]);
                    if (index >= 0) {
                        String path = cursor.getString(index);
                        onMediaFetched(reqCode, path);
                    }
                }
                cursor.close();
            }
        }
    }

    /**
     * save image to external storage
     */
    private void saveImage() {
        try {
            if (imageTask == null || imageTask.getStatus() != RUNNING) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    // store images directly
                    File imageFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES), filename);
                    OutputStream fileStream = new FileOutputStream(imageFile);
                    imageTask = new ImageSaver(this);
                    imageTask.execute(image, fileStream);
                } else {
                    // use scoped storage
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, DIRECTORY_PICTURES);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    Uri imageUri = getContentResolver().insert(EXTERNAL_CONTENT_URI, values);
                    if (imageUri != null) {
                        OutputStream fileStream = getContentResolver().openOutputStream(imageUri);
                        imageTask = new ImageSaver(this);
                        imageTask.execute(image, fileStream);
                    }
                }
            }
        } catch (FileNotFoundException err) {
            err.printStackTrace();
        }
    }


    @Override
    public final void onLocationChanged(@NonNull Location location) {
        onAttachLocation(location);
        locationPending = false;
    }


    @Override
    public final void onProviderDisabled(@NonNull String provider) {
        onAttachLocation(null);
        locationPending = false;
    }


    @Override
    public final void onProviderEnabled(@NonNull String provider) {
    }


    @Override
    public final void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /**
     * called when an image was successfully saved to external storage
     */
    public void onImageSaved() {
        Toast.makeText(getApplicationContext(), R.string.info_image_saved, Toast.LENGTH_LONG).show();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // start media scanner to scan for new image
            String[] fileName = {filename};
            MediaScannerConnection.scanFile(getApplicationContext(), fileName, null, null);
        }
    }

    /**
     * called when an error occurs while storing image
     */
    public void onError() {
        Toast.makeText(getApplicationContext(), R.string.error_image_save, Toast.LENGTH_SHORT).show();
    }

    /**
     * Ask for GPS location
     */
    protected void getLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(PERMISSIONS[1][0]) == PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            requestPermissions(PERMISSIONS[1], REQ_CHECK_PERM);
        }
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
            requestPermissions(PERMISSIONS[0], requestCode);
        }
    }

    /**
     * store image bitmap into storage
     *
     * @param filename name of the file
     * @param image    image bitmap to store
     */
    protected void storeImage(Bitmap image, String filename) {
        this.image = image;
        this.filename = filename;
        if (!filename.endsWith(".jpg")) {
            this.filename += ".jpg";
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                || checkSelfPermission(PERMISSIONS[2][0]) == PERMISSION_GRANTED) {
            saveImage();
        } else {
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
     * start locating
     */
    @SuppressLint("MissingPermission") // suppressing because of an android studio bug
    private void fetchLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
            locationPending = true;
        } else {
            onAttachLocation(null);
        }
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
                    mediaSelect.setType("*/*");
                    String[] mimeTypes = new String[]{TYPE_IMAGE, TYPE_VIDEO};
                    mediaSelect.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    break;

                case REQUEST_IMAGE:
                case REQUEST_PROFILE:
                case REQUEST_BANNER:
                    mediaSelect.setType(TYPE_IMAGE);
                    break;
            }
        } else {
            mediaSelect.setType("*/*");
        }
        try {
            startActivityForResult(mediaSelect, requestCode);
        } catch (ActivityNotFoundException err) {
            Toast.makeText(getApplicationContext(), R.string.error_no_media_app, LENGTH_SHORT).show();
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
     * @param path       local path to the media file
     */
    protected abstract void onMediaFetched(int resultType, String path);
}