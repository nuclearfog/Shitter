package org.nuclearfog.twidda.window;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.ImageAdapter;
import org.nuclearfog.twidda.adapter.ImageAdapter.OnImageClickListener;
import org.nuclearfog.twidda.backend.ImageLoader;
import org.nuclearfog.zoomview.ZoomView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL;
import static org.nuclearfog.twidda.backend.ImageLoader.Mode.ONLINE;
import static org.nuclearfog.twidda.backend.ImageLoader.Mode.STORAGE;


public class MediaViewer extends AppCompatActivity implements OnImageClickListener, OnPreparedListener {

    public static final String KEY_MEDIA_LINK = "link";
    public static final String KEY_MEDIA_TYPE = "mediatype";

    public enum MediaType {
        IMAGE,
        IMAGE_STORAGE,
        VIDEO,
        VIDEO_STORAGE,
        ANGIF,
        ANGIF_STORAGE
    }

    private ImageLoader imageAsync;
    private VideoView videoView;
    private ZoomView zoomImage;
    private MediaType type;
    private String[] link;
    private int width;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        setContentView(R.layout.page_image);
        RecyclerView imageList = findViewById(R.id.image_list);
        MediaController videoController = findViewById(R.id.video_controller);
        View imageWindow = findViewById(R.id.image_window);
        View videoWindow = findViewById(R.id.video_window);
        zoomImage = findViewById(R.id.image_full);
        videoView = findViewById(R.id.video_view);

        Bundle param = getIntent().getExtras();
        if (param != null && param.containsKey(KEY_MEDIA_LINK) && param.containsKey(KEY_MEDIA_TYPE)) {
            link = param.getStringArray(KEY_MEDIA_LINK);
            type = (MediaType) param.getSerializable(KEY_MEDIA_TYPE);
        } else if (BuildConfig.DEBUG)
            throw new AssertionError();

        switch (type) {
            case IMAGE:
            case IMAGE_STORAGE:
                imageList.setLayoutManager(new LinearLayoutManager(this, HORIZONTAL, false));
                imageList.setAdapter(new ImageAdapter(this));
                Display d = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                d.getSize(size);
                width = size.x;
                imageWindow.setVisibility(VISIBLE);
                break;

            case ANGIF:
                videoController.hide();
                videoWindow.setVisibility(VISIBLE);
                videoView.setVideoPath(link[0]);
                videoView.setOnPreparedListener(this);
                break;

            case ANGIF_STORAGE:
                videoController.hide();
                videoWindow.setVisibility(VISIBLE);
                videoView.setVideoPath(link[0]);
                videoView.setOnPreparedListener(this);
                break;

            case VIDEO:
                videoController.setAnchorView(videoView);
                videoView.setMediaController(videoController);
                videoWindow.setVisibility(VISIBLE);
                videoView.setVideoURI(Uri.parse(link[0]));
                break;

            case VIDEO_STORAGE:
                videoController.setAnchorView(videoView);
                videoView.setMediaController(videoController);
                videoWindow.setVisibility(VISIBLE);
                videoView.setVideoPath(link[0]);
                break;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        switch (type) {
            case IMAGE:
                if (imageAsync == null) {
                    imageAsync = new ImageLoader(this, ONLINE);
                    imageAsync.execute(link);
                }
            case IMAGE_STORAGE:
                if (imageAsync == null) {
                    imageAsync = new ImageLoader(this, STORAGE);
                    imageAsync.execute(link);
                }
                break;

            case VIDEO:
            case ANGIF:
            case VIDEO_STORAGE:
            case ANGIF_STORAGE:
                if (videoView.getCurrentPosition() == 0)
                    videoView.start();
                else
                    videoView.resume();
                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (type != MediaType.IMAGE && type != MediaType.IMAGE_STORAGE)
            videoView.pause();
    }


    @Override
    protected void onDestroy() {
        if (imageAsync != null && imageAsync.getStatus() == Status.RUNNING)
            imageAsync.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onImageClick(Bitmap image) {
        setImage(image);
    }


    @Override
    public boolean onImageTouch(Bitmap image) {
        if (type == MediaType.IMAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int check = checkSelfPermission(WRITE_EXTERNAL_STORAGE);
                if (check == PERMISSION_GRANTED) {
                    storeImage(image);
                } else {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 1);
                }
            } else {
                storeImage(image);
            }
            return true;
        }
        return false;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        if (type == MediaType.ANGIF || type == MediaType.ANGIF_STORAGE)
            mp.setLooping(true);
    }


    public void setImage(Bitmap image) {
        float ratio = image.getWidth() / (float) width;
        int destHeight = (int) (image.getHeight() / ratio);
        image = Bitmap.createScaledBitmap(image, width, destHeight, false);
        zoomImage.reset();
        zoomImage.setImageBitmap(image);
    }


    private void storeImage(Bitmap image) {
        String path = Environment.getExternalStorageDirectory().toString();
        path += "/Pictures/Shitter";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.GERMANY);
        String name = "shitter_" + formatter.format(new Date()) + ".png";

        File dir = new File(path);
        if (dir.mkdirs())
            Toast.makeText(this, R.string.image_folder_created, Toast.LENGTH_SHORT).show();
        File file = new File(dir, name);

        try {
            FileOutputStream output = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 0, output);
            Toast.makeText(this, R.string.image_saved, Toast.LENGTH_LONG).show();
            output.close();
        } catch (Exception err) {
            Toast.makeText(this, R.string.image_store_failure, Toast.LENGTH_SHORT).show();
        }
    }
}