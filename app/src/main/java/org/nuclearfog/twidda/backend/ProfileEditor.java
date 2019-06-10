package org.nuclearfog.twidda.backend;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Editable;
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

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.window.MediaViewer.MediaType.IMAGE;


public class ProfileEditor extends AsyncTask<Void, Void, TwitterUser> {

    public enum Mode {
        READ_DATA,
        WRITE_DATA
    }
    private final Mode mode;
    private WeakReference<ProfileEdit> ui;
    private WeakReference<Dialog> popup;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private Editable edit_name, edit_link, edit_bio, edit_loc;
    private String image_path;


    public ProfileEditor(@NonNull ProfileEdit c, Mode mode) {
        ui = new WeakReference<>(c);
        popup = new WeakReference<>(new Dialog(c));
        mTwitter = TwitterEngine.getInstance(c);

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
        this.mode = mode;
    }


    @Override
    protected void onPreExecute() {
        if (popup.get() == null || ui.get() == null) return;

        Dialog window = popup.get();
        window.requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setCanceledOnTouchOutside(false);
        window.setContentView(new ProgressBar(ui.get()));
        if (window.getWindow() != null)
            window.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        window.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (getStatus() == Status.RUNNING) {
                    cancel(true);
                    ui.get().finish();
                }
            }
        });
        window.show();
    }


    @Override
    protected TwitterUser doInBackground(Void[] v) {
        TwitterUser user = null;
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
                        mTwitter.updateProfileImage(image_path);
                    break;
            }
        } catch (TwitterException err) {
            this.err = err;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return user;
    }


    @Override
    protected void onPostExecute(TwitterUser user) {
        if (ui.get() != null && popup.get() != null) {
            if (user != null) {
                switch (mode) {
                    case READ_DATA:
                        edit_name.append(user.getUsername());
                        edit_link.append(user.getLink());
                        edit_loc.append(user.getLocation());
                        edit_bio.append(user.getBio());

                        ImageView pb_image = ui.get().findViewById(R.id.edit_pb);
                        String link = user.getImageLink() + "_bigger";
                        Picasso.get().load(link).into(pb_image);

                        final String mediaLink[] = {user.getImageLink()};
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
            } else {
                ErrorHandler.printError(ui.get(), err);
                ui.get().finish();
            }
            popup.get().dismiss();
        }
    }
}