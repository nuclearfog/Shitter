package org.nuclearfog.twidda.window;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.ImageAdapter;
import org.nuclearfog.twidda.adapter.ImageAdapter.OnImageClickListener;
import org.nuclearfog.twidda.backend.ImageLoad;
import org.nuclearfog.zoomview.ZoomView;

import static android.os.AsyncTask.Status.RUNNING;
import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;


public class ImageDetail extends AppCompatActivity implements OnImageClickListener {

    private ImageLoad mImage;
    private ZoomView zoomImage;
    private String link[];
    private int width;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_image);

        b = getIntent().getExtras();
        if (b != null)
            link = b.getStringArray("link");

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
        if (mImage != null && mImage.getStatus() == RUNNING) {
            mImage.cancel(true);
        }
        super.onDestroy();
    }


    @Override
    public void onImageClick(Bitmap image) {
        setImage(image);
    }


    public void setImage(Bitmap image) {
        float ratio = image.getWidth() / (float) width;
        int destHeight = (int) (image.getHeight() / ratio);
        image = Bitmap.createScaledBitmap(image, width, destHeight, false);
        zoomImage.reset();
        zoomImage.setImageBitmap(image);
    }
}