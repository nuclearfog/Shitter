package org.nuclearfog.twidda.backend;

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
import org.nuclearfog.twidda.window.UserProfile;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import twitter4j.TwitterException;

public class ProfileLoader extends AsyncTask<Long, TwitterUser, Long> {

    // GET USER TWEETS
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
    private long homeId;
    private boolean imgEnabled;

    private boolean isHome = false;
    private boolean isFollowing = false;
    private boolean isFollowed = false;
    private boolean isBlocked = false;
    private boolean isMuted = false;

    private String errMsg = "E: Profile Load, ";
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
        final long userId = args[0];
        final long mode = args[1];
        long page = 1L;
        if (args.length > 2)
            page = args[2];
        isHome = homeId == userId;

        try {
            TwitterUser user = database.getUser(userId);
            if (user != null)
                publishProgress(user);

            if (homeTl.getItemCount() == 0) {
                List<Tweet> tweets = database.getUserTweets(userId);
                homeTl.setData(tweets);
                publishProgress();
            }
            if (homeFav.getItemCount() == 0) {
                List<Tweet> favors = database.getUserFavs(userId);
                homeFav.setData(favors);
                publishProgress();
            }


            user = mTwitter.getUser(userId);
            publishProgress(user);
            database.storeUser(user);

            if (!isHome) {
                boolean connection[] = mTwitter.getConnection(userId);
                isFollowing = connection[0];
                isFollowed = connection[1];
                isBlocked = connection[2];
                isMuted = connection[3];
            }

            if (mode == ACTION_FOLLOW) {
                isFollowing = !isFollowing;
                mTwitter.followAction(userId, isFollowing);
                publishProgress(user);
            } else if (mode == ACTION_BLOCK) {
                isBlocked = !isBlocked;
                mTwitter.blockAction(userId, isBlocked);
                publishProgress(user);
            } else if (mode == ACTION_MUTE) {
                isMuted = !isMuted;
                mTwitter.muteAction(userId, isMuted);
                publishProgress(user);
            } else {
                boolean access = (!user.isLocked || isFollowed);
                if ((mode == GET_TWEETS || homeTl.getItemCount() == 0) && access) {
                    long id = 1L;
                    if (homeTl.getItemCount() > 0)
                        id = homeTl.getItemId(0);

                    List<Tweet> tweets = mTwitter.getUserTweets(userId, id, page);
                    homeTl.addNew(tweets);
                    publishProgress();
                    database.storeUserTweets(tweets);
                }
                if ((mode == GET_FAVORS || homeFav.getItemCount() == 0) && access) {
                    long id = 1L;
                    if (homeFav.getItemCount() > 0)
                        id = homeFav.getItemId(0);

                    List<Tweet> favors = mTwitter.getUserFavs(userId, id, page);
                    homeFav.addNew(favors);
                    publishProgress();
                    database.storeUserFavs(favors, userId);
                }
            }
        } catch (TwitterException err) {
            returnCode = err.getErrorCode();
            if (returnCode > 0 && returnCode != 136) {
                errMsg += err.getMessage();
            }
            return FAILURE;
        } catch (Exception err) {
            Log.e("ProfileLoader", err.getMessage());
            return FAILURE;
        }
        return mode;
    }


    @Override
    protected void onProgressUpdate(TwitterUser... users) {
        if (ui.get() == null) return;
        if (users.length == 1) {
            final TwitterUser user = users[0];

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
            if (user.isLocked) {
                ui.get().findViewById(R.id.profile_locked).setVisibility(View.VISIBLE);
            }
            if (isFollowed) {
                ui.get().findViewById(R.id.followback).setVisibility(View.VISIBLE);
            }
            if (imgEnabled) {
                Picasso.get().load(user.profileImg + "_bigger").into(profile);
                profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ImagePopup(ui.get()).execute(user.profileImg);
                    }
                });
            }
        } else {
            homeTl.notifyDataSetChanged();
            SwipeRefreshLayout tweetsReload = ui.get().findViewById(R.id.hometweets);
            tweetsReload.setRefreshing(false);

            homeFav.notifyDataSetChanged();
            SwipeRefreshLayout favorReload = ui.get().findViewById(R.id.homefavorits);
            favorReload.setRefreshing(false);
        }
    }


    @Override
    protected void onPostExecute(Long mode) {
        if (ui.get() == null) return;

        if (mode == ACTION_FOLLOW) {
            if (isFollowing)
                Toast.makeText(ui.get(), R.string.followed, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(ui.get(), R.string.unfollowed, Toast.LENGTH_SHORT).show();

        } else if (mode == ACTION_BLOCK) {
            if (isBlocked)
                Toast.makeText(ui.get(), R.string.blocked, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(ui.get(), R.string.unblocked, Toast.LENGTH_SHORT).show();

        } else if (mode == ACTION_MUTE) {
            if (isMuted)
                Toast.makeText(ui.get(), R.string.muted, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(ui.get(), R.string.unmuted, Toast.LENGTH_SHORT).show();

        } else if (mode == FAILURE) {
            SwipeRefreshLayout tweetsReload = ui.get().findViewById(R.id.hometweets);
            SwipeRefreshLayout favoriteReload = ui.get().findViewById(R.id.homefavorits);
            tweetsReload.setRefreshing(false);
            favoriteReload.setRefreshing(false);

            if (returnCode == 420) {
                Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_LONG).show();
            } else if (returnCode > 0 && returnCode != 136) {
                Toast.makeText(ui.get(), errMsg, Toast.LENGTH_LONG).show();
            }
        }
        if (!isHome) {
            ui.get().setConnection(isFollowing, isMuted, isBlocked);
            ui.get().invalidateOptionsMenu();
        }
    }
}