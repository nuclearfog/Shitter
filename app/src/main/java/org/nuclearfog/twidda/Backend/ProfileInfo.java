package org.nuclearfog.twidda.Backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.Window.UserProfile;

import twitter4j.Twitter;
import twitter4j.User;

public class ProfileInfo extends AsyncTask<Long,Void,Void>
{
    private String screenName, username, description, location, follower, following;
    private TextView txtUser,txtScrName, txtBio,txtLocation,txtFollowing,txtFollower;
    private ImageView profile, banner;
    private String imageLink, bannerLink;
    private Context context;
    private boolean imgEnabled = false;

    /**
     * @param context "this" Context
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
        txtFollowing = (TextView)((UserProfile)context).findViewById(R.id.following);
        txtFollower = (TextView)((UserProfile)context).findViewById(R.id.follower);
        profile = (ImageView)((UserProfile)context).findViewById(R.id.profile_img);
        banner  = (ImageView)((UserProfile)context).findViewById(R.id.banner);
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
        txtLocation.setText(location);
        txtFollower.setText(follower);
        txtFollowing.setText(following);
        profileImg = new ImageDownloader(profile);
        bannerImg = new ImageDownloader(banner);
        if(imgEnabled) {
            profileImg.execute(imageLink);
            bannerImg.execute(bannerLink);
        }
    }
}