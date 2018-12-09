package org.nuclearfog.twidda.window;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.StatusUpload;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_PICK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

/**
 * Tweet Window
 *
 * @see StatusUpload
 */
public class TweetPopup extends AppCompatActivity implements OnClickListener {

    private StatusUpload sendTweet;
    private View imageButton, previewBtn;
    private List<String> mediaPath;
    private TextView imgCount;
    private EditText tweet;
    private String addition = "";
    private long inReplyId = -1L;
    private int imgIndex = 0;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.popup_tweet);

        b = getIntent().getExtras();
        if (b != null) {
            if (b.containsKey("TweetID"))
                inReplyId = b.getLong("TweetID");
            if (b.containsKey("Addition"))
                addition = b.getString("Addition") + " ";
        }

        mediaPath = new ArrayList<>();

        View tweetButton = findViewById(R.id.sendTweet);
        View closeButton = findViewById(R.id.close);
        View root = findViewById(R.id.tweet_popup);
        imageButton = findViewById(R.id.image);
        previewBtn = findViewById(R.id.img_preview);
        tweet = findViewById(R.id.tweet_input);
        imgCount = findViewById(R.id.imgcount);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getTweetColor());
        tweet.append(addition);

        closeButton.setOnClickListener(this);
        tweetButton.setOnClickListener(this);
        imageButton.setOnClickListener(this);
        previewBtn.setOnClickListener(this);
    }


    @Override
    protected void onDestroy() {
        if (sendTweet != null && sendTweet.getStatus() == RUNNING)
            sendTweet.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        showClosingMsg();
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        super.onActivityResult(reqCode, returnCode, i);
        if (returnCode == RESULT_OK) {
            String[] mode = {MediaStore.Images.Media.DATA};
            if (i.getData() != null) {
                Cursor c = getContentResolver().query(i.getData(), mode, null, null, null);
                if (c != null && c.moveToFirst()) {
                    if (imgIndex == 0) {
                        previewBtn.setVisibility(View.VISIBLE);
                    }
                    if (imgIndex < 4) {
                        int index = c.getColumnIndex(mode[0]);
                        mediaPath.add(c.getString(index));
                        String count = Integer.toString(++imgIndex);
                        imgCount.setText(count);
                    }
                    if (imgIndex == 4) {
                        imageButton.setVisibility(View.INVISIBLE);
                    }
                    c.close();
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PERMISSION_GRANTED)
            getMedia();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendTweet:
                String tweetStr = tweet.getText().toString();
                if (sendTweet != null && sendTweet.getStatus() == RUNNING)
                    sendTweet.cancel(true);
                sendTweet = new StatusUpload(this, tweetStr, inReplyId);

                if (tweetStr.trim().isEmpty() && mediaPath.isEmpty()) {
                    Toast.makeText(this, R.string.empty_tweet, Toast.LENGTH_SHORT).show();
                } else if (tweetStr.length() > 280) {
                    Toast.makeText(this, R.string.char_limit_reached, Toast.LENGTH_SHORT).show();
                } else if (!mediaPath.isEmpty()) {
                    String[] paths = new String[mediaPath.size()];
                    paths = mediaPath.toArray(paths);
                    sendTweet.execute(paths);
                } else {
                    sendTweet.execute();
                }
                break;

            case R.id.close:
                showClosingMsg();
                break;

            case R.id.image:
                getMedia();
                break;

            case R.id.img_preview:
                Intent image = new Intent(this, ImageDetail.class);
                image.putExtra("link", mediaPath.toArray(new String[0]));
                image.putExtra("storable", false);
                startActivity(image);
                break;
        }
    }


    public void close() {
        Toast.makeText(this, R.string.tweet_sent, Toast.LENGTH_LONG).show();
        finish();
    }


    private void showClosingMsg() {
        if (!addition.equals(tweet.getText().toString()) || imgIndex > 0) {
            Builder closeDialog = new Builder(this);
            closeDialog.setMessage(R.string.confirm_cancel_tweet);
            closeDialog.setNegativeButton(R.string.no_confirm, null);
            closeDialog.setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            closeDialog.show();
        } else {
            finish();
        }
    }


    private void getMedia() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = checkSelfPermission(READ_EXTERNAL_STORAGE);
            if (check == PERMISSION_GRANTED) {
                Intent mediaIntent = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
                startActivityForResult(mediaIntent, 0);
            } else {
                requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, 1);
            }
        } else {
            Intent mediaIntent = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
            startActivityForResult(mediaIntent, 0);
        }
    }
}