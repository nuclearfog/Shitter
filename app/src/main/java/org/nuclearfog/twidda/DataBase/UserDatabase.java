package org.nuclearfog.twidda.DataBase;

import android.content.Context;
import android.content.SharedPreferences;

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

    public long getUserID(int pos){ return user.get(pos).getId();}
    public String getUsername(int pos){ return user.get(pos).getName();}
    public String getScreenname(int pos){ return user.get(pos).getScreenName();}
    public String getProfileURL(int pos){ return user.get(pos).getProfileBackgroundImageURL();}
    public int getSize(){ return user.size(); }
    public boolean loadImages(){ return toggleImg; }
}