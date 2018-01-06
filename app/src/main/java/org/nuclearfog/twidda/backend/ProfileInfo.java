package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.window.UserProfile;

import twitter4j.Twitter;
import twitter4j.User;

public class ProfileInfo extends AsyncTask<Long,Void,Void>
{
    private String screenName, username, description, location, follower, following;
    private TextView txtUser,txtScrName, txtBio,txtLocation, txtLink,txtFollowing,txtFollower;
    private ImageView profile, banner, linkIcon, locationIcon;
    private String imageLink, bannerLink, link;
    private Context context;
    private boolean imgEnabled = false;

    /**
     * @param context Activity's Context
     */
    public ProfileInfo(Context context) {
        this.context=context;
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        imgEnabled = settings.getBoolean("image_load",false);
    }

    @Override
    protected void onPreExecute() {
        txtUser = (TextView) ((UserProfile)context).findViewById(R.id.profile_username);
        txtScrName =(TextView) ((UserProfile)context).findViewById(R.id.profile_screenname);
        txtBio = (TextView)((UserProfile)context).findViewById(R.id.bio);
        txtLocation = (TextView)((UserProfile)context).findViewById(R.id.location);
        txtLink = (TextView)((UserProfile)context).findViewById(R.id.links);
        txtFollowing = (TextView)((UserProfile)context).findViewById(R.id.following);
        txtFollower = (TextView)((UserProfile)context).findViewById(R.id.follower);
        profile = (ImageView)((UserProfile)context).findViewById(R.id.profile_img);
        banner  = (ImageView)((UserProfile)context).findViewById(R.id.banner);
        linkIcon = (ImageView) ((UserProfile)context).findViewById(R.id.link_img);
        locationIcon = (ImageView) ((UserProfile)context).findViewById(R.id.location_img);
    }

    /**
     * @param args [0] Twitter User ID
     */
    @Override
    protected Void doInBackground(Long... args) {
        TwitterResource mTwitter = TwitterResource.getInstance(context);
        Twitter twitter = mTwitter.getTwitter();
        try {
            User user = twitter.showUser(args[0]);
            screenName = '@'+ user.getScreenName();
            username = user.getName();
            description = user.getDescription();
            location = user.getLocation();
            link = user.getURL();
            follower = "Follower: "+ user.getFollowersCount();
            following = "Following: "+user.getFriendsCount();
            imageLink = user.getProfileImageURL();
            bannerLink = user.getProfileBannerURL();
        } catch(Exception err){err.printStackTrace();}
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        ImageDownloader profileImg, bannerImg;
        txtUser.setText(username);
        txtScrName.setText(screenName);
        txtBio.setText(description);
        txtFollower.setText(follower);
        txtFollowing.setText(following);
        if(location!= null){
            txtLocation.setText(location);
            locationIcon.setVisibility(View.VISIBLE);
        }
        if(link != null){
            txtLink.setText(link);
            linkIcon.setVisibility(View.VISIBLE);
        }


        profileImg = new ImageDownloader(profile);
        bannerImg = new ImageDownloader(banner);
        if(imgEnabled) {
            profileImg.execute(imageLink);
            bannerImg.execute(bannerLink);
        } else{
            profile.setImageResource(R.mipmap.pb);
        }
    }
}