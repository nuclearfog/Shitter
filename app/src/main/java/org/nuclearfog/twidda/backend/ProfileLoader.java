package org.nuclearfog.twidda.backend;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.window.UserDetail;
import org.nuclearfog.twidda.window.UserProfile;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import twitter4j.TwitterException;


public class ProfileLoader extends AsyncTask<Long, Long, Long> {

    // GET USER TWEETS
    private static final long GET_USER = 1;
    public static final long GET_TWEETS = 2;
    public static final long GET_FAVORS = 3;

    // USER ACTION
    public static final long ACTION_FOLLOW = 6;
    public static final long ACTION_BLOCK = 4;
    public static final long ACTION_MUTE = 5;

    // INTERN FLAGS
    private static final long FAILURE = -1;

    private TimelineAdapter homeTl, homeFav;
    private WeakReference<UserProfile> ui;
    private SimpleDateFormat sdf;
    private TwitterEngine mTwitter;
    private DatabaseAdapter database;
    private TwitterUser user;
    private long homeId;
    private boolean imgEnabled;

    private boolean isHome = false;
    private boolean isFollowing = false;
    private boolean isFollowed = false;
    private boolean isBlocked = false;
    private boolean isMuted = false;
    private boolean canDm = false;

    private String errMsg = "E Profile Load: ";
    private int returnCode = 0;

    /**
     * @param context Context to Activity
     * @see UserProfile
     */
    public ProfileLoader(UserProfile context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        database = new DatabaseAdapter(context);
        sdf = settings.getDateFormatter();
        int font = settings.getFontColor();
        int highlight = settings.getHighlightColor();
        imgEnabled = settings.loadImages();
        homeId = settings.getUserId();
        RecyclerView profileTweets = context.findViewById(R.id.ht_list);
        RecyclerView profileFavors = context.findViewById(R.id.hf_list);
        homeTl = (TimelineAdapter) profileTweets.getAdapter();
        homeFav = (TimelineAdapter) profileFavors.getAdapter();

        if (homeTl == null) {
            homeTl = new TimelineAdapter(context);
            homeTl.setColor(highlight, font);
            homeTl.toggleImage(imgEnabled);
            profileTweets.setAdapter(homeTl);
        }
        if (homeFav == null) {
            homeFav = new TimelineAdapter(context);
            homeFav.setColor(highlight, font);
            homeFav.toggleImage(imgEnabled);
            profileFavors.setAdapter(homeFav);
        }
    }


