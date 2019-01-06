package org.nuclearfog.twidda.backend;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
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
import org.nuclearfog.twidda.adapter.TimelineAdapter;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.window.UserDetail;
import org.nuclearfog.twidda.window.UserProfile;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import twitter4j.TwitterException;

public class ProfileLoader extends AsyncTask<Long, Long, Long> {

    public static final long GET_TWEETS = 1;
    public static final long GET_FAVORS = 2;
    // USER ACTION
    public static final long ACTION_FOLLOW = 3;
    public static final long ACTION_BLOCK = 4;
    public static final long ACTION_MUTE = 5;
    // INTERN FLAGS
    private static final long GET_USER = 6;
    private static final long FAILURE = -1;

    private TimelineAdapter homeTl, homeFav;
    private WeakReference<UserProfile> ui;
    private SimpleDateFormat sdf;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private DatabaseAdapter database;
    private TwitterUser user;
    private List<Tweet> tweets, favors;
    private NumberFormat formatter;
    private long homeId;
    private int highlight;
    private boolean imgEnabled;

    private boolean isHome = false;
    private boolean isFollowing = false;
    private boolean isFollowed = false;
    private boolean isBlocked = false;
    private boolean isMuted = false;
    private boolean canDm = false;


    /**
     * @param context Context to Activity
     * @see UserProfile
     */
    public ProfileLoader(UserProfile context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        formatter = NumberFormat.getIntegerInstance();
        database = new DatabaseAdapter(context);
        sdf = settings.getDateFormatter();
        imgEnabled = settings.loadImages();
        homeId = settings.getUserId();
        highlight = settings.getHighlightColor();

        tweets = new ArrayList<>();
        favors = new ArrayList<>();

        RecyclerView profileTweets = context.findViewById(R.id.ht_list);
        RecyclerView profileFavors = context.findViewById(R.id.hf_list);
        homeTl = (TimelineAdapter) profileTweets.getAdapter();
        homeFav = (TimelineAdapter) profileFavors.getAdapter();
    }


    @Override
    protected Long doInBackground(Long... args) {
        final long UID = args[0];
        final long MODE = args[1];
        long page = 1L;
        long sinceId = 1L;
        if (args.length > 2)
            page = args[2];
        isHome = homeId == UID;

        try {
            user = database.getUser(UID);
            if (user != null)
                publishProgress(GET_USER);

            if (homeTl.getItemCount() == 0) {
                tweets = database.getUserTweets(UID);
                if (!tweets.isEmpty())
                    publishProgress(GET_TWEETS);
            }
            if (homeFav.getItemCount() == 0) {
                favors = database.getUserFavs(UID);
                if (!favors.isEmpty())
                    publishProgress(GET_FAVORS);
            }

            user = mTwitter.getUser(UID);
            if (!isHome) {
                boolean connection[] = mTwitter.getConnection(UID);
                isFollowing = connection[0];
                isFollowed = connection[1];
                isBlocked = connection[2];
                isMuted = connection[3];
                canDm = connection[4];
            }
            publishProgress(GET_USER);
            database.storeUser(user);

            if (MODE == ACTION_FOLLOW) {
                if (user.isLocked()) {
                    if (isFollowing) {
                        isFollowing = false;
                        user = mTwitter.followAction(UID, false);
                    } else if (user.followRequested()) {
                        user = mTwitter.followAction(UID, false);
                    } else {
                        user = mTwitter.followAction(UID, true);
                    }
                } else {
                    isFollowing = !isFollowing;
                    user = mTwitter.followAction(UID, isFollowing);
                }
                publishProgress(GET_USER);
            } else if (MODE == ACTION_BLOCK) {
                isBlocked = !isBlocked;
                user = mTwitter.blockAction(UID, isBlocked);
                publishProgress(GET_USER);
            } else if (MODE == ACTION_MUTE) {
                isMuted = !isMuted;
                user = mTwitter.muteAction(UID, isMuted);
                publishProgress(GET_USER);
            } else {
                if (!user.isLocked() || isFollowing) {
                    if ((MODE == GET_TWEETS || homeTl.getItemCount() == 0)) {
                        if (homeTl.getItemCount() > 0)
                            sinceId = homeTl.getItemId(0);
                        tweets = mTwitter.getUserTweets(UID, sinceId, page);
                        database.storeUserTweets(tweets);
                        publishProgress(GET_TWEETS);
                    }
                    if ((MODE == GET_FAVORS || homeFav.getItemCount() == 0)) {
                        if (homeFav.getItemCount() > 0)
                            sinceId = homeFav.getItemId(0);
                        favors = mTwitter.getUserFavs(UID, sinceId, page);
                        database.storeUserFavs(favors, UID);
                        publishProgress(GET_FAVORS);
                    }
                }
            }
        } catch (TwitterException err) {
            this.err = err;
            return FAILURE;
        } catch (Exception err) {
            Log.e("ProfileLoader", err.getMessage());
            return FAILURE;
        }
        return MODE;
    }


