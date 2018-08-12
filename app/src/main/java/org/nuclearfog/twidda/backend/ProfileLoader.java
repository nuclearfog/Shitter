package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.ErrorLog;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.window.UserProfile;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import twitter4j.TwitterException;

public class ProfileLoader extends AsyncTask<Long,Void,Long> {

    public static final long GET_INFORMATION = 0; //Profilinformation
    public static final long ACTION_FOLLOW   = 1; // Folgen/Entfolgen
    public static final long GET_TWEETS      = 2; // Tweets Laden
    public static final long GET_FAVS        = 3; // Favoriten Laden
    public static final long ACTION_MUTE     = 4;
    public static final long LOAD_DB         = 5;
    private static final long FAILURE        = 6;
    private static final long IGNORE         = 7;

    private String screenName, username, description, location, follower, following;
    private String profileImage, link, dateString;
    private TimelineRecycler homeTl, homeFav;
    private WeakReference<UserProfile> ui;
    private SimpleDateFormat sdf;
    private TwitterEngine mTwitter;
    private ErrorLog errorLog;
    private long homeId;
    private boolean imgEnabled;
    private boolean isHome = false;
    private boolean isFollowing = false;
    private boolean isFollowed = false;
    private boolean isVerified = false;
    private boolean isLocked = false;
    private boolean blocked = false;
    private String errMsg = "E: Profile Load, ";
    private int returnCode = 0;

    /**
     * @param context Context to Activity
     * @see UserProfile
     */
    public ProfileLoader(Context context) {
        ui = new WeakReference<>((UserProfile)context);
        mTwitter = TwitterEngine.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        sdf = settings.getDateFormatter();
        errorLog = new ErrorLog(ui.get());
        int font = settings.getFontColor();
        int highlight = settings.getHighlightColor();
        imgEnabled = settings.loadImages();
        homeId = settings.getUserId();
        RecyclerView profileTweets = ui.get().findViewById(R.id.ht_list);
        RecyclerView profileFavorits = ui.get().findViewById(R.id.hf_list);
        homeTl = (TimelineRecycler) profileTweets.getAdapter();
        homeFav = (TimelineRecycler) profileFavorits.getAdapter();

        if(homeTl == null) {
            homeTl = new TimelineRecycler(ui.get());
            homeTl.setColor(highlight, font);
            homeTl.toggleImage(imgEnabled);
            profileTweets.setAdapter(homeTl);
        }
        if(homeFav == null) {
            homeFav = new TimelineRecycler(ui.get());
            homeFav.setColor(highlight, font);
            homeFav.toggleImage(imgEnabled);
            profileFavorits.setAdapter(homeFav);
        }
    }


    @Override
    protected Long doInBackground(Long... args) {
        long userId = args[0];
        final long MODE = args[1];
        long id = 1L;
        try {
            isHome = homeId == userId;
            if(!isHome && (MODE==ACTION_FOLLOW||MODE==ACTION_MUTE||MODE==GET_INFORMATION))
            {
                boolean connection[] = mTwitter.getConnection(userId);
                isFollowing = connection[0];
                isFollowed = connection[1];
                blocked = connection[2];
            }

            TwitterUser user;
            DatabaseAdapter database = new DatabaseAdapter(ui.get());
            if(MODE == GET_INFORMATION) {
                user = mTwitter.getUser(userId);
                database.storeUser(user);
            } else {
                user = database.getUser(userId);
                if(user == null)
                    return IGNORE;
            }
            screenName = user.screenname;
            username = user.username;
            description = user.bio;
            location = user.location;
            isVerified = user.isVerified;
            isLocked = user.isLocked;
            link = user.link;
            follower = Integer.toString(user.follower);
            following = Integer.toString(user.following);
            profileImage = user.profileImg;
            Date time = new Date(user.created);
            dateString = "seit " + sdf.format(time);
            description = description.replace('\n', ' ');

            if (MODE == GET_TWEETS && !isLocked)
            {
                List<Tweet> tweets;

                if(homeTl.getItemCount() > 0) {
                    id = homeTl.getItemId(0);
                    tweets = mTwitter.getUserTweets(userId,args[2],id);
                    database.storeUserTweets(tweets);
                    tweets.addAll(homeTl.getData());
                } else {
                    tweets = database.getUserTweets(userId);
                    if(tweets.size() == 0 && !isLocked) {
                        tweets = mTwitter.getUserTweets(userId,args[2],id);
                        database.storeUserTweets(tweets);
                    }
                }
                homeTl.setData(tweets);
            } else if (MODE == GET_FAVS && !isLocked)
            {
                List<Tweet> favorits;
                if(homeFav.getItemCount() > 0) {
                    id = homeFav.getItemId(0);
                    favorits = mTwitter.getUserFavs(userId,args[2],id);
                    database.storeUserFavs(favorits,userId);
                    favorits.addAll(homeFav.getData());

                } else {
                    favorits = database.getUserFavs(userId);
                    if(favorits.size() == 0 && !isLocked) {
                        favorits = mTwitter.getUserFavs(userId,args[2],id);
                        database.storeUserFavs(favorits,userId);
                    }
                }
                homeFav.setData(favorits);
            }
            else if(MODE == ACTION_FOLLOW)
            {
                if(isFollowing) {
                    mTwitter.followAction(userId,false);
                    isFollowing = false;
                } else {
                    mTwitter.followAction(userId,true);
                    isFollowing = true;
                }
            }
            else if(MODE == ACTION_MUTE)
            {
                if(blocked) {
                    mTwitter.blockAction(userId,false);
                    blocked = false;
                } else {
                    mTwitter.blockAction(userId,true);
                    blocked = true;
                }
            }
        } catch (TwitterException err) {
            returnCode = err.getErrorCode();

            if (returnCode != 136) {
                errMsg += err.getMessage();
                errorLog.add(errMsg);
            }
            return FAILURE;
        }
        catch(Exception err) {
            errMsg += err.getMessage();
            errorLog.add(errMsg);
            return FAILURE;
        }
        return MODE;
    }

