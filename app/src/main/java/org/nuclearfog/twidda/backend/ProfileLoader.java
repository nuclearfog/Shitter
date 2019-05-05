package org.nuclearfog.twidda.backend;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.window.ImageDetail;
import org.nuclearfog.twidda.window.UserProfile;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import twitter4j.TwitterException;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class ProfileLoader extends AsyncTask<Long, Void, Boolean> {

    public enum Mode {
        LDR_PROFILE,
        ACTION_FOLLOW,
        ACTION_BLOCK,
        ACTION_MUTE
    }

    private final Mode mode;

    private WeakReference<UserProfile> ui;
    private SimpleDateFormat sdf;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private TwitterUser user;
    private NumberFormat formatter;
    private long homeId;
    private int highlight;
    private boolean imgEnabled;
    private boolean isHome;
    private boolean isFollowing;
    private boolean isFollowed;
    private boolean isBlocked;
    private boolean isMuted;
    private boolean canDm;


    /**
     * @param context Context to Activity
     * @see UserProfile
     */
    public ProfileLoader(@NonNull UserProfile context, Mode mode) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        formatter = NumberFormat.getIntegerInstance();
        sdf = settings.getDateFormatter();
        imgEnabled = settings.getImageLoad();
        homeId = settings.getUserId();
        highlight = settings.getHighlightColor();
        this.mode = mode;
    }


    @Override
    protected Boolean doInBackground(Long... args) {
        final long UID = args[0];

        DatabaseAdapter db = new DatabaseAdapter(ui.get());
        isHome = homeId == UID;
        try {
            if (mode == Mode.LDR_PROFILE) {
                user = db.getUser(UID);
                if (user != null) {
                    publishProgress();
                }
            }
            user = mTwitter.getUser(UID);
            publishProgress();
            db.storeUser(user);

            if (!isHome) {
                boolean[] connection = mTwitter.getConnection(UID);
                isFollowing = connection[0];
                isFollowed = connection[1];
                isBlocked = connection[2];
                isMuted = connection[3];
                canDm = connection[4];
            }

            switch (mode) {
                case ACTION_FOLLOW:
                    if (user.isLocked()) {
                        if (isFollowing)
                            user = mTwitter.unfollowUser(UID);
                        else if (!user.followRequested())
                            user = mTwitter.followUser(UID);
                        // TODO purge follow request
                    } else {
                        if (!isFollowing)
                            user = mTwitter.followUser(UID);
                        else
                            user = mTwitter.unfollowUser(UID);
                        isFollowing = !isFollowing;
                    }
                    publishProgress();
                    break;

                case ACTION_BLOCK:
                    if (!isBlocked)
                        user = mTwitter.blockUser(UID);
                    else
                        user = mTwitter.unblockUser(UID);
                    isBlocked = !isBlocked;
                    publishProgress();
                    break;

                case ACTION_MUTE:
                    if (!isMuted)
                        user = mTwitter.muteUser(UID);
                    else
                        user = mTwitter.unmuteUser(UID);
                    isMuted = !isMuted;
                    publishProgress();
                    break;
            }
        } catch (TwitterException err) {
            this.err = err;
            return false;
        } catch (Exception err) {
            if (err.getMessage() != null)
                Log.e("ProfileLoader", err.getMessage());
            return false;
        }
        return true;
    }


    @Override
    protected void onProgressUpdate(Void... v) {
        if (ui.get() == null) return;

        TextView txtUser = ui.get().findViewById(R.id.profile_username);
        TextView txtScrName = ui.get().findViewById(R.id.profile_screenname);
        TextView txtBio = ui.get().findViewById(R.id.bio);
        TextView txtLocation = ui.get().findViewById(R.id.location);
        TextView txtLink = ui.get().findViewById(R.id.links);
        TextView txtFollowing = ui.get().findViewById(R.id.following);
        TextView txtFollower = ui.get().findViewById(R.id.follower);
        ImageView profile = ui.get().findViewById(R.id.profile_img);
        View location_ico = ui.get().findViewById(R.id.loction_ico);
        View link_ico = ui.get().findViewById(R.id.links_ico);

        if (mode == Mode.LDR_PROFILE) {
            View profile_head = ui.get().findViewById(R.id.profile_header);
            if (profile_head.getVisibility() != VISIBLE) {
                profile_head.setVisibility(VISIBLE);

                View verified = ui.get().findViewById(R.id.profile_verify);
                View followback = ui.get().findViewById(R.id.followback);
                View locked = ui.get().findViewById(R.id.profile_locked);
                TextView txtCreated = ui.get().findViewById(R.id.profile_date);
                String date = sdf.format(new Date(user.getCreatedAt()));
                txtCreated.setText(date);

                if (user.isVerified())
                    verified.setVisibility(VISIBLE);
                if (isFollowed)
                    followback.setVisibility(VISIBLE);
                if (user.isLocked()) {
                    locked.setVisibility(VISIBLE);
                }
                profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent image = new Intent(ui.get(), ImageDetail.class);
                        image.putExtra("link", new String[]{user.getImageLink()});
                        image.putExtra("storable", true);
                        ui.get().startActivity(image);

                    }
                });
            }
        }
        Spanned bio = Tagger.makeText(user.getBio(), highlight, ui.get());
        txtBio.setMovementMethod(LinkMovementMethod.getInstance());
        txtBio.setText(bio);
        txtUser.setText(user.getUsername());
        txtScrName.setText(user.getScreenname());
        txtFollower.setText(formatter.format(user.getFollower()));
        txtFollowing.setText(formatter.format(user.getFollowing()));

        if (user.getLocation() != null && !user.getLocation().isEmpty()) {
            txtLocation.setText(user.getLocation());
            txtLocation.setVisibility(VISIBLE);
            location_ico.setVisibility(VISIBLE);
        } else {
            txtLocation.setVisibility(GONE);
            location_ico.setVisibility(GONE);
        }
        if (user.getLink() != null && !user.getLink().isEmpty()) {
            txtLink.setText(user.getLink());
            txtLink.setVisibility(VISIBLE);
            link_ico.setVisibility(VISIBLE);
        } else {
            txtLink.setVisibility(GONE);
            link_ico.setVisibility(GONE);
        }
        if (imgEnabled) {
            String link = user.getImageLink() + "_bigger";
            Picasso.get().load(link).into(profile);
        }

        ui.get().setTweetCount(user.getTweetCount(), user.getFavorCount());
    }


    @Override
    protected void onPostExecute(final Boolean success) {
        if (ui.get() == null) return;

        if (success) {
            switch (mode) {
                case ACTION_FOLLOW:
                    if (!user.isLocked())
                        if (isFollowing)
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
                ui.get().setConnection(isFollowing, isMuted, isBlocked, user.isLocked(), canDm, user.followRequested());
                ui.get().invalidateOptionsMenu();
            }
        } else {
            if (err != null) {
                boolean killActivity = ErrorHandler.printError(ui.get(), err);
                if (killActivity)
                    ui.get().finish();
            }
        }
    }
}