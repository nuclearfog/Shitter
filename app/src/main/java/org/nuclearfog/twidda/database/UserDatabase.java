package org.nuclearfog.twidda.database;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import twitter4j.User;

public class UserDatabase {

    private List<Long> uID;
    private List<String> uName, scrName, imgUrl;
    private List<Boolean> verified;

    private boolean toggleImg;
    private int size = 0;

    public UserDatabase(Context context, List<User> user) {
        uID = new ArrayList<>();
        uName = new ArrayList<>();
        scrName = new ArrayList<>();
        imgUrl = new ArrayList<>();
        verified = new ArrayList<>();

        SharedPreferences s = context.getSharedPreferences("settings", 0);
        toggleImg = s.getBoolean("image_load", false);

        init(user);
    }

    public long getUserID(int pos){ return uID.get(pos);}
    public String getUsername(int pos){ return uName.get(pos);}
    public String getScreenname(int pos){ return scrName.get(pos);}
    public String getImageUrl(int pos){ return imgUrl.get(pos);}
    public boolean isVerified(int pos){ return verified.get(pos);}
    public int getSize(){ return size; }
    public boolean loadImages(){ return toggleImg; }

    private void init(List<User> user) {
        for(User usr : user) {
            uID.add(usr.getId());
            uName.add(usr.getName());
            scrName.add('@'+usr.getScreenName());
            imgUrl.add(usr.getMiniProfileImageURLHttps());
            verified.add(usr.isVerified());
            size++;
        }
    }
}