    @Override
    protected void onPostExecute(Long mode) {
        final UserProfile connect = ui.get();
        if(connect == null)
            return;

        if(mode == GET_INFORMATION || mode == LOAD_DB) {
            TextView txtUser = connect.findViewById(R.id.profile_username);
            TextView txtScrName = connect.findViewById(R.id.profile_screenname);
            TextView txtBio = connect.findViewById(R.id.bio);
            TextView txtLocation = connect.findViewById(R.id.location);
            TextView txtLink = connect.findViewById(R.id.links);
            TextView txtCreated = connect.findViewById(R.id.profile_date);
            TextView txtFollowing = connect.findViewById(R.id.following);
            TextView txtFollower  = connect.findViewById(R.id.follower);
            ImageView profile = connect.findViewById(R.id.profile_img);

            txtUser.setText(username);
            txtScrName.setText(screenName);
            txtBio.setText(description);
            txtFollower.setText(follower);
            txtFollowing.setText(following);
            txtCreated.setText(dateString);
            txtFollower.setVisibility(View.VISIBLE);
            txtFollowing.setVisibility(View.VISIBLE);
            txtCreated.setVisibility(View.VISIBLE);
            if(location!= null && !location.isEmpty()) {
                txtLocation.setText(location);
                txtLocation.setVisibility(View.VISIBLE);
            }
            if(link != null && !link.isEmpty()) {
                txtLink.setText(link);
                txtLink.setVisibility(View.VISIBLE);
            }
            if(isVerified) {
                connect.findViewById(R.id.profile_verify).setVisibility(View.VISIBLE);
            }
            if(isLocked) {
                connect.findViewById(R.id.profile_locked).setVisibility(View.VISIBLE);
            }
            if(isFollowed) {
                connect.findViewById(R.id.followback).setVisibility(View.VISIBLE);
            }
            if(imgEnabled) {
                Picasso.with(connect).load(profileImage+"_bigger").into(profile);
                profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ImagePopup(connect).execute(profileImage);
                    }
                });
            }
        }
        else if(mode == GET_TWEETS)
        {
            homeTl.notifyDataSetChanged();
            SwipeRefreshLayout tweetsReload = connect.findViewById(R.id.hometweets);
            tweetsReload.setRefreshing(false);
        }
        else if(mode == GET_FAVS)
        {
            homeFav.notifyDataSetChanged();
            SwipeRefreshLayout favoritsReload = connect.findViewById(R.id.homefavorits);
            favoritsReload.setRefreshing(false);
        }
        else if(mode == ACTION_FOLLOW) {
            int textId;
            if(isFollowing)
                textId = R.string.followed;
            else
                textId = R.string.unfollowed;
            Toast.makeText(ui.get(), textId, Toast.LENGTH_SHORT).show();
        }
        else if(mode == ACTION_MUTE) {
            int textId;
            if(blocked)
                textId = R.string.blocked;
            else
                textId = R.string.unblocked;
            Toast.makeText(ui.get(), textId, Toast.LENGTH_SHORT).show();
        }
        else if(mode == FAILURE)
        {
            SwipeRefreshLayout tweetsReload = connect.findViewById(R.id.hometweets);
            SwipeRefreshLayout favoriteReload = connect.findViewById(R.id.homefavorits);
            tweetsReload.setRefreshing(false);
            favoriteReload.setRefreshing(false);

            if (returnCode == 420) {
                Toast.makeText(connect, R.string.rate_limit_exceeded, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(connect, errMsg, Toast.LENGTH_LONG).show();
            }
        }
        if(!isHome && (mode==ACTION_FOLLOW||mode==ACTION_MUTE||mode==GET_INFORMATION)) {
            Toolbar tool = connect.findViewById(R.id.profile_toolbar);
            if(tool.getMenu().size() >= 2) {
                MenuItem followIcon = tool.getMenu().getItem(1);
                MenuItem blockIcon = tool.getMenu().getItem(2);
                if (isFollowing) {
                    followIcon.setIcon(R.drawable.follow_enabled);
                    followIcon.setTitle(R.string.unfollow);
                } else {
                    followIcon.setIcon(R.drawable.follow);
                    followIcon.setTitle(R.string.follow);
                }
                if (blocked) {
                    blockIcon.setIcon(R.drawable.block_enabled);
                    blockIcon.setTitle(R.string.unblock);
                    followIcon.setVisible(false);
                } else {
                    blockIcon.setIcon(R.drawable.block);
                    blockIcon.setTitle(R.string.block);
                    followIcon.setVisible(true);
                }
            }
        }
    }
}