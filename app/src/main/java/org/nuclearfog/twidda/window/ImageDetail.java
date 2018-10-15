package org.nuclearfog.twidda.window;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.ImageAdapter;
import org.nuclearfog.twidda.adapter.ImageAdapter.OnImageClickListener;
import org.nuclearfog.twidda.backend.ImageLoad;
import org.nuclearfog.zoomview.ZoomView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;


/**
 * @see ImageLoad
 */
public class ImageDetail extends AppCompatActivity implements OnImageClickListener {

    boolean storable = true;
    private ImageLoad mImage;
    private ZoomView zoomImage;
    private String link[];
    private int width;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_image);

        b = getIntent().getExtras();
        if (b != null) {
            link = b.getStringArray("link");
            if (b.containsKey("storable"))
                storable = b.getBoolean("storable");
        }

        zoomImage = findViewById(R.id.image_full);
        RecyclerView imageList = findViewById(R.id.image_list);
        imageList.setLayoutManager(new LinearLayoutManager(this, HORIZONTAL, false));
        imageList.setAdapter(new ImageAdapter(this));
        Display d = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);
        width = size.x;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mImage == null) {
            mImage = new ImageLoad(this);
            mImage.execute(link);
        }
    }


    @Override
    protected void onDestroy() {
        if (mImage != null && mImage.getStatus() == RUNNING)
            mImage.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onImageClick(Bitmap image) {
        setImage(image);
    }


    @Override
    public boolean onImageTouch(Bitmap image) {
        if (storable) {
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
        String name = "shitter_" + formatter.format(new Date()) + ".jpg";

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
            err.printStackTrace();
        }
    }
}