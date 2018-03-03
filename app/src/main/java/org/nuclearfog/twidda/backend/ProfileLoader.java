package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.window.UserProfile;

import java.text.SimpleDateFormat;
import java.util.Date;
import twitter4j.User;

import com.squareup.picasso.Picasso;

public class ProfileLoader extends AsyncTask<Long,Void,Long> {

    public static final long GET_INFORMATION = 0x0;
    public static final long ACTION_FOLLOW   = 0x1;
    public static final long GET_TWEETS      = 0x2;
    public static final long GET_FAVS        = 0x3;
    public static final long ACTION_MUTE     = 0x4;
    private static final long FAILURE        = 0x6;

    private String screenName, username, description, location, follower, following;
    private TextView txtUser,txtScrName,txtBio,txtLocation,txtLink,txtFollowing,txtFollower,txtCreated;
    private ImageView profile, banner, linkIcon, locationIcon, verifier, locked, followback;
    private SwipeRefreshLayout tweetsReload, favoritsReload;
    private ListView profileTweets, profileFavorits;
    private String imageLink, bannerLink, fullPbLink, link, dateString;
    private TimelineAdapter homeTl, homeFav;
    private Context context;
    private Toolbar tool;
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
        this.context=context;
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        imgEnabled = settings.getBoolean("image_load",true);
    }

    @Override
    protected void onPreExecute() {
        txtUser  = (TextView)((UserProfile)context).findViewById(R.id.profile_username);
        txtScrName = (TextView)((UserProfile)context).findViewById(R.id.profile_screenname);
        txtBio = (TextView)((UserProfile)context).findViewById(R.id.bio);
        txtLocation = (TextView)((UserProfile)context).findViewById(R.id.location);
        txtLink = (TextView)((UserProfile)context).findViewById(R.id.links);
        txtCreated = (TextView)((UserProfile)context).findViewById(R.id.profile_date);
        txtFollowing = (TextView)((UserProfile)context).findViewById(R.id.following);
        txtFollower  = (TextView)((UserProfile)context).findViewById(R.id.follower);
        profile  = (ImageView)((UserProfile)context).findViewById(R.id.profile_img);
        banner   = (ImageView)((UserProfile)context).findViewById(R.id.banner);
        linkIcon = (ImageView)((UserProfile)context).findViewById(R.id.link_img);
        verifier = (ImageView)((UserProfile)context).findViewById(R.id.profile_verify);
        followback = (ImageView)((UserProfile)context).findViewById(R.id.followback);
        locked = (ImageView)((UserProfile)context).findViewById(R.id.profile_locked);
        locationIcon = (ImageView)((UserProfile)context).findViewById(R.id.location_img);
        tweetsReload    = (SwipeRefreshLayout)((UserProfile)context).findViewById(R.id.hometweets);
        favoritsReload  = (SwipeRefreshLayout)((UserProfile)context).findViewById(R.id.homefavorits);
        profileTweets   = (ListView)((UserProfile)context).findViewById(R.id.ht_list);
        profileFavorits = (ListView)((UserProfile)context).findViewById(R.id.hf_list);
        tool = (Toolbar) ((UserProfile)context).findViewById(R.id.profile_toolbar);
    }

    @Override
    protected Long doInBackground(Long... args) {
        long userId = args[0];
        final long MODE = args[1];
        long id = 1L;
        TwitterEngine mTwitter = TwitterEngine.getInstance(context);
        try {
            isHome = TwitterEngine.getHomeId() == userId;
            if(!isHome)
            {
                isFollowing = mTwitter.getConnection(userId, true);
                isFollowed  = mTwitter.getConnection(userId, false);
                muted = mTwitter.getBlocked(userId);
            }
            if(MODE == GET_INFORMATION)
            {
                User user = mTwitter.getUser(userId);
                screenName = '@'+user.getScreenName();
                username = user.getName();
                description = user.getDescription();
                location = user.getLocation();
                isVerified = user.isVerified();
                isLocked = user.isProtected();
                link = user.getURL();
                follower = "Follower: "+user.getFollowersCount();
                following = "Following: "+user.getFriendsCount();
                imageLink = user.getProfileImageURL();
                bannerLink = user.getProfileBannerMobileURL();
                fullPbLink = user.getOriginalProfileImageURL();
                Date d = user.getCreatedAt();
                dateString = "seit "+new SimpleDateFormat("dd.MM.yyyy").format(d);
            }
            else if(MODE == GET_TWEETS)
            {
                homeTl = (TimelineAdapter) profileTweets.getAdapter();
                if(homeTl == null || homeTl.getCount() == 0) {
                    TweetDatabase hTweets = new TweetDatabase(mTwitter.getUserTweets(userId,args[2],id),context,TweetDatabase.USER_TL,userId);
                    homeTl = new TimelineAdapter(context,hTweets);
                } else {
                    id = homeTl.getItemId(0);
                    homeTl.getData().add(mTwitter.getUserTweets(userId,args[2],id));
                }
            }
            else if(MODE == GET_FAVS)
            {
                homeFav = (TimelineAdapter) profileFavorits.getAdapter();
                if(homeFav != null) {
                    id = homeFav.getItemId(0);
                    homeFav.getData().add(mTwitter.getUserFavs(userId,args[2],id));
                } else {
                    TweetDatabase fTweets = new TweetDatabase(mTwitter.getUserFavs(userId,args[2],id),context,TweetDatabase.FAV_TL,userId);
                    homeFav = new TimelineAdapter(context,fTweets);
                }
            }
            else if(MODE == ACTION_FOLLOW)
            {
                if(isFollowing)
                    isFollowing = mTwitter.toggleFollow(userId);
                else
                    isFollowing = mTwitter.toggleFollow(userId);
            }
            else if(MODE == ACTION_MUTE)
            {
                if(muted)
                    muted = mTwitter.toggleBlock(userId);
                else
                    muted = mTwitter.toggleBlock(userId);
            }
        } catch(Exception err) {
            err.printStackTrace();
            return FAILURE;
        }
        return MODE;
    }

    @Override
    protected void onPostExecute(Long mode) {
        if(mode == GET_INFORMATION) {
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
                linkIcon.setVisibility(View.VISIBLE);
            }
            if(isVerified) {
                verifier.setVisibility(View.VISIBLE);
            }
            if(isLocked) {
                locked.setVisibility(View.VISIBLE);
            }
            if(isFollowed) {
                followback.setVisibility(View.VISIBLE);
            }
            if(imgEnabled) {
                Picasso.with(context).load(imageLink).into(profile);
              //  Picasso.with(context).load(bannerLink).into(banner); // TODO
                profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ImagePopup(context).execute(fullPbLink);
                    }
                });
            }
        }
        else if(mode == GET_TWEETS)
        {
            if(profileTweets.getAdapter() == null)
                profileTweets.setAdapter(homeTl);
            else
                homeTl.notifyDataSetChanged();
            tweetsReload.setRefreshing(false);
        }
        else if(mode == GET_FAVS)
        {
            if(profileFavorits.getAdapter() == null)
                profileFavorits.setAdapter(homeFav);
            else
                homeFav.notifyDataSetChanged();
            favoritsReload.setRefreshing(false);
        }
        else if(mode == FAILURE)
        {
            Toast.makeText(context,"Fehler beim Laden des Profils",Toast.LENGTH_LONG).show();
            tweetsReload.setRefreshing(false);
            favoritsReload.setRefreshing(false);
        }
        if(!isHome) {
            if(isFollowing)
                tool.getMenu().getItem(1).setIcon(R.drawable.follow_enabled);
            else
                tool.getMenu().getItem(1).setIcon(R.drawable.follow);
            if(muted)
                tool.getMenu().getItem(2).setIcon(R.drawable.block_enabled);
            else
                tool.getMenu().getItem(2).setIcon(R.drawable.block);
        }
    }
}