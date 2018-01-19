package org.nuclearfog.twidda.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import java.util.List;
import twitter4j.User;

public class UserDatabase {

    private List<User> user;
    private boolean toggleImg;

    public UserDatabase(Context context, List<User> user) {
        this.user = user;
        SharedPreferences s = context.getSharedPreferences("settings", 0);
        toggleImg = s.getBoolean("image_load", false);
    }

    public UserDatabase(Context context, List<User> user, List<Bitmap> pbImg){}

    public long getUserID(int pos){ return user.get(pos).getId();}
    public String getUsername(int pos){ return user.get(pos).getName();}
    public String getScreenname(int pos){ return '@' + user.get(pos).getScreenName();}
    public String getImageUrl(int pos){ return user.get(pos).getMiniProfileImageURL();}
    public int getSize(){ return user.size(); }
    public boolean loadImages(){ return toggleImg; }
}