    @Override
    protected Long doInBackground(Long... args) {
        final long UID = args[0];
        final long MODE = args[1];
        long page = 1L;
        if (args.length > 2)
            page = args[2];
        isHome = homeId == UID;

        try {
            user = database.getUser(UID);
            if (user != null)
                publishProgress(GET_USER);

            if (homeTl.getItemCount() == 0) {
                List<Tweet> tweets = database.getUserTweets(UID);
                if (!tweets.isEmpty()) {
                    homeTl.setData(tweets);
                    publishProgress(GET_TWEETS);
                }
            }
            if (homeFav.getItemCount() == 0) {
                List<Tweet> favors = database.getUserFavs(UID);
                if (!favors.isEmpty()) {
                    homeFav.setData(favors);
                    publishProgress(GET_FAVORS);
                }
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
                isFollowing = !isFollowing;
                mTwitter.followAction(UID, isFollowing);
                publishProgress(GET_USER);
            } else if (MODE == ACTION_BLOCK) {
                isBlocked = !isBlocked;
                mTwitter.blockAction(UID, isBlocked);
                publishProgress(GET_USER);
            } else if (MODE == ACTION_MUTE) {
                isMuted = !isMuted;
                mTwitter.muteAction(UID, isMuted);
                publishProgress(GET_USER);
            } else {
                boolean access = (!user.isLocked || isFollowing);
                if ((MODE == GET_TWEETS || homeTl.getItemCount() == 0) && access) {
                    long id = 1L;
                    if (homeTl.getItemCount() > 0)
                        id = homeTl.getItemId(0);

                    List<Tweet> tweets = mTwitter.getUserTweets(UID, id, page);
                    homeTl.addNew(tweets);
                    database.storeUserTweets(tweets);
                }
                publishProgress(GET_TWEETS);

                if ((MODE == GET_FAVORS || homeFav.getItemCount() == 0) && access) {
                    long id = 1L;
                    if (homeFav.getItemCount() > 0)
                        id = homeFav.getItemId(0);

                    List<Tweet> favors = mTwitter.getUserFavs(UID, id, page);
                    homeFav.addNew(favors);
                    database.storeUserFavs(favors, UID);
                }
                publishProgress(GET_FAVORS);
            }
        } catch (TwitterException err) {
            returnCode = err.getErrorCode();
            errMsg += err.getMessage();
            return FAILURE;
        } catch (Exception err) {
            err.printStackTrace();
            errMsg += err.getMessage();
            Log.e("ProfileLoader", errMsg);
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

            String follower = Integer.toString(user.follower);
            String following = Integer.toString(user.following);
            String date = sdf.format(new Date(user.created));
            txtUser.setText(user.username);
            txtScrName.setText(user.screenname);
            txtBio.setText(user.bio);
            txtFollower.setText(follower);
            txtFollowing.setText(following);
            txtCreated.setText(date);
            txtFollower.setVisibility(View.VISIBLE);
            txtFollowing.setVisibility(View.VISIBLE);
            txtCreated.setVisibility(View.VISIBLE);
            if (user.location != null && !user.location.isEmpty()) {
                txtLocation.setText(user.location);
                txtLocation.setVisibility(View.VISIBLE);
            }
            if (user.link != null && !user.link.isEmpty()) {
                txtLink.setText(user.link);
                txtLink.setVisibility(View.VISIBLE);
            }
            if (user.isVerified) {
                ui.get().findViewById(R.id.profile_verify).setVisibility(View.VISIBLE);
            }
            if (isFollowed) {
                ui.get().findViewById(R.id.followback).setVisibility(View.VISIBLE);
            }
            if (imgEnabled) {
                Picasso.get().load(user.profileImg + "_bigger").into(profile);
            }
            if (user.isLocked) {
                ui.get().findViewById(R.id.profile_locked).setVisibility(View.VISIBLE);
            } else {
                txtFollowing.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent following = new Intent(ui.get(), UserDetail.class);
                        following.putExtra("userID", user.userID);
                        following.putExtra("mode", 0);
                        ui.get().startActivity(following);
                    }
                });
                txtFollower.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent follower = new Intent(ui.get(), UserDetail.class);
                        follower.putExtra("userID", user.userID);
                        follower.putExtra("mode", 1);
                        ui.get().startActivity(follower);
                    }
                });
            }
            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getStatus() != Status.RUNNING)
                        new ImagePopup(ui.get()).execute(user.profileImg);
                }
            });

        } else if (MODE == GET_TWEETS) {
            SwipeRefreshLayout homeReload = ui.get().findViewById(R.id.hometweets);
            homeReload.setRefreshing(false);
            homeTl.notifyDataSetChanged();
        } else if (MODE == GET_FAVORS) {
            SwipeRefreshLayout favReload = ui.get().findViewById(R.id.homefavorits);
            favReload.setRefreshing(false);
            homeFav.notifyDataSetChanged();
        }
    }


    @Override
    protected void onPostExecute(final Long MODE) {
        if (ui.get() == null) return;

        if (MODE == ACTION_FOLLOW) {
            if (isFollowing)
                Toast.makeText(ui.get(), R.string.followed, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(ui.get(), R.string.unfollowed, Toast.LENGTH_SHORT).show();

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
            switch (returnCode) {
                case 420:
                    Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_LONG).show();
                    break;
                case 136:
                    break;
                case -1:
                    Toast.makeText(ui.get(), R.string.error_not_specified, Toast.LENGTH_LONG).show();
                default:
                    Toast.makeText(ui.get(), errMsg, Toast.LENGTH_LONG).show();
            }
            SwipeRefreshLayout homeReload = ui.get().findViewById(R.id.hometweets);
            SwipeRefreshLayout favReload = ui.get().findViewById(R.id.homefavorits);
            homeReload.setRefreshing(false);
            favReload.setRefreshing(false);
        }
        if (!isHome) {
            ui.get().setConnection(isFollowing, isMuted, isBlocked, canDm);
            ui.get().invalidateOptionsMenu();
        }
    }
}