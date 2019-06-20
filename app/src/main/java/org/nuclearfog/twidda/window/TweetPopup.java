package org.nuclearfog.twidda.window;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.StatusUploader;
import org.nuclearfog.twidda.backend.helper.FilenameTools;
import org.nuclearfog.twidda.backend.helper.FilenameTools.FileType;
import org.nuclearfog.twidda.backend.items.TweetHolder;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.LinkedList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_PICK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.window.MediaViewer.MediaType.ANGIF_STORAGE;
import static org.nuclearfog.twidda.window.MediaViewer.MediaType.IMAGE_STORAGE;
import static org.nuclearfog.twidda.window.MediaViewer.MediaType.VIDEO_STORAGE;


public class TweetPopup extends AppCompatActivity implements OnClickListener {

    public static final String KEY_TWEETPOPUP_REPLYID = "replyID";
    public static final String KEY_TWEETPOPUP_ADDITION = "Addition";

    private enum Mode {
        IMAGE,
        VIDEO,
        GIF,
        NONE
    }

    private static final String[] READ_STORAGE = {READ_EXTERNAL_STORAGE};
    private static final String[] GET_MEDIA = {MediaStore.Images.Media.DATA};
    private static final String TYPE_IMAGE = "image/*";
    private static final String TYPE_VIDEO = "video/*";
    private static final int PICK_MEDIA = 3;
    private static final int CHECK_PERM = 4;
    private static final int MAX_IMAGES = 4;

    private StatusUploader uploaderAsync;
    private View imageButton, previewBtn;
    private List<String> mediaPath;
    private TextView imgCount;
    private EditText tweet;
    private String addition = "";
    private long inReplyId = 0;
    private Mode mode = Mode.NONE;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.popup_tweet);
        View tweetButton = findViewById(R.id.sendTweet);
        View closeButton = findViewById(R.id.close);
        View root = findViewById(R.id.tweet_popup);
        imageButton = findViewById(R.id.image);
        previewBtn = findViewById(R.id.img_preview);
        tweet = findViewById(R.id.tweet_input);
        imgCount = findViewById(R.id.imgcount);

        Bundle param = getIntent().getExtras();
        if (param != null) {
            inReplyId = param.getLong(KEY_TWEETPOPUP_REPLYID, 0);
            addition = param.getString(KEY_TWEETPOPUP_ADDITION, "") + " ";
        }

        mediaPath = new LinkedList<>();
        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getPopupColor());
        tweet.append(addition);

        closeButton.setOnClickListener(this);
        tweetButton.setOnClickListener(this);
        imageButton.setOnClickListener(this);
        previewBtn.setOnClickListener(this);
    }


    @Override
    protected void onDestroy() {
        if (uploaderAsync != null && uploaderAsync.getStatus() == RUNNING)
            uploaderAsync.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        showClosingMsg();
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent intent) {
        super.onActivityResult(reqCode, returnCode, intent);
        if (reqCode == PICK_MEDIA && returnCode == RESULT_OK) {
            if (intent != null && intent.getData() != null) {
                Cursor cursor = getContentResolver().query(intent.getData(), GET_MEDIA, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(GET_MEDIA[0]);
                    String path = cursor.getString(index);
                    FileType type = FilenameTools.getFileType(path);

                    switch (type) {
                        case IMAGE:
                            if (mode == Mode.NONE)
                                mode = Mode.IMAGE;
                            if (mediaPath.size() < MAX_IMAGES && mode == Mode.IMAGE) {
                                mediaPath.add(path);
                                previewBtn.setVisibility(VISIBLE);
                                String count = Integer.toString(mediaPath.size());
                                imgCount.setText(count);
                                if (mediaPath.size() == MAX_IMAGES)
                                    imageButton.setVisibility(INVISIBLE);
                            }
                            break;

                        case ANGIF:
                            if (mode == Mode.NONE)
                                mode = Mode.GIF;
                            if (mode == Mode.GIF) {
                                mediaPath.add(path);
                                previewBtn.setVisibility(VISIBLE);
                                imageButton.setVisibility(INVISIBLE);
                            }
                            break;

                        case VIDEO:
                            if (mode == Mode.NONE)
                                mode = Mode.VIDEO;
                            if (mode == Mode.VIDEO) {
                                mediaPath.add(path);
                                previewBtn.setVisibility(VISIBLE);
                                imageButton.setVisibility(INVISIBLE);
                            }
                            break;

                        default:
                            Toast.makeText(this, R.string.error_file_format, LENGTH_SHORT).show();
                            break;
                    }
                    cursor.close();
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CHECK_PERM && grantResults[0] == PERMISSION_GRANTED)
            getMedia();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendTweet:
                String tweetStr = tweet.getText().toString();
                String[] paths = new String[mediaPath.size()];
                paths = mediaPath.toArray(paths);

                if (uploaderAsync != null && uploaderAsync.getStatus() == RUNNING)
                    uploaderAsync.cancel(true);

                if (tweetStr.trim().isEmpty() && paths.length == 0) {
                    Toast.makeText(this, R.string.empty_tweet, LENGTH_SHORT).show();
                } else if (paths.length > 0) {
                    TweetHolder tweet = new TweetHolder(tweetStr, inReplyId, paths);
                    uploaderAsync = new StatusUploader(this, tweet);
                    uploaderAsync.execute();
                } else if (!tweetStr.trim().isEmpty()) {
                    TweetHolder tweet = new TweetHolder(tweetStr, inReplyId);
                    uploaderAsync = new StatusUploader(this, tweet);
                    uploaderAsync.execute();
                }
                break;

            case R.id.close:
                showClosingMsg();
                break;

            case R.id.image:
                checkPermission();
                break;

            case R.id.img_preview:
                Intent image = new Intent(this, MediaViewer.class);
                image.putExtra(KEY_MEDIA_LINK, mediaPath.toArray(new String[0]));

                switch (mode) {
                    case IMAGE:
                        image.putExtra(KEY_MEDIA_TYPE, IMAGE_STORAGE);
                        startActivity(image);
                        break;

                    case VIDEO:
                        image.putExtra(KEY_MEDIA_TYPE, VIDEO_STORAGE);
                        startActivity(image);
                        break;

                    case GIF:
                        image.putExtra(KEY_MEDIA_TYPE, ANGIF_STORAGE);
                        startActivity(image);
                        break;
                }
                break;
        }
    }


    public void close() {
        Toast.makeText(this, R.string.tweet_sent, LENGTH_LONG).show();
        finish();
    }


    private void showClosingMsg() {
        if (!addition.equals(tweet.getText().toString()) || !mediaPath.isEmpty()) {
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


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = checkSelfPermission(READ_EXTERNAL_STORAGE);
            if (check == PERMISSION_GRANTED) {
                getMedia();
            } else {
                requestPermissions(READ_STORAGE, CHECK_PERM);
            }
        } else {
            getMedia();
        }
    }


    private void getMedia() {
        if (mode == Mode.NONE) {
            Intent mediaIntent = new Intent(ACTION_PICK);
            mediaIntent.setDataAndType(EXTERNAL_CONTENT_URI, TYPE_IMAGE + TYPE_VIDEO);
            startActivityForResult(mediaIntent, PICK_MEDIA);
        } else if (mode == Mode.IMAGE) {
            Intent imageIntent = new Intent(ACTION_PICK);
            imageIntent.setDataAndType(EXTERNAL_CONTENT_URI, TYPE_IMAGE);
            startActivityForResult(imageIntent, PICK_MEDIA);
        }
    }
}