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

import java.util.List;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.User;

public class ProfileAction extends AsyncTask<Long,Void,Long>
{
    public static final long GET_INFORMATION = 0x0;
    public static final long ACTION_FOLLOW   = 0x1;
    public static final long GET_TWEETS      = 0x2;
    public static final long GET_FAVS        = 0x3;
    public static final long ACTION_MUTE     = 0x4;
    private static final long FAILURE        = 0x6;

    private String screenName, username, description, location, follower, following;
    private TextView txtUser,txtScrName,txtBio,txtLocation,txtLink,txtFollowing,txtFollower;
    private ImageView profile, banner, linkIcon, locationIcon;
    private SwipeRefreshLayout tweetsReload, favoritsReload;
    private ListView profileTweets, profileFavorits;
    private String imageLink, bannerLink, link;
    private TimelineAdapter homeTl, homeFav;
    private Context context;
    private Toolbar tool;
    private boolean imgEnabled = false;
    private boolean isFollowing = false;
    private boolean  isFollowed = false;
    private boolean muted = false;
    private boolean isHome = false;
    private int load;
    private long homeUserID;

    public ProfileAction(Context context, Toolbar tool) {
        this.context=context;
        this.tool = tool;
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        imgEnabled = settings.getBoolean("image_load",false);
        load = settings.getInt("preload", 10);
        homeUserID = settings.getLong("userID", -1);
    }

    @Override
    protected void onPreExecute() {
        txtUser  = (TextView) ((UserProfile)context).findViewById(R.id.profile_username);
        txtScrName = (TextView) ((UserProfile)context).findViewById(R.id.profile_screenname);
        txtBio = (TextView)((UserProfile)context).findViewById(R.id.bio);
        txtLocation = (TextView)((UserProfile)context).findViewById(R.id.location);
        txtLink = (TextView)((UserProfile)context).findViewById(R.id.links);
        txtFollowing = (TextView)((UserProfile)context).findViewById(R.id.following);
        txtFollower  = (TextView)((UserProfile)context).findViewById(R.id.follower);
        profile  = (ImageView)((UserProfile)context).findViewById(R.id.profile_img);
        banner   = (ImageView)((UserProfile)context).findViewById(R.id.banner);
        linkIcon = (ImageView) ((UserProfile)context).findViewById(R.id.link_img);
        locationIcon = (ImageView) ((UserProfile)context).findViewById(R.id.location_img);
        tweetsReload    = (SwipeRefreshLayout)((UserProfile)context).findViewById(R.id.hometweets);
        favoritsReload  = (SwipeRefreshLayout)((UserProfile)context).findViewById(R.id.homefavorits);
        profileTweets   = (ListView)((UserProfile)context).findViewById(R.id.ht_list);
        profileFavorits = (ListView)((UserProfile)context).findViewById(R.id.hf_list);
    }

    /**
     * @param args [0] Twitter User ID
     * @see #GET_INFORMATION
     * @see #ACTION_FOLLOW
     * @see #GET_TWEETS
     * @see #GET_FAVS
     * @see #ACTION_MUTE
     */
    @Override
    protected Long doInBackground(Long... args) {
        long userId = args[0];
        final long MODE = args[1];
        isHome = userId == homeUserID;
        TwitterResource mTwitter = TwitterResource.getInstance(context);
        Twitter twitter = mTwitter.getTwitter();
        Paging p = new Paging();
        p.setCount(load);
        try {
            if(!isHome)
            {
                isFollowing = twitter.showFriendship(homeUserID,userId).isSourceFollowingTarget();
                isFollowed  = twitter.showFriendship(homeUserID,userId).isTargetFollowingSource();
                muted = twitter.showFriendship(homeUserID,userId).isSourceMutingTarget();
            }
            if(MODE == GET_INFORMATION)
            {
                User user = twitter.showUser(userId);
                screenName = '@'+ user.getScreenName();
                username = user.getName();
                description = user.getDescription();
                location = user.getLocation();
                link = user.getURL();
                follower = "Follower: "+ user.getFollowersCount();
                following = "Following: "+user.getFriendsCount();
                imageLink = user.getProfileImageURL();
                bannerLink = user.getProfileBannerURL();
            }
            else if(MODE == GET_TWEETS)
            {
                List<twitter4j.Status> tweets = twitter.getUserTimeline(userId,p);
                TweetDatabase hTweets = new TweetDatabase(tweets,context,TweetDatabase.USER_TL,userId);
                homeTl = new TimelineAdapter(context,hTweets);
            }
            else if(MODE == GET_FAVS)
            {
                List<twitter4j.Status> favorits = twitter.getFavorites(userId,p);
                TweetDatabase fTweets = new TweetDatabase(favorits,context,TweetDatabase.FAV_TL,userId);
                homeFav = new TimelineAdapter(context,fTweets);
            }
            else if(MODE == ACTION_FOLLOW)
            {
                if(isFollowing) {
                    twitter.destroyFriendship(userId);
                    isFollowing = false;
                } else {
                    twitter.createFriendship(userId);
                    isFollowing = true;
                }
            }
            else if(MODE == ACTION_MUTE)
            {
                if(muted) {
                    twitter.destroyMute(userId);
                    muted = false;
                } else {
                    twitter.createMute(userId);
                    muted = true;
                }
            }
        } catch(Exception err) {
            err.printStackTrace();
            return FAILURE;
        }
        return MODE;
    }

    @Override
    protected void onPostExecute(Long mode) {
        if(mode == GET_INFORMATION)
        {
            ImageDownloader profileImg, bannerImg;
            txtUser.setText(username);
            txtScrName.setText(screenName);
            txtBio.setText(description);
            txtFollower.setText(follower);
            txtFollowing.setText(following);
            if(location!= null) {
                txtLocation.setText(location);
                locationIcon.setVisibility(View.VISIBLE);
            }
            if(link != null) {
                txtLink.setText(link);
                linkIcon.setVisibility(View.VISIBLE);
            }
            profileImg = new ImageDownloader(profile);
            bannerImg = new ImageDownloader(banner);
            if(imgEnabled) {
                profileImg.execute(imageLink);
                bannerImg.execute(bannerLink);
            } else {
                profile.setImageResource(R.mipmap.pb);
            }
        }
        else if(mode == GET_TWEETS)
        {
            profileTweets.setAdapter(homeTl);
            tweetsReload.setRefreshing(false);
        }
        else if(mode == GET_FAVS)
        {
            profileFavorits.setAdapter(homeFav);
            favoritsReload.setRefreshing(false);
        }
        else if(mode == FAILURE)
        {
            Toast.makeText(context,"Fehler beim Laden des Profils",Toast.LENGTH_LONG).show();
        }
        if(!isHome) { //Set Icons if it isn't the home profile
            if(isFollowing) {
                tool.getMenu().getItem(1).setIcon(R.drawable.follow_active);
            } else {
                tool.getMenu().getItem(1).setIcon(R.drawable.follow);
            }
            if(muted) {
                tool.getMenu().getItem(2).setIcon(R.drawable.block_active);
            } else {
                tool.getMenu().getItem(2).setIcon(R.drawable.block);
            }
        }
    }
}