package org.nuclearfog.twidda.backend.apiold;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.utils.ProxySetup;
import org.nuclearfog.twidda.backend.utils.TLSSocketFactory;
import org.nuclearfog.twidda.backend.utils.Tokens;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UploadedMedia;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Backend for the Twitter API. All app actions are managed here.
 *
 * @author nuclearfog
 */
public class TwitterEngine {

    private static final TwitterEngine mTwitter = new TwitterEngine();

    private GlobalSettings settings;
    private Tokens tokens;
    private Twitter twitter;

    private boolean isInitialized = false;


    private TwitterEngine() {
    }

    /**
     * Initialize Twitter4J instance
     */
    private void initTwitter(AccessToken aToken) {
        TLSSocketFactory.getSupportTLSifNeeded();
        ConfigurationBuilder builder = new ConfigurationBuilder();
        // set API keys
        builder.setOAuthConsumerKey(tokens.getConsumerKey());
        builder.setOAuthConsumerSecret(tokens.getConsumerSec());
        // Twitter4J has its own proxy settings
        if (settings.isProxyEnabled()) {
            builder.setHttpProxyHost(settings.getProxyHost());
            builder.setHttpProxyPort(settings.getProxyPortNumber());
            if (settings.isProxyAuthSet()) {
                builder.setHttpProxyUser(settings.getProxyUser());
                builder.setHttpProxyPassword(settings.getProxyPass());
            }
        }
        // init proxy connection
        ProxySetup.setConnection(settings);
        // init Twitter instance
        TwitterFactory factory = new TwitterFactory(builder.build());
        if (aToken != null) {
            twitter = factory.getInstance(aToken);
        } else {
            twitter = factory.getInstance();
        }
    }

    /**
     * get singleton instance
     *
     * @param context Main Thread Context
     * @return TwitterEngine Instance
     */
    public static TwitterEngine getInstance(Context context) {
        if (!mTwitter.isInitialized) {
            mTwitter.isInitialized = true;
            // initialize database and settings
            mTwitter.settings = GlobalSettings.getInstance(context);
            mTwitter.tokens = Tokens.getInstance(context);
            // check if already logged in
            if (mTwitter.settings.isLoggedIn()) {
                // init login access
                String accessToken = mTwitter.settings.getAccessToken();
                String tokenSecret = mTwitter.settings.getTokenSecret();
                AccessToken token = new AccessToken(accessToken, tokenSecret);
                mTwitter.initTwitter(token);
            } else {
                // init empty session
                mTwitter.initTwitter(null);
            }
        }
        return mTwitter;
    }

    /**
     * reset Twitter state
     */
    public static void resetTwitter() {
        mTwitter.isInitialized = false;
    }

    /**
     * get a list of blocked/muted user IDs
     *
     * @return list of user IDs
     * @throws EngineException if twitter service is unavailable
     */
    public List<Long> getExcludedUserIDs() throws EngineException {
        try {
            IDs[] ids = new IDs[2];
            ids[0] = twitter.getBlocksIDs();
            ids[1] = twitter.getBlocksIDs();
            Set<Long> idSet = new TreeSet<>();
            for (IDs id : ids) {
                for (long userId : id.getIDs()) {
                    idSet.add(userId);
                }
            }
            return new ArrayList<>(idSet);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }

    /**
     * update current users profile
     *
     * @param name       new username
     * @param url        new profile link or empty if none
     * @param loc        new location name or empty if none
     * @param bio        new bio description or empty if none
     * @param profileImg local path to the profile image or null
     * @param bannerImg  local path to the banner image or null
     * @return updated user information
     * @throws EngineException if access is unavailable
     */
    public User updateProfile(String name, String url, String loc, String bio, String profileImg, String bannerImg) throws EngineException {
        try {
            if (profileImg != null && !profileImg.isEmpty())
                twitter.updateProfileImage(new File(profileImg));
            if (bannerImg != null && !bannerImg.isEmpty())
                twitter.updateProfileBanner(new File(bannerImg));
            twitter4j.User user = twitter.updateProfile(name, url, loc, bio);
            return new UserV1(user, twitter.getId());
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * download image from Twitter
     *
     * @param link link of the image
     * @return bitmap image
     * @throws EngineException if image loading failed
     */
    @Nullable
    public Bitmap getImage(String link) throws EngineException {
        try {
            URL url = new URL(link);
            InputStream stream = url.openConnection().getInputStream();
            return BitmapFactory.decodeStream(stream);
        } catch (IOException err) {
            throw new EngineException(EngineException.BITMAP_FAILURE);
        }
    }

    /**
     * upload image to twitter and return unique media ID
     *
     * @param path path to the media file
     * @return media ID
     * @throws EngineException if twitter service is unavailable or media not found
     */
    public long uploadImage(@NonNull String path) throws EngineException {
        try {
            UploadedMedia media = twitter.uploadMedia("", new FileInputStream(path));
            return media.getMediaId();
        } catch (FileNotFoundException err) {
            throw new EngineException(EngineException.FILENOTFOUND);
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Upload video to twitter and return unique media ID
     *
     * @param path path to the media file
     * @return media ID
     * @throws EngineException if twitter service is unavailable or media not found
     */
    public long uploadVideo(@NonNull String path) throws EngineException {
        try {
            UploadedMedia media = twitter.uploadMediaChunked("", new FileInputStream(path));
            return media.getMediaId();
        } catch (FileNotFoundException err) {
            throw new EngineException(EngineException.FILENOTFOUND);
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }
}