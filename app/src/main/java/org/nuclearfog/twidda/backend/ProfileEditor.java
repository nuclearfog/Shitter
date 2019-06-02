package org.nuclearfog.twidda.backend;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.window.MediaViewer;
import org.nuclearfog.twidda.window.ProfileEdit;

import java.io.File;
import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.window.MediaViewer.MediaType.IMAGE;


public class ProfileEditor extends AsyncTask<Void, Void, Void> {

    public enum Mode {
        READ_DATA,
        WRITE_DATA
    }

    private final Mode mode;
    private boolean failure;

    private WeakReference<ProfileEdit> ui;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private TwitterUser user;
    private Editable edit_name, edit_link, edit_bio, edit_loc;
    private Dialog popup;
    private String image_path;


    public ProfileEditor(@NonNull ProfileEdit c, Mode mode) {
        ui = new WeakReference<>(c);
        mTwitter = TwitterEngine.getInstance(c);
        popup = new Dialog(c);
        this.mode = mode;

        EditText name = ui.get().findViewById(R.id.edit_name);
        EditText link = ui.get().findViewById(R.id.edit_link);
        EditText loc = ui.get().findViewById(R.id.edit_location);
        EditText bio = ui.get().findViewById(R.id.edit_bio);
        Button text_path = ui.get().findViewById(R.id.edit_upload);

        edit_name = name.getText();
        edit_link = link.getText();
        edit_loc = loc.getText();
        edit_bio = bio.getText();
        image_path = text_path.getText().toString();

        popup.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popup.setCanceledOnTouchOutside(false);
        popup.setContentView(new ProgressBar(c));
    }


    @Override
    protected void onPreExecute() {
        if (popup.getWindow() != null)
            popup.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        popup.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (getStatus() == Status.RUNNING) {
                    cancel(true);
                    ui.get().finish();
                }
            }
        });
        popup.show();
    }


    @Override
    protected Void doInBackground(Void... v) {
        try {
            switch (mode) {
                case READ_DATA:
                    user = mTwitter.getCurrentUser();
                    break;

                case WRITE_DATA:
                    String username = edit_name.toString();
                    String user_link = edit_link.toString();
                    String user_loc = edit_loc.toString();
                    String user_bio = edit_bio.toString();
                    user = mTwitter.updateProfile(username, user_link, user_loc, user_bio);
                    DatabaseAdapter db = new DatabaseAdapter(ui.get());
                    db.storeUser(user);

                    if (!image_path.trim().isEmpty())
                        mTwitter.updateProfileImage(new File(image_path));
                    break;
            }
        } catch (TwitterException err) {
            this.err = err;
            failure = true;
        } catch (Exception err) {
            if (err.getMessage() != null)
                Log.e("E: ProfileEditor", err.getMessage());
            failure = true;
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void v) {
        if (ui.get() == null) return;

        popup.dismiss();

        if (failure) {
            ErrorHandler.printError(ui.get(), err);
            ui.get().finish();
        } else {
            switch (mode) {
                case READ_DATA:
                    edit_name.append(user.getUsername());
                    edit_link.append(user.getLink());
                    edit_loc.append(user.getLocation());
                    edit_bio.append(user.getBio());

                    ImageView pb_image = ui.get().findViewById(R.id.edit_pb);
                    String link = user.getImageLink() + "_bigger";
                    Picasso.get().load(link).into(pb_image);

                    final String mediaLink[] = new String[]{user.getImageLink()};
                    pb_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent image = new Intent(ui.get(), MediaViewer.class);
                            image.putExtra(KEY_MEDIA_LINK, mediaLink);
                            image.putExtra(KEY_MEDIA_TYPE, IMAGE);
                            ui.get().startActivity(image);
                        }
                    });
                    break;

                case WRITE_DATA:
                    Toast.makeText(ui.get(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
                    ui.get().finish();
                    break;
            }
        }
    }


    @Override
    protected void onCancelled() {
        popup.dismiss();
    }
}