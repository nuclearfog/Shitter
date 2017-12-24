package org.nuclearfog.twidda.Engine;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.Window.Profile;

import twitter4j.Twitter;
import twitter4j.User;

public class ProfileInformation extends AsyncTask<Long,Void,Void>
{
    private Context context,toClass;
    private String screenName, username, description, location, follower, following;
    private TextView txtUser,txtScrName, txtBio,txtLocation,txtFollowing,txtFollower;

    /**
     * @param context "this" Context
     */
    public ProfileInformation(Context context) {
        this.context=context;
        this.toClass = toClass;
    }


    @Override
    protected void onPreExecute() {
        txtUser = (TextView) ((Profile)context).findViewById(R.id.profile_username);
        txtScrName =(TextView) ((Profile)context).findViewById(R.id.profile_screenname);
        txtBio = (TextView)((Profile)context).findViewById(R.id.bio);
        txtLocation = (TextView)((Profile)context).findViewById(R.id.location);
        txtFollowing = (TextView)((Profile)context).findViewById(R.id.following);
        txtFollower = (TextView)((Profile)context).findViewById(R.id.follower);
    }


    /**
     * @param args [0] Username
     */
    @Override
    protected Void doInBackground(Long... args) {
        TwitterStore mTwitter = TwitterStore.getInstance(context);
        Twitter twitter = mTwitter.getTwitter();
        try {
            User user = twitter.showUser(args[0]);
            screenName = user.getScreenName();
            username = "@"+ user.getName();
            description = user.getDescription();
            location = user.getLocation();
            follower = "Follower: "+ user.getFollowersCount();
            following = "Following: "+user.getFriendsCount();
        } catch(Exception err){err.printStackTrace();}
        return null;
    }


    @Override
    protected void onPostExecute(Void v) {
        txtUser.setText(username);
        txtScrName.setText(screenName);
        txtBio.setText(description);
        txtLocation.setText(location);
        txtFollower.setText(follower);
        txtFollowing.setText(following);
    }
}