    @Override
    protected void onProgressUpdate(Long... mode) {
        if (ui.get() == null) return;

        final long MODE = mode[0];
        if (MODE == GET_USER) {
            TextView txtUser = ui.get().findViewById(R.id.profile_username);
            TextView txtScrName = ui.get().findViewById(R.id.profile_screenname);
            TextView txtBio = ui.get().findViewById(R.id.bio);
            TextView txtLocation = ui.get().findViewById(R.id.location);
            TextView txtLink = ui.get().findViewById(R.id.links);
            TextView txtCreated = ui.get().findViewById(R.id.profile_date);
            TextView txtFollowing = ui.get().findViewById(R.id.following);
            TextView txtFollower = ui.get().findViewById(R.id.follower);
            ImageView profile = ui.get().findViewById(R.id.profile_img);

            View following_ico = ui.get().findViewById(R.id.following_ico);
            View follower_ico = ui.get().findViewById(R.id.follower_ico);
            View location_ico = ui.get().findViewById(R.id.loction_ico);
            View link_ico = ui.get().findViewById(R.id.links_ico);
            View date_ico = ui.get().findViewById(R.id.date_ico);

            String date = sdf.format(new Date(user.getCreatedAt()));
            Spanned bio = Tagger.makeText(user.getBio(), highlight, ui.get());
            txtBio.setMovementMethod(LinkMovementMethod.getInstance());
            txtBio.setText(bio);
            txtUser.setText(user.getUsername());
            txtScrName.setText(user.getScreenname());
            txtFollower.setText(formatter.format(user.getFollower()));
            txtFollowing.setText(formatter.format(user.getFollowing()));
            txtCreated.setText(date);

            follower_ico.setVisibility(View.VISIBLE);
            following_ico.setVisibility(View.VISIBLE);
            date_ico.setVisibility(View.VISIBLE);

            if (user.getLocation() != null && !user.getLocation().isEmpty()) {
                txtLocation.setText(user.getLocation());
                location_ico.setVisibility(View.VISIBLE);
            }
            if (user.getLink() != null && !user.getLink().isEmpty()) {
                txtLink.setText(user.getLink());
                link_ico.setVisibility(View.VISIBLE);
            }
            if (user.isVerified()) {
                ui.get().findViewById(R.id.profile_verify).setVisibility(View.VISIBLE);
            }
            if (isFollowed) {
                ui.get().findViewById(R.id.followback).setVisibility(View.VISIBLE);
            }
            if (imgEnabled) {
                String link = user.getImageLink() + "_bigger";
                Picasso.get().load(link).into(profile);
            }
            if (user.isLocked()) {
                ui.get().findViewById(R.id.profile_locked).setVisibility(View.VISIBLE);
            } else {
                txtFollowing.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent following = new Intent(ui.get(), UserDetail.class);
                        following.putExtra("userID", user.getId());
                        following.putExtra("mode", 0);
                        ui.get().startActivity(following);
                    }
                });
                txtFollower.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent follower = new Intent(ui.get(), UserDetail.class);
                        follower.putExtra("userID", user.getId());
                        follower.putExtra("mode", 1);
                        ui.get().startActivity(follower);
                    }
                });
            }
            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ui.get().imageClick(user.getImageLink());
                }
            });

            ui.get().setTweetCount(user.getTweetCount(), user.getFavorCount());

        } else if (MODE == GET_TWEETS) {
            homeTl.setData(tweets);
            homeTl.notifyDataSetChanged();
            SwipeRefreshLayout homeReload = ui.get().findViewById(R.id.hometweets);
            homeReload.setRefreshing(false);
        } else if (MODE == GET_FAVORS) {
            homeFav.setData(favors);
            homeFav.notifyDataSetChanged();
            SwipeRefreshLayout favReload = ui.get().findViewById(R.id.homefavorits);
            favReload.setRefreshing(false);
        }
    }


    @Override
    protected void onPostExecute(final Long MODE) {
        if (ui.get() == null) return;

        SwipeRefreshLayout homeReload = ui.get().findViewById(R.id.hometweets);
        SwipeRefreshLayout favReload = ui.get().findViewById(R.id.homefavorits);
        homeReload.setRefreshing(false);
        favReload.setRefreshing(false);

        if (MODE == ACTION_FOLLOW) {
            if (!user.isLocked()) {
                if (isFollowing)
                    Toast.makeText(ui.get(), R.string.followed, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(ui.get(), R.string.unfollowed, Toast.LENGTH_SHORT).show();
            }
        } else if (MODE == ACTION_BLOCK) {
            if (isBlocked)
                Toast.makeText(ui.get(), R.string.blocked, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(ui.get(), R.string.unblocked, Toast.LENGTH_SHORT).show();

        } else if (MODE == ACTION_MUTE) {
            if (isMuted)
                Toast.makeText(ui.get(), R.string.muted, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(ui.get(), R.string.unmuted, Toast.LENGTH_SHORT).show();

        } else if (MODE == FAILURE) {
            if (err != null) {
                boolean killActivity = ErrorHandling.printError(ui.get(), err);
                if (killActivity)
                    ui.get().finish();
            }
        }
        if (!isHome) {
            ui.get().setConnection(isFollowing, isMuted, isBlocked, canDm, user.followRequested());
            ui.get().invalidateOptionsMenu();
        }
    }


    @Override
    protected void onCancelled(Long mode) {
        if (ui.get() == null) return;

        SwipeRefreshLayout homeReload = ui.get().findViewById(R.id.hometweets);
        SwipeRefreshLayout favReload = ui.get().findViewById(R.id.homefavorits);
        homeReload.setRefreshing(false);
        favReload.setRefreshing(false);
    }
}