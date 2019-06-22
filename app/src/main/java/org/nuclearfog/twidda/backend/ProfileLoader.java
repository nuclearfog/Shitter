package org.nuclearfog.twidda.backend;

import android.content.Intent;
import android.os.AsyncTask;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.window.MediaViewer;
import org.nuclearfog.twidda.window.UserProfile;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;

import twitter4j.TwitterException;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.window.MediaViewer.MediaType.IMAGE;


public class ProfileLoader extends AsyncTask<Long, TwitterUser, TwitterUser> {

    public enum Mode {
        LDR_PROFILE,
        ACTION_FOLLOW,
        ACTION_BLOCK,
        ACTION_MUTE
    }

    private final Mode mode;
    private WeakReference<UserProfile> ui;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private GlobalSettings settings;
    private DatabaseAdapter db;
    private boolean isHome;
    private boolean isFriend;
    private boolean isFollower;
    private boolean isBlocked;
    private boolean isMuted;
    private boolean canDm;


    public ProfileLoader(@NonNull UserProfile context, Mode mode) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        settings = GlobalSettings.getInstance(context);
        db = new DatabaseAdapter(context);
        this.mode = mode;
    }


    @Override
    protected TwitterUser doInBackground(Long[] args) {
        TwitterUser user = null;
        long userId = args[0];

        isHome = userId == settings.getUserId();
        try {
            if (mode == Mode.LDR_PROFILE) {
                user = db.getUser(userId);
                if (user != null)
                    publishProgress(user);
                user = mTwitter.getUser(userId);
                db.storeUser(user);
            }
            if (!isHome) {
                boolean[] connection = mTwitter.getConnection(userId);
                isFriend = connection[0];
                isFollower = connection[1];
                isBlocked = connection[2];
                isMuted = connection[3];
                canDm = connection[4];
                if (isBlocked || isMuted)
                    db.muteUser(userId, true);
                else
                    db.muteUser(userId, false);
            }
            if (user != null)
                publishProgress(user);

            switch (mode) {
                case ACTION_FOLLOW:
                    if (!isFriend) {
                        user = mTwitter.followUser(userId);
                        if (!user.isLocked())
                            isFriend = true;
                    } else {
                        user = mTwitter.unfollowUser(userId);
                        isFriend = false;
                    }
                    publishProgress(user);
                    break;

                case ACTION_BLOCK:
                    if (!isBlocked)
                        user = mTwitter.blockUser(userId);
                    else
                        user = mTwitter.unblockUser(userId);
                    isBlocked = !isBlocked;
                    publishProgress(user);
                    db.muteUser(userId, isBlocked);
                    break;

                case ACTION_MUTE:
                    if (!isMuted)
                        user = mTwitter.muteUser(userId);
                    else
                        user = mTwitter.unmuteUser(userId);
                    isMuted = !isMuted;
                    publishProgress(user);
                    db.muteUser(userId, isMuted);
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
    protected void onProgressUpdate(@NonNull TwitterUser[] users) {
        if (ui.get() != null) {
            final TwitterUser user = users[0];

            ImageView profile = ui.get().findViewById(R.id.profile_img);
            TextView txtUser = ui.get().findViewById(R.id.profile_username);
            TextView txtScrName = ui.get().findViewById(R.id.profile_screenname);
            TextView txtBio = ui.get().findViewById(R.id.bio);
            TextView txtLocation = ui.get().findViewById(R.id.location);
            TextView txtLink = ui.get().findViewById(R.id.links);
            TextView txtFollowing = ui.get().findViewById(R.id.following);
            TextView txtFollower = ui.get().findViewById(R.id.follower);
            View profile_head = ui.get().findViewById(R.id.profile_header);
            View follow_back = ui.get().findViewById(R.id.follow_back);

            String strFollower = settings.getNumberFormatter().format(user.getFollower());
            String strFollowing = settings.getNumberFormatter().format(user.getFollowing());
            Spanned bio = Tagger.makeText(user.getBio(), settings.getHighlightColor(), ui.get());
            txtBio.setMovementMethod(LinkMovementMethod.getInstance());
            txtUser.setText(user.getUsername());
            txtScrName.setText(user.getScreenname());
            txtFollower.setText(strFollower);
            txtFollowing.setText(strFollowing);
            txtBio.setText(bio);
            if (profile_head.getVisibility() != VISIBLE) {
                profile_head.setVisibility(VISIBLE);
                TextView txtCreated = ui.get().findViewById(R.id.profile_date);
                String date = SimpleDateFormat.getDateTimeInstance().format(user.getCreatedAt());
                txtCreated.setText(date);
            }
            if (user.isVerified()) {
                txtUser.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
            } else {
                txtUser.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            if (user.isLocked()) {
                txtScrName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
            } else {
                txtScrName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            if (isFollower) {
                follow_back.setVisibility(VISIBLE);
            } else {
                follow_back.setVisibility(INVISIBLE);
            }
            if (user.getLocation() != null && !user.getLocation().isEmpty()) {
                txtLocation.setText(user.getLocation());
                txtLocation.setVisibility(VISIBLE);
            } else {
                txtLocation.setVisibility(GONE);
            }
            if (user.getLink() != null && !user.getLink().isEmpty()) {
                txtLink.setText(user.getLink());
                txtLink.setVisibility(VISIBLE);
                txtLink.setOnClickListener(ui.get());
            } else {
                txtLink.setVisibility(GONE);
                txtLink.setOnClickListener(null);
            }
            if (settings.getImageLoad()) {
                String link = user.getImageLink() + "_bigger";
                Picasso.get().load(link).into(profile);
            }
            if (!profile.isClickable()) {
                profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent image = new Intent(ui.get(), MediaViewer.class);
                        image.putExtra(KEY_MEDIA_LINK, new String[]{user.getImageLink()});
                        image.putExtra(KEY_MEDIA_TYPE, IMAGE);
                        ui.get().startActivity(image);
                    }
                });
            }
            ui.get().setTweetCount(user.getTweetCount(), user.getFavorCount());
        }
    }


    @Override
    protected void onPostExecute(@Nullable TwitterUser user) {
        if (ui.get() != null) {
            if (user != null) {
                switch (mode) {
                    case ACTION_FOLLOW:
                        if (!user.isLocked())
                            if (isFriend)
                                Toast.makeText(ui.get(), R.string.followed, Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(ui.get(), R.string.unfollowed, Toast.LENGTH_SHORT).show();
                        break;

                    case ACTION_BLOCK:
                        if (isBlocked)
                            Toast.makeText(ui.get(), R.string.blocked, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ui.get(), R.string.unblocked, Toast.LENGTH_SHORT).show();
                        break;

                    case ACTION_MUTE:
                        if (isMuted)
                            Toast.makeText(ui.get(), R.string.muted, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ui.get(), R.string.unmuted, Toast.LENGTH_SHORT).show();
                        break;
                }
                if (!isHome) {
                    ui.get().setConnection(isFriend, isMuted, isBlocked, user.isLocked(), canDm, user.followRequested());
                }
            }
            if (err != null) {
                boolean killActivity = ErrorHandler.printError(ui.get(), err);
                if (killActivity)
                    ui.get().finish();
            }
        }
    }
}