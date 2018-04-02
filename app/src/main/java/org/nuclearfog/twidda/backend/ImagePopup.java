package org.nuclearfog.twidda.backend;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.nuclearfog.twidda.R;
import java.io.InputStream;
import java.net.URL;

public class ImagePopup extends AsyncTask<String, Void, Boolean> implements Button.OnClickListener {

    private ImageView mImg;
    private Dialog popup;
    private Bitmap imgArray[];
    private ProgressBar mCircle;
    private LayoutInflater inf;
    private Button left, right;
    private int index = 0;
    private int position = 0;

    public ImagePopup(Context c) {
        popup = new Dialog(c);
        mCircle = new ProgressBar(c);
        inf = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    protected void onPreExecute() {
        popup.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(popup.getWindow() != null)
            popup.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        popup.setContentView(mCircle);
        popup.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(true);
            }
        });
        popup.show();
    }

    @Override
    protected Boolean doInBackground(String... links) {
        try {
            int size = links.length;
            if(size == 0)
                return false;
            imgArray = new Bitmap[size];
            if(links[0].startsWith("/")) {
                for(String link : links) {
                    if(link != null)
                        imgArray[index++] = BitmapFactory.decodeFile(link);
                }
            }
            else {
                for(String link : links) {
                    InputStream mediaStream = new URL(link).openStream();
                    imgArray[index++] = BitmapFactory.decodeStream(mediaStream);
                }
            }
            return true;
        } catch (Exception err) {
            Log.e("shitter:","Image download failed!");
            err.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(result) {
            View content = inf.inflate(R.layout.imagepreview,null);
            mImg = content.findViewById(R.id.fullSizeImage);
            setImage(imgArray[position]);
            popup.setContentView(content);
            if(index > 0) {
                left = content.findViewById(R.id.image_left);
                right = content.findViewById(R.id.image_right);
                if(index > 1) {
                    left.setVisibility(View.INVISIBLE);
                    right.setVisibility(View.VISIBLE);
                    left.setOnClickListener(this);
                    right.setOnClickListener(this);
                }
                popup.show();
            }
        } else {
            popup.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.image_left:
                if(position == 1)
                    left.setVisibility(View.INVISIBLE);
                right.setVisibility(View.VISIBLE);
                position--;
                break;
            case R.id.image_right:
                if(position == index-2)
                    right.setVisibility(View.INVISIBLE);
                left.setVisibility(View.VISIBLE);
                position++;
                break;
        }
        setImage(imgArray[position]);
    }

    private void setImage(Bitmap btm) {
        int height = (int)(btm.getHeight() / (btm.getWidth() / 640.0));
        btm = Bitmap.createScaledBitmap( btm,640,height, false);
        mImg.setImageBitmap(btm);
    }
}