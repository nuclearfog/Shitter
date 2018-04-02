package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.window.UserProfile;
import org.nuclearfog.twidda.backend.listitems.*;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import com.squareup.picasso.Picasso;

import twitter4j.TwitterException;

public class ProfileLoader extends AsyncTask<Long,Void,Long> {

    public static final long GET_INFORMATION = 0x0;
    public static final long ACTION_FOLLOW   = 0x1;
    public static final long GET_TWEETS      = 0x2;
    public static final long GET_FAVS        = 0x3;
    public static final long ACTION_MUTE     = 0x4;
    public static final long LOAD_DB         = 0x5;
    private static final long FAILURE        = 0x6;
    private static final long IGNORE         = 0x9;

    private String screenName, username, description, location, follower, following;
    private RecyclerView profileTweets, profileFavorits;
    private String /* bannerLink,*/ profileImage, link, dateString;
    private TimelineRecycler homeTl, homeFav;
    private WeakReference<UserProfile> ui;
    private TwitterEngine mTwitter;
    private String errMsg = "";
    private int font, highlight;
    private boolean isHome = false;
    private boolean imgEnabled = false;
    private boolean isFollowing = false;
    private boolean isFollowed = false;
    private boolean isVerified = false;
    private boolean isLocked = false;
    private boolean muted = false;

    /**
     * @param context Context to Activity
     * @see UserProfile
     */
    public ProfileLoader(Context context) {
        ui = new WeakReference<>((UserProfile)context);
        profileTweets = (RecyclerView) ui.get().findViewById(R.id.ht_list);
        profileFavorits = (RecyclerView) ui.get().findViewById(R.id.hf_list);
        mTwitter = TwitterEngine.getInstance(context);
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        font = settings.getInt("font_color", 0xffffffff);
        highlight = settings.getInt("highlight_color", 0xffff00ff);
        imgEnabled = settings.getBoolean("image_load",true);
    }


