package org.nuclearfog.twidda.backend;

import android.annotation.SuppressLint;
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

import org.nuclearfog.twidda.R;

import java.io.InputStream;
import java.net.URL;

public class ImagePopup extends AsyncTask<String, Void, Boolean> {

    private Dialog popup;
    private Bitmap imgArray[];
    private LayoutInflater inflater;
    private int position = 0;


    public ImagePopup(Context context) {
        popup = new Dialog(context);
        inflater = LayoutInflater.from(context);
    }


    @Override
    @SuppressLint("InflateParams")
    protected void onPreExecute() {
        popup.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popup.setCanceledOnTouchOutside(false);
        if (popup.getWindow() != null)
            popup.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View load = inflater.inflate(R.layout.item_load, null, false);
        View cancelButton = load.findViewById(R.id.kill_button);
        popup.setContentView(load);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
        popup.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (getStatus() == Status.RUNNING)
                    cancel(true);
            }
        });
        popup.show();
    }


    @Override
    protected Boolean doInBackground(String... links) {
        try {
            int size = links.length;
            if (size == 0)
                return false;
            imgArray = new Bitmap[size];
            if (links[0].startsWith("/")) {
                for (int index = 0; index < size; index++) {
                    imgArray[index] = BitmapFactory.decodeFile(links[index]);
                }
            } else {
                for (int index = 0; index < size; index++) {
                    InputStream mediaStream = new URL(links[index]).openStream();
                    imgArray[index] = BitmapFactory.decodeStream(mediaStream);
                }
            }
            return true;
        } catch (Exception err) {
            err.printStackTrace();
            Log.e("Image Popup", err.getMessage());
            return false;
        }
    }


    @Override
    @SuppressLint("InflateParams")
    protected void onPostExecute(Boolean result) {
        if (result) {
            View content = inflater.inflate(R.layout.popup_image, null, false);
            final ImageView mImg = content.findViewById(R.id.fullSizeImage);
            setImage(imgArray[position], mImg);
            popup.setContentView(content);
            popup.setCanceledOnTouchOutside(true);
            final int size = imgArray.length;
            if (size > 0) {
                final Button left = content.findViewById(R.id.image_left);
                final Button right = content.findViewById(R.id.image_right);
                if (size > 1) {
                    left.setVisibility(View.INVISIBLE);
                    right.setVisibility(View.VISIBLE);
                    left.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (position == 1)
                                left.setVisibility(View.INVISIBLE);
                            right.setVisibility(View.VISIBLE);
                            setImage(imgArray[--position], mImg);
                        }
                    });
                    right.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (position == size - 2)
                                right.setVisibility(View.INVISIBLE);
                            left.setVisibility(View.VISIBLE);
                            setImage(imgArray[++position], mImg);
                        }
                    });
                }
                popup.show();
            }
        } else {
            popup.dismiss();
        }
    }


    private void setImage(Bitmap btm, ImageView mImg) {
        if (btm != null) {
            int height = (int) (btm.getHeight() / (btm.getWidth() / 640.0));
            btm = Bitmap.createScaledBitmap(btm, 640, height, false);
            mImg.setImageBitmap(btm);
        }
    }
}