    @Override
    protected Long doInBackground(Long... args) {
        long userId = args[0];
        final long MODE = args[1];
        long id = 1L;
        try {
            isHome = TwitterEngine.getHomeId() == userId;
            if(!isHome)
            {
                boolean connection[] = mTwitter.getConnection(userId);
                isFollowing = connection[0];
                isFollowed = connection[1];
                muted = connection[2];
            }
            if(MODE == GET_INFORMATION || MODE == LOAD_DB)
            {
                TwitterUser user;
                if(MODE == LOAD_DB) {
                    user = new DatabaseAdapter(ui.get()).getUser(userId);
                    if(user == null)
                        return IGNORE;
                } else {
                    user = mTwitter.getUser(userId);
                    new DatabaseAdapter(ui.get()).storeUser(user);
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
                // bannerLink = user.bannerImg;
                profileImage = user.profileImg;
                Date d = new Date(user.created);
                dateString = "seit "+ DateFormat.getDateTimeInstance().format(d);
            }
            else if(MODE == GET_TWEETS)
            {
                DatabaseAdapter tweetDb = new DatabaseAdapter(ui.get());
                List<Tweet> tweets;
                homeTl = (TimelineRecycler) profileTweets.getAdapter();
                if(homeTl != null && homeTl.getItemCount() > 0) {
                    id = homeTl.getItemId(0);
                    tweets = mTwitter.getUserTweets(userId,args[2],id);
                    tweetDb.store(tweets, DatabaseAdapter.TWEET, userId);
                    tweets.addAll(homeTl.getData());
                } else {
                    tweets = new DatabaseAdapter(ui.get()).load(DatabaseAdapter.TWEET,userId);
                    if(tweets.size() == 0) {
                        tweets = mTwitter.getUserTweets(userId,args[2],id);
                        tweetDb.store(tweets, DatabaseAdapter.TWEET, userId);
                    }
                }
                homeTl = new TimelineRecycler(tweets,ui.get());
                homeTl.setColor(highlight,font);
                homeTl.toggleImage(imgEnabled);
            }
            else if(MODE == GET_FAVS)
            {
                DatabaseAdapter tweetDb = new DatabaseAdapter(ui.get());
                List<Tweet> favorits;
                homeFav = (TimelineRecycler) profileFavorits.getAdapter();
                if(homeFav != null && homeFav.getItemCount() > 0) {
                    id = homeFav.getItemId(0);
                    favorits = mTwitter.getUserFavs(userId,args[2],id);
                    tweetDb.store(favorits, DatabaseAdapter.FAVT, userId);
                    favorits.addAll(homeFav.getData());
                } else {
                    favorits = new DatabaseAdapter(ui.get()).load(DatabaseAdapter.FAVT,userId);
                    if(favorits.size() == 0) {
                        favorits = mTwitter.getUserFavs(userId,args[2],id);
                        tweetDb.store(favorits, DatabaseAdapter.FAVT, userId);
                    }
                }
                homeFav = new TimelineRecycler(favorits,ui.get());
                homeFav.setColor(highlight,font);
                homeFav.toggleImage(imgEnabled);
            }
            else if(MODE == ACTION_FOLLOW)
            {
                if(isFollowing) {
                    isFollowing = mTwitter.toggleFollow(userId);
                } else {
                    isFollowing = mTwitter.toggleFollow(userId);
                }
            }
            else if(MODE == ACTION_MUTE)
            {
                if(muted) {
                    muted = mTwitter.toggleBlock(userId);
                } else {
                    muted = mTwitter.toggleBlock(userId);
                }
            }
        } catch (TwitterException err) {
            int errCode = err.getErrorCode();
            if(errCode != 136 && errCode != -1) {
                errMsg = err.getMessage();
            }
            err.printStackTrace();
            return FAILURE;
        }
        catch(Exception err) {
            errMsg = err.getMessage();
            err.printStackTrace();
            return FAILURE;
        }
        return MODE;
    }

    @Override
    protected void onPostExecute(Long mode) {
        UserProfile connect = ui.get();
        if(connect == null)
            return;
        final Context context = connect;

        if(mode == GET_INFORMATION || mode == LOAD_DB) {
            TextView txtUser = (TextView)connect.findViewById(R.id.profile_username);
            TextView txtScrName = (TextView)connect.findViewById(R.id.profile_screenname);
            TextView txtBio = (TextView)connect.findViewById(R.id.bio);
            TextView txtLocation = (TextView)connect.findViewById(R.id.location);
            TextView txtLink = (TextView)connect.findViewById(R.id.links);
            TextView txtCreated = (TextView)connect.findViewById(R.id.profile_date);
            TextView txtFollowing = (TextView)connect.findViewById(R.id.following);
            TextView txtFollower  = (TextView)connect.findViewById(R.id.follower);
            ImageView profile  = (ImageView)connect.findViewById(R.id.profile_img);
            //ImageView banner   = (ImageView)connect.findViewById(R.id.banner);
            ImageView locationIcon = (ImageView)connect.findViewById(R.id.location_img);
            connect.findViewById(R.id.following_icon).setVisibility(View.VISIBLE);
            connect.findViewById(R.id.follower_icon).setVisibility(View.VISIBLE);

            txtUser.setText(username);
            txtScrName.setText(screenName);
            txtBio.setText(description);
            txtFollower.setText(follower);
            txtFollowing.setText(following);
            txtCreated.setText(dateString);
            if(location!= null && !location.isEmpty()) {
                txtLocation.setText(location);
                locationIcon.setVisibility(View.VISIBLE);
            }
            if(link != null && !link.isEmpty()) {
                txtLink.setText(link);
                connect.findViewById(R.id.link_img).setVisibility(View.VISIBLE);
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
                Picasso.with(context).load(profileImage+"_bigger").into(profile);
                profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ImagePopup(context).execute(profileImage);
                    }
                });
            }
        }
        else if(mode == GET_TWEETS)
        {
            profileTweets.setAdapter(homeTl);
            SwipeRefreshLayout tweetsReload = (SwipeRefreshLayout)connect.findViewById(R.id.hometweets);
            tweetsReload.setRefreshing(false);
        }
        else if(mode == GET_FAVS)
        {
            profileFavorits.setAdapter(homeFav);
            SwipeRefreshLayout favoritsReload = (SwipeRefreshLayout)connect.findViewById(R.id.homefavorits);
            favoritsReload.setRefreshing(false);
        }
        else if(mode == FAILURE)
        {
            Toast.makeText(context,"Fehler: "+errMsg,Toast.LENGTH_LONG).show();
            SwipeRefreshLayout tweetsReload = (SwipeRefreshLayout)connect.findViewById(R.id.hometweets);
            SwipeRefreshLayout favoritsReload = (SwipeRefreshLayout)connect.findViewById(R.id.homefavorits);
            tweetsReload.setRefreshing(false);
            favoritsReload.setRefreshing(false);
        }
        if(!isHome) {
            Toolbar tool = (Toolbar) connect.findViewById(R.id.profile_toolbar);
            if(isFollowing) {
                tool.getMenu().getItem(1).setIcon(R.drawable.follow_enabled);
            } else {
                tool.getMenu().getItem(1).setIcon(R.drawable.follow);
            } if(muted) {
                tool.getMenu().getItem(2).setIcon(R.drawable.block_enabled);
            } else {
                tool.getMenu().getItem(2).setIcon(R.drawable.block);
            }
        }
    }
}